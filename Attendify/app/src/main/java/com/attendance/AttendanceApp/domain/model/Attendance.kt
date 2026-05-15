package com.attendance.attendanceapp.domain.model

data class Attendance(
    val id: String,
    val studentId: String,
    val sessionId: String,
    val status: AttendanceStatus,
    val timestamp: Long
)

enum class AttendanceStatus {
    Present, Absent, Permission
}
