package com.attendance.attendanceapp.domain.model

data class Department(
    val id: String,
    val name: String
)

data class Section(
    val id: String,
    val courseId: String,
    val name: String,
    val year: String,
    val semester: String
)

data class Student(
    val studentId: String,
    val userId: String,
    val departmentId: String?,
    val year: String,
    val semester: String
)

data class Teacher(
    val teacherId: String,
    val userId: String,
    val departmentId: String?,
    val specialization: String
)

data class Schedule(
    val scheduleId: String,
    val courseId: String,
    val teacherId: String,
    val departmentId: String,
    val year: String,
    val semester: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String
)

data class Notification(
    val id: String,
    val userId: String,
    val message: String,
    val title: String,
    val type: String, // warning/info
    val isRead: Boolean,
    val createdAt: Long
)

data class CourseAttendanceReport(
    val courseId: String,
    val courseName: String,
    val presentCount: Int,
    val totalSessions: Int,
    val percentage: Float,
    val absences: Int
)

data class StudentReportItem(
    val studentId: String,
    val name: String,
    val departmentId: String,
    val semester: String,
    val year: String,
    val attendancePercentage: Float,
    val presentCount: Int,
    val totalSessions: Int
)

data class SessionStudentReport(
    val studentId: String,
    val name: String,
    val status: AttendanceStatus,
    val time: String = "--:--"
)
