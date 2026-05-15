package com.attendance.attendanceapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "courses",
    indices = [Index(value = ["departmentId"])]
)
data class CourseEntity(
    @PrimaryKey val courseId: String,
    val name: String,
    val departmentId: String,
    val year: String,
    val semester: String
)
