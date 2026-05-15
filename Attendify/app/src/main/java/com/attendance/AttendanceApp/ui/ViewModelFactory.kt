package com.attendance.attendanceapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.attendance.attendanceapp.domain.repository.UserRepository
import com.attendance.attendanceapp.domain.repository.AcademicRepository
import com.attendance.attendanceapp.domain.repository.AttendanceRepository
import com.attendance.attendanceapp.ui.screens.auth.AuthViewModel
import com.attendance.attendanceapp.ui.screens.admin.AdminViewModel
import com.attendance.attendanceapp.ui.screens.teacher.TeacherViewModel
import com.attendance.attendanceapp.ui.screens.student.StudentViewModel
import com.attendance.attendanceapp.ui.screens.common.ProfileViewModel
import com.attendance.attendanceapp.ui.screens.common.NotificationViewModel

class ViewModelFactory(
    private val userRepository: UserRepository,
    private val academicRepository: AcademicRepository? = null,
    private val attendanceRepository: AttendanceRepository? = null,
    private val notificationRepository: com.attendance.attendanceapp.domain.repository.NotificationRepository? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(userRepository, academicRepository!!) as T
            }
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> {
                AdminViewModel(academicRepository!!, userRepository, attendanceRepository!!, notificationRepository!!) as T
            }
            modelClass.isAssignableFrom(TeacherViewModel::class.java) -> {
                TeacherViewModel(academicRepository!!, userRepository, attendanceRepository!!, notificationRepository!!) as T
            }
            modelClass.isAssignableFrom(StudentViewModel::class.java) -> {
                StudentViewModel(academicRepository!!, userRepository, attendanceRepository!!, notificationRepository!!) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                NotificationViewModel(notificationRepository!!, userRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
