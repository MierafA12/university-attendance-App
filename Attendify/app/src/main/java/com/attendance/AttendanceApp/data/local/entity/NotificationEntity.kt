package com.attendance.attendanceapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["userId"])]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // "warning", "info"
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
