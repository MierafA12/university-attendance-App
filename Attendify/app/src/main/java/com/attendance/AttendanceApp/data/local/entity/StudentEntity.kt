package com.attendance.attendanceapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a student profile linked to a User account.
 * A student belongs to a Department and has a year of study.
 */
@Entity(
    tableName = "students",
    indices = [
        Index(value = ["userId"], unique = true),
        Index(value = ["departmentId"])
    ]
)
data class StudentEntity(
    @PrimaryKey val studentId: String,
    val userId: String,
    val departmentId: String?,
    val year: String,
    val semester: String
)
