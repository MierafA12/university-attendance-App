package com.attendance.attendanceapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey val scheduleId: String,
    val courseId: String,
    val teacherId: String,
    val departmentId: String,
    val year: String,
    val semester: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String
)
