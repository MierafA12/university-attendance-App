package com.attendance.attendanceapp.ui.screens.student

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.attendanceapp.domain.model.*
import com.attendance.attendanceapp.domain.repository.AcademicRepository
import com.attendance.attendanceapp.domain.repository.AttendanceRepository
import com.attendance.attendanceapp.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StudentViewModel(
    private val academicRepository: AcademicRepository,
    private val userRepository: UserRepository,
    private val attendanceRepository: AttendanceRepository,
    private val notificationRepository: com.attendance.attendanceapp.domain.repository.NotificationRepository
) : ViewModel() {
    


    private val _uiState = mutableStateOf<StudentUiState>(StudentUiState.Idle)
    val uiState: State<StudentUiState> = _uiState

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val currentUser: StateFlow<User?> = userRepository.getUserById(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val studentProfile: StateFlow<Student?> = userRepository.getStudentByUserId(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All schedules for the student's department and year/semester
    val studentSchedules: StateFlow<List<Schedule>> = combine(
        academicRepository.getAllSchedules(),
        academicRepository.getAllDepartments(),
        studentProfile
    ) { schedules, departments, profile ->
        if (profile == null) emptyList()
        else {
            val normalizedProfileYear = profile.year.filter { it.isDigit() }
            val profileDeptId = profile.departmentId?.trim() ?: ""
            
            schedules.filter { schedule ->
                val normalizedScheduleYear = schedule.year.filter { it.isDigit() }
                
                // Match year and semester
                val yearMatch = normalizedScheduleYear == normalizedProfileYear || schedule.year.trim() == profile.year.trim()
                val semesterMatch = schedule.semester.trim() == profile.semester.trim()
                
                // Match department (check ID first, then name)
                val scheduleDeptId = schedule.departmentId.trim()
                val deptMatch = if (profileDeptId.isEmpty()) false 
                else {
                    // 1. Direct ID or Name match
                    scheduleDeptId == profileDeptId || 
                    // 2. Resolve Schedule ID to Name and compare
                    departments.find { it.id == scheduleDeptId }?.name?.equals(profileDeptId, ignoreCase = true) == true ||
                    // 3. Resolve Profile ID to Name and compare
                    departments.find { it.id == profileDeptId }?.name?.equals(scheduleDeptId, ignoreCase = true) == true ||
                    // 4. Cross-resolve names for both
                    run {
                        val scheduleDeptName = departments.find { it.id == scheduleDeptId }?.name
                        val profileDeptName = departments.find { it.id == profileDeptId }?.name
                        scheduleDeptName != null && profileDeptName != null && scheduleDeptName.equals(profileDeptName, ignoreCase = true)
                    } ||
                    // 5. Partial matches as fallback
                    (profileDeptId.length > 3 && scheduleDeptId.contains(profileDeptId, ignoreCase = true)) ||
                    (scheduleDeptId.length > 3 && profileDeptId.contains(scheduleDeptId, ignoreCase = true))
                }
                
                yearMatch && semesterMatch && deptMatch
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // A list of course IDs that are currently in the student's schedule
    val scheduledCourseIds: StateFlow<Set<String>> = studentSchedules.map { schedules ->
        schedules.map { it.courseId }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // All courses for the student's program (with schedule fallback)
    val studentCourses: StateFlow<List<Course>> = combine(
        academicRepository.getAllCourses(),
        academicRepository.getAllDepartments(),
        studentProfile,
        scheduledCourseIds
    ) { courses, departments, profile, scheduledIds ->
        if (profile == null) emptyList()
        else {
            val normalizedProfileYear = profile.year.filter { it.isDigit() }
            val profileDeptId = profile.departmentId?.trim() ?: ""
            
            courses.filter { course ->
                // If it's already in the schedule, it's definitely a student course
                if (scheduledIds.contains(course.id)) return@filter true

                val normalizedCourseYear = course.year.filter { it.isDigit() }
                val yearMatch = normalizedCourseYear == normalizedProfileYear || course.year.trim() == profile.year.trim()
                val semesterMatch = course.semester.trim() == profile.semester.trim()
                
                val courseDeptId = course.departmentId.trim()
                val deptMatch = if (profileDeptId.isEmpty()) false
                else {
                    courseDeptId == profileDeptId ||
                    departments.find { it.id == courseDeptId }?.name?.equals(profileDeptId, ignoreCase = true) == true ||
                    departments.find { it.id == profileDeptId }?.name?.equals(courseDeptId, ignoreCase = true) == true ||
                    run {
                        val courseDeptName = departments.find { it.id == courseDeptId }?.name
                        val profileDeptName = departments.find { it.id == profileDeptId }?.name
                        courseDeptName != null && profileDeptName != null && courseDeptName.equals(profileDeptName, ignoreCase = true)
                    } ||
                    (profileDeptId.length > 3 && courseDeptId.contains(profileDeptId, ignoreCase = true)) ||
                    (courseDeptId.length > 3 && profileDeptId.contains(courseDeptId, ignoreCase = true))
                }
                
                yearMatch && semesterMatch && deptMatch
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySchedules: StateFlow<List<Schedule>> = studentSchedules.map { schedules ->
        val date = Date()
        val fullDay = SimpleDateFormat("EEEE", Locale.ENGLISH).format(date).trim()
        val shortDay = SimpleDateFormat("EEE", Locale.ENGLISH).format(date).trim()
        
        schedules.filter { 
            val dbDay = it.dayOfWeek.trim()
            dbDay.equals(fullDay, ignoreCase = true) || dbDay.equals(shortDay, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceHistory: StateFlow<List<Attendance>> = studentProfile.flatMapLatest { profile ->
        val uid = currentUserId
        val studentId = profile?.studentId
        
        val uidFlow = attendanceRepository.getAttendanceByStudent(uid)
        val studentIdFlow = if (studentId != null && studentId != uid) {
            attendanceRepository.getAttendanceByStudent(studentId)
        } else {
            flowOf(emptyList())
        }
        
        combine(uidFlow, studentIdFlow) { list1, list2 ->
            (list1 + list2).distinctBy { it.sessionId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courses: StateFlow<List<Course>> = academicRepository.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<Session>> = attendanceRepository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courseNames: StateFlow<Map<String, String>> = courses.map { list ->
        list.associate { it.id to it.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val attendanceReport: StateFlow<List<CourseAttendanceReport>> = combine(
        studentSchedules,
        courses,
        attendanceHistory,
        allSessions
    ) { schedules, allCourses, history, sessions ->
        try {
            // Group schedules by courseId to ensure each course appears only once in the report
            val schedulesByCourse = schedules.groupBy { it.courseId }
            
            schedulesByCourse.map { (courseId, courseSchedules) ->
                val course = allCourses.find { it.id == courseId }
                val courseName = (course?.name ?: "Course $courseId").trim()
                
                // Collect all session IDs for all schedules of this course
                val courseScheduleIds = courseSchedules.map { it.scheduleId }.toSet()
                val courseSessions = sessions.filter { it.scheduleId in courseScheduleIds }
                val totalSessions = courseSessions.size
                
                // Attendance for these sessions
                val presentCount = history.count { record -> 
                    courseSessions.any { it.id == record.sessionId } && 
                    record.status == AttendanceStatus.Present
                }
                
                val absences = (totalSessions - presentCount).coerceAtLeast(0)
                val percentage = if (totalSessions > 0) (presentCount.toFloat() / totalSessions) * 100 else 100f
                
                CourseAttendanceReport(
                    courseId = courseId,
                    courseName = courseName,
                    presentCount = presentCount,
                    totalSessions = totalSessions,
                    percentage = percentage,
                    absences = absences
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("StudentVM", "Error calculating attendance report", e)
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallAttendance: StateFlow<Float> = attendanceReport.map { report ->
        try {
            if (report.isEmpty()) 100f
            else {
                val totalPresent = report.sumOf { it.presentCount }
                val totalPossible = report.sumOf { it.totalSessions }
                if (totalPossible > 0) (totalPresent.toFloat() / totalPossible) * 100f else 100f
            }
        } catch (e: Exception) {
            100f
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100f)

    val detailedAttendanceHistory: StateFlow<List<AttendanceRecord>> = combine(
        allSessions,
        attendanceHistory,
        courseNames,
        studentSchedules
    ) { sessions, history, names, schedules ->
        try {
            // Create a record for every attendance in history
            history.map { record ->
                val session = sessions.find { it.id == record.sessionId }
                val scheduleId = session?.scheduleId ?: ""
                val schedule = schedules.find { it.scheduleId == scheduleId }
                val courseId = schedule?.courseId ?: ""
                
                AttendanceRecord(
                    sessionId = record.sessionId,
                    courseId = courseId,
                    courseName = if (courseId.isNotEmpty()) (names[courseId] ?: "Course $courseId").trim() else "Unknown Class",
                    status = record.status,
                    timestamp = record.timestamp,
                    isAbsent = record.status == AttendanceStatus.Absent
                )
            }.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            android.util.Log.e("StudentVM", "Error calculating detailed history", e)
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val criticalCourses: StateFlow<List<CourseAttendanceReport>> = attendanceReport.map { report ->
        report.filter { it.absences >= 3 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markAttendance(qrCode: String) {
        viewModelScope.launch {
            _uiState.value = StudentUiState.Loading
            try {
                val profile = studentProfile.value
                val studentIdToUse = profile?.studentId ?: currentUserId
                
                // Use the new repository method that checks both local and remote
                val session = attendanceRepository.findSessionByQrCode(qrCode)
                
                if (session != null) {
                    // ALWAYS use the Firebase UID (currentUserId) as the primary identifier for attendance
                    // This ensures the teacher can look up the user details and the student can see their history.
                    val studentIdToUse = currentUserId
                    
                    // Check if already marked (fetch latest history)
                    val history = attendanceRepository.getAttendanceByStudent(studentIdToUse).first()
                    val alreadyMarked = history.any { it.sessionId == session.id }
                    
                    if (alreadyMarked) {
                        _uiState.value = StudentUiState.Error("Attendance already marked for this session")
                        return@launch
                    }

                    val attendance = Attendance(
                        id = UUID.randomUUID().toString(),
                        studentId = studentIdToUse,
                        sessionId = session.id,
                        status = AttendanceStatus.Present,
                        timestamp = System.currentTimeMillis()
                    )
                    attendanceRepository.markAttendance(attendance)
                    _uiState.value = StudentUiState.Success("Attendance marked successfully!")
                }
 else {
                    _uiState.value = StudentUiState.Error("Invalid or expired QR code (Session not found or inactive)")
                }
            } catch (e: Exception) {
                _uiState.value = StudentUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun getCourseName(courseId: String): String {
        return courseNames.value[courseId] ?: courseId
    }

    fun getCourseNameFromSession(sessionId: String): String {
        val session = allSessions.value.find { it.id == sessionId }
        val scheduleId = session?.scheduleId
        val schedule = studentSchedules.value.find { it.scheduleId == scheduleId }
        return if (schedule != null) getCourseName(schedule.courseId) else "Unknown Class"
    }

    fun resetState() {
        _uiState.value = StudentUiState.Idle
    }

    init {
        if (currentUserId.isNotEmpty()) {
            checkAndGenerateNotifications()
        }
    }

    private fun checkAndGenerateNotifications() {
        if (currentUserId.isEmpty()) return
        viewModelScope.launch {
            try {
                attendanceReport.collect { reports ->
                    if (currentUserId.isEmpty()) return@collect
                    val existing = notificationRepository.getNotificationsByUser(currentUserId).first()
                    reports.forEach { report ->
                        if (report.absences >= 3) {
                            val notificationId = "ATTENDANCE_WARN_${report.courseId}_${currentUserId}"
                            val alreadyNotified = existing.any { it.id == notificationId || (it.title == "Attendance Warning" && it.message.contains(report.courseName)) }
                            if (!alreadyNotified) {
                                notificationRepository.insertNotification(
                                    Notification(
                                        id = notificationId,
                                        userId = currentUserId,
                                        title = "Attendance Warning",
                                        message = "You have ${report.absences} absences in ${report.courseName}. Please attend classes to avoid penalties.",
                                        type = "warning",
                                        isRead = false,
                                        createdAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("StudentVM", "Error in notification check", e)
            }
        }
    }
}

sealed class StudentUiState {
    object Idle : StudentUiState()
    object Loading : StudentUiState()
    data class Success(val message: String) : StudentUiState()
    data class Error(val message: String) : StudentUiState()
}

data class AttendanceRecord(
    val sessionId: String,
    val courseId: String,
    val courseName: String,
    val status: AttendanceStatus,
    val timestamp: Long,
    val isAbsent: Boolean
)
