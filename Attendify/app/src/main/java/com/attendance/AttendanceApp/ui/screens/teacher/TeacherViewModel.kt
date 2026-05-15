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

    val activeSession: StateFlow<Session?> = attendanceRepository.getAllSessions()
        .map { sessions ->
            sessions.filter { it.isActive }.sortedByDescending { it.date }.firstOrNull()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    fun getAttendanceForSession(sessionId: String): Flow<List<JoinedStudent>> {
        if (sessionId.isBlank()) return flowOf(emptyList())
        return combine(
            attendanceRepository.getAttendanceBySession(sessionId),
            userRepository.getUsersByRole(Role.student)
        ) { records, students ->
            records.map { record ->
                val student = students.find { it.id == record.studentId }
                JoinedStudent(
                    name = student?.name ?: "Unknown Student",
                    id = record.studentId,
                    time = try {
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(record.timestamp))
                    } catch (e: Exception) {
                        "--:--"
                    }
                )
            }
        }
    }

    fun getCourseNameByScheduleIdFlow(scheduleId: String): Flow<String> {
        return schedulesWithDetails.map { details ->
            details.find { it.schedule.scheduleId == scheduleId }?.courseName ?: "Course $scheduleId"
        }
    }

    fun getCourseNameByScheduleId(scheduleId: String): String {
        return schedulesWithDetails.value.find { it.schedule.scheduleId == scheduleId }?.courseName ?: "Course $scheduleId"
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
            teacherSchedules.filter { it.isNotEmpty() }.first().let { schedules ->
                // Only notify if no "New Course Assigned" notification exists yet
                notificationRepository.getNotificationsByUser(currentUserId).first().let { existing ->
                    val alreadyNotified = existing.any { it.title == "New Course Assigned" }
                    if (!alreadyNotified) {
                        notificationRepository.insertNotification(
                            Notification(
                                id = "",
                                userId = currentUserId,
                                title = "New Course Assigned",
                                message = "You have ${schedules.size} courses assigned for the current semester.",
                                type = "info",
                                isRead = false,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        }
    }
}

data class ScheduleWithDetails(
    val schedule: Schedule,
    val courseName: String,
    val deptName: String
)

data class JoinedStudent(val name: String, val id: String, val time: String)

sealed class TeacherUiState {
    object Idle : TeacherUiState()
    object Loading : TeacherUiState()
    data class Success(val message: String, val sessionId: String = "") : TeacherUiState()
    data class Error(val message: String) : TeacherUiState()
}