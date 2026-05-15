package com.attendance.attendanceapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_sessions",
    indices = [Index(value = ["scheduleId"])]
)
data class SessionEntity(
    @PrimaryKey val sessionId: String,
    val scheduleId: String,
    val qrCode: String,
    val date: Long, // timestamp
    val isActive: Boolean = true,
    val durationMinutes: Int = 15,
    val maxStudents: Int = 0
)
