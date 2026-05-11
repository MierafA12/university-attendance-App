package com.attendance.attendanceapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["sessionId", "studentId"], unique = true),
        Index(value = ["studentId"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val studentId: String,
    val status: String, // "Present", "Absent"
    val timestamp: Long
)
