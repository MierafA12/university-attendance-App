package com.attendance.attendanceapp.ui.screens.teacher

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

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TeacherViewModel(
    val academicRepository: AcademicRepository,
    val userRepository: UserRepository,
    val attendanceRepository: AttendanceRepository,
    val notificationRepository: com.attendance.attendanceapp.domain.repository.NotificationRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<TeacherUiState>(TeacherUiState.Idle)
    val uiState: State<TeacherUiState> = _uiState

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val currentUser: StateFlow<User?> = userRepository.getUserById(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val departments: StateFlow<List<Department>> = academicRepository.getAllDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courses: StateFlow<List<Course>> = academicRepository.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teacherSchedules: StateFlow<List<Schedule>> = academicRepository.getSchedulesByTeacher(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schedulesWithDetails: StateFlow<List<ScheduleWithDetails>> = combine(
        teacherSchedules,
        courses,
        departments
    ) { schedules, courseList, deptList ->
        schedules.map { schedule ->
            ScheduleWithDetails(
                schedule = schedule,
                courseName = courseList.find { it.id == schedule.courseId }?.name ?: schedule.courseId,
                deptName = deptList.find { it.id == schedule.departmentId }?.name ?: schedule.departmentId
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySchedules: StateFlow<List<ScheduleWithDetails>> = schedulesWithDetails.map { schedules ->
        try {
            val date = Date()
            val fullDay = SimpleDateFormat("EEEE", Locale.ENGLISH).format(date).trim()
            val shortDay = SimpleDateFormat("EEE", Locale.ENGLISH).format(date).trim()
            
            schedules.filter { 
                val dbDay = it.schedule.dayOfWeek.trim()
                dbDay.equals(fullDay, ignoreCase = true) || dbDay.equals(shortDay, ignoreCase = true)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<Attendance>> = attendanceRepository.getAllAttendance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<Session>> = attendanceRepository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCourseId = MutableStateFlow("")
    val selectedCourseId: StateFlow<String> = _selectedCourseId.asStateFlow()

    fun setCourseFilter(courseId: String) {
        _selectedCourseId.value = courseId
    }

    val studentReports: StateFlow<List<StudentReportItem>> = combine(
        userRepository.getUsersByRole(Role.student),
        userRepository.getAllStudentProfiles(),
        attendanceRepository.getAllSessions(),
        attendanceRepository.getAllAttendance(),
        combine(teacherSchedules, _selectedCourseId, departments) { s, c, d -> Triple(s, c, d) }
    ) { users, studentProfiles, sessions, attendances, filterTriple ->
        val (schedules, courseFilter, deptList) = filterTriple
        val teacherScheduleIds = schedules.map { it.scheduleId }.toSet()
        val teacherSessions = sessions.filter { it.scheduleId in teacherScheduleIds }

        users.mapNotNull { user ->
            val profile = studentProfiles.find { it.userId == user.id } ?: return@mapNotNull null
            
            val studentYear = profile.year.filter { it.isDigit() }
            val studentSem = profile.semester.filter { it.isDigit() }
            val studentDeptId = profile.departmentId?.trim() ?: ""

            // Filter sessions that are relevant to this student's profile AND the teacher's schedules
            val studentRelevantSessions = teacherSessions.filter { session ->
                val schedule = schedules.find { it.scheduleId == session.scheduleId } ?: return@filter false
                val matchesCourse = courseFilter.isEmpty() || schedule.courseId == courseFilter
                
                // 1. Normalize strings for flexible matching (e.g. "4" matches "Year 4")
                val scheduleYear = schedule.year.filter { it.isDigit() }
                val scheduleSem = schedule.semester.filter { it.isDigit() }
                val scheduleDeptId = schedule.departmentId.trim()

                val yearMatch = studentYear == scheduleYear || profile.year.trim().equals(schedule.year.trim(), ignoreCase = true)
                val semMatch = studentSem == scheduleSem || profile.semester.trim().equals(schedule.semester.trim(), ignoreCase = true)
                
                // 2. Robust Department Matching (ID or Name)
                val deptMatch = if (studentDeptId.isEmpty()) false 
                else {
                    scheduleDeptId.equals(studentDeptId, ignoreCase = true) || 
                    deptList.find { it.id == scheduleDeptId }?.name?.equals(studentDeptId, ignoreCase = true) == true ||
                    deptList.find { it.id == studentDeptId }?.name?.equals(scheduleDeptId, ignoreCase = true) == true
                }

                yearMatch && semMatch && deptMatch && matchesCourse
            }
            
            val totalSessions = studentRelevantSessions.size
            // If filtering by course, only show students who have at least one session in that course
            if (courseFilter.isNotEmpty() && totalSessions == 0) return@mapNotNull null
            
            val sessionIds = studentRelevantSessions.map { it.id }.toSet()
            
            // Check for both Firebase UID and Student ID to handle legacy or mixed records
            val studentIds = setOfNotNull(user.id, profile.studentId)
            // Count specific statuses
            val studentAttendanceRecords = attendances.filter { it.studentId in studentIds && it.sessionId in sessionIds }
            val presentCount = studentAttendanceRecords.count { it.status == AttendanceStatus.Present }
            val permissionCount = studentAttendanceRecords.count { it.status == AttendanceStatus.Permission }
            
            // "Permission attendance ignored": subtract permission sessions from the total denominator
            val effectiveTotalSessions = totalSessions - permissionCount
            
            val percentage = if (effectiveTotalSessions > 0) {
                (presentCount.toFloat() / effectiveTotalSessions.toFloat()) * 100f
            } else {
                0f
            }

            StudentReportItem(
                studentId = user.id,
                name = user.name,
                departmentId = profile.departmentId ?: "",
                semester = profile.semester,
                year = profile.year,
                attendancePercentage = percentage,
                presentCount = presentCount,
                totalSessions = effectiveTotalSessions
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun isSessionTrulyActive(session: Session): Boolean {
        if (!session.isActive) return false
        val now = System.currentTimeMillis()
        val expiryTime = session.date + (session.durationMinutes.toLong() * 60 * 1000)
        return now < expiryTime
    }

    val activeSession: StateFlow<Session?> = combine(
        attendanceRepository.getAllSessions(),
        teacherSchedules
    ) { sessions, schedules ->
        val teacherScheduleIds = schedules.map { it.scheduleId }.toSet()
        sessions.filter { 
            it.isActive && 
            it.scheduleId in teacherScheduleIds && 
            isSessionTrulyActive(it) 
        }.sortedByDescending { it.date }
            .firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeSessionAttendanceCount: StateFlow<Int> = activeSession
        .flatMapLatest { session ->
            if (session == null) flowOf(emptyList())
            else attendanceRepository.getAttendanceBySession(session.id)
        }
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun startSession(scheduleId: String, durationMinutes: Int = 15, maxStudents: Int = 0) {
        viewModelScope.launch {
            _uiState.value = TeacherUiState.Loading
            try {
                // 1. Deactivate any existing active sessions
                attendanceRepository.deactivateAllSessions()

                // 2. Create the new session
                val session = Session(
                    id = "",
                    scheduleId = scheduleId,
                    qrCode = (100000..999999).random().toString(),
                    date = System.currentTimeMillis(),
                    isActive = true,
                    durationMinutes = durationMinutes,
                    maxStudents = maxStudents
                )
                val sessionId = attendanceRepository.insertSession(session)
                _uiState.value = TeacherUiState.Success("Session started successfully", sessionId)
            } catch (e: Exception) {
                android.util.Log.e("TeacherVM", "Start session failed", e)
                _uiState.value = TeacherUiState.Error(e.message ?: "Failed to start session")
            }
        }
    }

    fun stopSession(sessionId: String) {
        viewModelScope.launch {
            try {
                // Use the active session if it matches, otherwise try to find it
                val session = activeSession.value?.takeIf { it.id == sessionId }
                
                if (session != null) {
                    val updatedSession = session.copy(isActive = false)
                    attendanceRepository.insertSession(updatedSession)
                } else {
                    attendanceRepository.deactivateAllSessions()
                }
                _uiState.value = TeacherUiState.Success("Session stopped")
            } catch (e: Exception) {
                android.util.Log.e("TeacherVM", "Stop session failed", e)
                _uiState.value = TeacherUiState.Error(e.message ?: "Failed to stop session")
            }
        }
    }

    fun getAttendanceForSession(sessionId: String): Flow<List<SessionStudentReport>> {
        if (sessionId.isBlank()) return flowOf(emptyList())
        
        return combine(
            attendanceRepository.getAllSessions(),
            teacherSchedules,
            userRepository.getUsersByRole(Role.student),
            userRepository.getAllStudentProfiles(),
            combine(attendanceRepository.getAttendanceBySession(sessionId), departments) { r, d -> Pair(r, d) }
        ) { sessions, schedules, allStudents, profiles, recordsDeptPair ->
            val (records, deptList) = recordsDeptPair
            val session = sessions.find { it.id == sessionId } ?: return@combine emptyList()
            val schedule = schedules.find { it.scheduleId == session.scheduleId } ?: return@combine emptyList()
            
            val scheduleYear = schedule.year.filter { it.isDigit() }
            val scheduleSem = schedule.semester.filter { it.isDigit() }
            val scheduleDeptId = schedule.departmentId.trim()

            // Filter students who should be in this session
            val relevantStudents = allStudents.filter { user ->
                val profile = profiles.find { it.userId == user.id } ?: return@filter false
                
                val studentYear = profile.year.filter { it.isDigit() }
                val studentSem = profile.semester.filter { it.isDigit() }
                val studentDeptId = profile.departmentId?.trim() ?: ""

                val yearMatch = studentYear == scheduleYear || profile.year.trim().equals(schedule.year.trim(), ignoreCase = true)
                val semMatch = studentSem == scheduleSem || profile.semester.trim().equals(schedule.semester.trim(), ignoreCase = true)
                val deptMatch = scheduleDeptId.equals(studentDeptId, ignoreCase = true) || 
                               deptList.find { it.id == scheduleDeptId }?.name?.equals(studentDeptId, ignoreCase = true) == true ||
                               deptList.find { it.id == studentDeptId }?.name?.equals(scheduleDeptId, ignoreCase = true) == true
                
                yearMatch && semMatch && deptMatch
            }

            relevantStudents.map { student ->
                // Sort records by timestamp to always get the latest update if duplicates exist
                val record = records.filter { it.studentId == student.id }.sortedByDescending { it.timestamp }.firstOrNull()
                SessionStudentReport(
                    studentId = student.id,
                    name = student.name,
                    status = record?.status ?: AttendanceStatus.Absent,
                    time = record?.let { 
                        try {
                            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it.timestamp))
                        } catch (e: Exception) { "--:--" }
                    } ?: "--:--"
                )
            }.sortedBy { it.name }
        }
    }

    fun updateAttendanceStatus(sessionId: String, studentId: String, status: AttendanceStatus) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Query the repository directly for the latest session attendance to avoid stale or uninitialized state
                val records = attendanceRepository.getAttendanceBySession(sessionId).first()
                val existing = records.find { it.studentId == studentId }
                
                if (existing != null) {
                    // Update existing record
                    attendanceRepository.markAttendance(existing.copy(
                        status = status,
                        timestamp = System.currentTimeMillis()
                    ))
                } else {
                    // Create new record if one doesn't exist (e.g. marking an Absent student as Present/Permission)
                    val newRecord = Attendance(
                        id = UUID.randomUUID().toString(),
                        studentId = studentId,
                        sessionId = sessionId,
                        status = status,
                        timestamp = System.currentTimeMillis()
                    )
                    attendanceRepository.markAttendance(newRecord)
                }
            } catch (e: Exception) {
                android.util.Log.e("TeacherVM", "Failed to update status: ${e.message}")
            }
        }
    }

    fun getCourseNameByScheduleIdFlow(scheduleId: String): Flow<String> {
        return combine(academicRepository.getAllSchedules(), academicRepository.getAllCourses()) { schedules, courseList ->
            val schedule = schedules.find { it.scheduleId == scheduleId }
            val courseId = schedule?.courseId ?: ""
            courseList.find { it.id == courseId }?.name ?: courseId.ifEmpty { "Unknown Course" }
        }
    }

    fun getCourseNameByScheduleId(scheduleId: String): String {
        val schedule = teacherSchedules.value.find { it.scheduleId == scheduleId }
        val courseId = schedule?.courseId ?: return "Unknown Course"
        return courses.value.find { it.id == courseId }?.name ?: courseId
    }

    fun getCourseName(courseId: String): String {
        return courses.value.find { it.id == courseId }?.name ?: courseId
    }

    fun getDeptName(deptId: String): String {
        return departments.value.find { it.id == deptId }?.name ?: deptId
    }

    fun getDeptNameByScheduleId(scheduleId: String): String {
        val schedule = teacherSchedules.value.find { it.scheduleId == scheduleId }
        return if (schedule != null) getDeptName(schedule.departmentId) else scheduleId
    }

    fun getStudentsForCourse(courseId: String): Flow<List<User>> {
        return academicRepository.getCourseById(courseId).flatMapLatest { course ->
            if (course == null) flowOf(emptyList())
            else {
                userRepository.getUsersByRole(Role.student).map { students ->
                    students.filter { it.id.isNotBlank() }
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = TeacherUiState.Idle
    }

    init {
        checkNewSchedules()
        monitorSessionCapacity()
        monitorSessionTimeout()
    }

    private fun monitorSessionTimeout() {
        viewModelScope.launch {
            while (true) {
                val currentActive = activeSession.value
                if (currentActive != null && !isSessionTrulyActive(currentActive)) {
                    stopSession(currentActive.id)
                }
                kotlinx.coroutines.delay(30000) // Check every 30 seconds
            }
        }
    }

    private fun monitorSessionCapacity() {
        viewModelScope.launch {
            combine(activeSession, activeSessionAttendanceCount) { session, count ->
                Pair(session, count)
            }.collect { (session, count) ->
                if (session != null && session.isActive && session.maxStudents > 0 && count >= session.maxStudents) {
                    stopSession(session.id)
                }
            }
        }
    }

    private fun checkNewSchedules() {
        viewModelScope.launch {
            try {
                // Use collectLatest to respond to the first emission without hanging indefinitely if empty
                teacherSchedules.collectLatest { schedules ->
                    if (schedules.isNotEmpty() && currentUserId.isNotEmpty()) {
                        // Only notify if no "New Course Assigned" notification exists yet
                        val notificationId = "COURSE_ASSIGNED_${currentUserId}"
                        val existing = notificationRepository.getNotificationsByUser(currentUserId).first()
                        val alreadyNotified = existing.any { it.id == notificationId || it.title == "New Course Assigned" }
                        
                        if (!alreadyNotified) {
                            notificationRepository.insertNotification(
                                Notification(
                                    id = notificationId,
                                    userId = currentUserId,
                                    title = "New Course Assigned",
                                    message = "You have ${schedules.size} courses assigned for the current semester.",
                                    type = "info",
                                    isRead = false,
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                        // We found schedules and handled notification, can stop collecting
                        return@collectLatest 
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("TeacherVM", "Error in checkNewSchedules", e)
            }
        }
    }
}

data class ScheduleWithDetails(
    val schedule: Schedule,
    val courseName: String,
    val deptName: String
)

data class JoinedStudent(val name: String, val id: String, val time: String) // Keep for backward compatibility if needed

sealed class TeacherUiState {
    object Idle : TeacherUiState()
    object Loading : TeacherUiState()
    data class Success(val message: String, val sessionId: String = "") : TeacherUiState()
    data class Error(val message: String) : TeacherUiState()
}