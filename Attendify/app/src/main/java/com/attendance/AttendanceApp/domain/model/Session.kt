package com.attendance.attendanceapp.domain.model

data class Session(
    val id: String,
    val scheduleId: String,
    val qrCode: String,
    val date: Long,
    val isActive: Boolean = true,
    val durationMinutes: Int = 15,
    val maxStudents: Int = 0 // 0 means no limit
)
