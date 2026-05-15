package com.attendance.attendanceapp.ui.screens.admin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.attendanceapp.domain.model.Course
import com.attendance.attendanceapp.domain.model.Department
import com.attendance.attendanceapp.domain.model.Schedule
import com.attendance.attendanceapp.domain.model.User
import com.attendance.attendanceapp.domain.model.Role
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

    val allSessions: StateFlow<List<com.attendance.attendanceapp.domain.model.Session>> = attendanceRepository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
                userRepository.updateUser(user.copy(status = com.attendance.attendanceapp.domain.model.UserStatus.active))
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

    fun updateUserStatus(user: User, status: com.attendance.attendanceapp.domain.model.UserStatus) {
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

    fun getStudentDetails(userId: String): Flow<com.attendance.attendanceapp.domain.model.Student?> {
        return userRepository.getStudentByUserId(userId)
    }

    fun getTeacherDetails(userId: String): Flow<com.attendance.attendanceapp.domain.model.Teacher?> {
        return userRepository.getTeacherByUserId(userId)
    }

    fun updateUserDetails(
        user: User, 
        student: com.attendance.attendanceapp.domain.model.Student? = null,
        teacher: com.attendance.attendanceapp.domain.model.Teacher? = null
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
            combine(teachers, students) { t, s -> t + s }.collect { allUsers ->
                val pendingCount = allUsers.count { it.status == com.attendance.attendanceapp.domain.model.UserStatus.pending }
                if (pendingCount > 0) {
                    val currentAdminId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val existing = notificationRepository.getNotificationsByUser(currentAdminId).first()
                    val alreadyNotified = existing.any { it.title == "Pending Approvals" }
                    
                    if (!alreadyNotified) {
                        notificationRepository.insertNotification(
                            com.attendance.attendanceapp.domain.model.Notification(
                                id = "",
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
        }
    }
}

sealed class AdminUiState {
    object Idle : AdminUiState()
    object Loading : AdminUiState()
    data class Success(val message: String) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}
