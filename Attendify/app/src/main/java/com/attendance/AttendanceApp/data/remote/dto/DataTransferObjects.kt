package com.attendance.attendanceapp.data.remote.dto

data class UserDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // student, teacher, admin
    val status: String = "pending"
)

data class DepartmentDto(
    val id: String = "",
    val name: String = ""
)

data class CourseDto(
    val id: String = "",
    val name: String = "",
    val departmentId: String = "",
    val year: String = "",
    val semester: String = ""
)

data class SectionDto(
    val sectionId: String = "",
    val courseId: String = "",
    val name: String = "",
    val year: String = "",
    val semester: String = ""
)

data class StudentDto(
    val studentId: String = "",
    val userId: String = "",
    val departmentId: String? = null,
    val year: String = "",
    val semester: String = ""
)

data class TeacherDto(
    val teacherId: String = "",
    val userId: String = "",
    val departmentId: String? = null,
    val specialization: String = ""
)

data class ScheduleDto(
    val scheduleId: String = "",
    val courseId: String = "",
    val teacherId: String = "",
    val departmentId: String = "",
    val year: String = "",
    val semester: String = "",
    val dayOfWeek: String = "",
    val startTime: String = "",
    val endTime: String = ""
)

data class SessionDto(
    val sessionId: String = "",
    val scheduleId: String = "",
    val qrCode: String = "",
    val date: Long = 0L,
    val isActive: Boolean = true,
    val durationMinutes: Int = 15,
    val maxStudents: Int = 0
)

data class AttendanceDto(
    val id: String = "",
    val studentId: String = "",
    val sessionId: String = "",
    val status: String = "", // Present, Absent
    val timestamp: Long = 0L
)

data class NotificationDto(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)
