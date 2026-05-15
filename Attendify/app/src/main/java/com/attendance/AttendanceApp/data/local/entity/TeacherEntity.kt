package com.attendance.attendanceapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a teacher profile linked to a User account.
 * A teacher optionally belongs to a Department.
 */
@Entity(
    tableName = "teachers",
    indices = [
        Index(value = ["userId"], unique = true),
        Index(value = ["departmentId"])
    ]
)
data class TeacherEntity(
    @PrimaryKey val teacherId: String,
    val userId: String,
    val departmentId: String?,
    val specialization: String
)
