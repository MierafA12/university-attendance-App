package com.attendance.attendanceapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val password: String,
    val role: String, // "student", "teacher", "admin"
    val status: String = "pending"
)
