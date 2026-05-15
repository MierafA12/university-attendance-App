package com.attendance.attendanceapp.ui.screens.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.attendanceapp.domain.model.*
import com.attendance.attendanceapp.domain.repository.AcademicRepository
import com.attendance.attendanceapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminViewModel(
    private val academicRepository: AcademicRepository,
    private val userRepository: UserRepository,
    private val attendanceRepository: com.attendance.attendanceapp.domain.repository.AttendanceRepository,
    private val notificationRepository: com.attendance.attendanceapp.domain.repository.NotificationRepository
) : ViewModel() {


    private val _uiState = mutableStateOf<AdminUiState>(AdminUiState.Idle)
    val uiState: State<AdminUiState> = _uiState

    val currentUser: StateFlow<User?> = userRepository.getUserById(
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val departments: StateFlow<List<Department>> = academicRepository.getAllDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teachers: StateFlow<List<User>> = userRepository.getUsersByRole(Role.teacher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val students: StateFlow<List<User>> = userRepository.getUsersByRole(Role.student)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courses: StateFlow<List<Course>> = academicRepository.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSchedules: StateFlow<List<Schedule>> = academicRepository.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<Session>> = attendanceRepository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<Attendance>> = attendanceRepository.getAllAttendance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCourseId = MutableStateFlow("")
    val selectedCourseId: StateFlow<String> = _selectedCourseId.asStateFlow()

    fun setCourseFilter(courseId: String) {
        _selectedCourseId.value = courseId
    }

    val studentReports: StateFlow<List<StudentReportItem>> = combine(
        userRepository.getUsersByRole(Role.student),
        userRepository.getAllStudentProfiles(),
        allSessions,
        allAttendance,
        combine(allSchedules, _selectedCourseId, departments) { s, c, d -> Triple(s, c, d) }
    ) { users, studentProfiles, sessions, attendances, filterTriple ->
        val (schedules, courseFilter, deptList) = filterTriple
        users.mapNotNull { user ->
            val profile = studentProfiles.find { it.userId == user.id } ?: return@mapNotNull null
            
            val studentYear = profile.year.filter { it.isDigit() }
            val studentSem = profile.semester.filter { it.isDigit() }
            val studentDeptId = profile.departmentId?.trim() ?: ""

            val relevantSessions = sessions.filter { session ->
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
            
            val totalSessions = relevantSessions.size
            // If filtering by course, only show students who have at least one session in that course
            if (courseFilter.isNotEmpty() && totalSessions == 0) return@mapNotNull null
            
            val relevantSessionIds = relevantSessions.map { it.id }.toSet()
            
            // Check for both Firebase UID and Student ID to handle legacy or mixed records
            val studentIds = setOfNotNull(user.id, profile.studentId)
            val presentCount = attendances.count { it.studentId in studentIds && it.sessionId in relevantSessionIds }
            
            val percentage = if (totalSessions > 0) {
                (presentCount.toFloat() / totalSessions.toFloat()) * 100f
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
                totalSessions = totalSessions
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createSchedule(
        courseId: String,
        teacherId: String,
        departmentId: String,
        year: String,
        semester: String,
        dayOfWeek: String,
        startTime: String,
        endTime: String
    ) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val schedule = Schedule(
                    scheduleId = "", // Firestore will generate or we use repo
                    courseId = courseId,
                    teacherId = teacherId,
                    departmentId = departmentId,
                    year = year,
                    semester = semester,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime
                )
                academicRepository.insertSchedule(schedule)
                _uiState.value = AdminUiState.Success("Schedule created successfully")
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Failed to create schedule")
            }
        }
    }

    fun createDepartment(name: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                academicRepository.insertDepartment(Department(id = "", name = name))
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Failed to create department")
            }
        }
    }

    fun createCourse(name: String, departmentId: String, year: String, semester: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                academicRepository.insertCourse(Course(id = "", name = name, departmentId = departmentId, year = year, semester = semester))
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Failed to create course")
            }
        }
    }

    fun approveUser(user: User) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                userRepository.updateUser(user.copy(status = UserStatus.active))
                _uiState.value = AdminUiState.Success("${user.name} approved")
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Failed to approve user")
            }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                userRepository.deleteUser(user.id)
                _uiState.value = AdminUiState.Success("${user.name} deleted")
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Failed to delete user")
            }
        }
    }

    fun updateUserStatus(user: User, status: UserStatus) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                userRepository.updateUser(user.copy(status = status))
                _uiState.value = AdminUiState.Success("Status updated for ${user.name}")
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Failed to update status")
            }
        }
    }

    fun deleteCourse(courseId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                academicRepository.deleteCourse(courseId)
                _uiState.value = AdminUiState.Success("Course deleted")
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Failed to delete course")
            }
        }
    }

    fun deleteDepartment(deptId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                academicRepository.deleteDepartment(deptId)
                _uiState.value = AdminUiState.Success("Department deleted")
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Failed to delete department")
            }
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                academicRepository.deleteSchedule(scheduleId)
                _uiState.value = AdminUiState.Success("Schedule deleted")
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Failed to delete schedule")
            }
        }
    }

    fun getStudentDetails(userId: String): Flow<Student?> {
        return userRepository.getStudentByUserId(userId)
    }

    fun getTeacherDetails(userId: String): Flow<Teacher?> {
        return userRepository.getTeacherByUserId(userId)
    }

    fun updateUserDetails(
        user: User, 
        student: Student? = null,
        teacher: Teacher? = null
    ) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                userRepository.updateUser(user)
                student?.let { userRepository.updateStudent(it) }
                teacher?.let { userRepository.updateTeacher(it) }
                _uiState.value = AdminUiState.Success("User updated successfully")
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error(e.message ?: "Failed to update user")
            }
        }
    }

    fun resetState() {
        _uiState.value = AdminUiState.Idle
    }

    init {
        checkPendingApprovals()
    }

    private fun checkPendingApprovals() {
        viewModelScope.launch {
            try {
                combine(teachers, students) { t, s -> t + s }.collectLatest { allUsers ->
                    val pendingCount = allUsers.count { it.status == com.attendance.attendanceapp.domain.model.UserStatus.pending }
                    val currentAdminId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    
                    if (pendingCount > 0 && currentAdminId.isNotEmpty()) {
                        val notificationId = "PENDING_APPROVALS_${currentAdminId}"
                        val existing = notificationRepository.getNotificationsByUser(currentAdminId).first()
                        val alreadyNotified = existing.any { it.id == notificationId || it.title == "Pending Approvals" }
                        
                        if (!alreadyNotified) {
                            notificationRepository.insertNotification(
                                Notification(
                                    id = notificationId,
                                    userId = currentAdminId,
                                    title = "Pending Approvals",
                                    message = "There are $pendingCount new users waiting for your approval.",
                                    type = "warning",
                                    isRead = false,
                                    createdAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AdminVM", "Error in checkPendingApprovals", e)
            }
        }
    }
}

sealed class AdminUiState {
    object Idle : AdminUiState()
    object Loading : AdminUiState()
    data class Success(val message: String) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}
