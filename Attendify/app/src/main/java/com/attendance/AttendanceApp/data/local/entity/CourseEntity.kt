package com.attendance.attendanceapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "courses",
    foreignKeys = [
        ForeignKey(
            entity = DepartmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["departmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["departmentId"])]
)
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val courseId: Int = 0,
    val name: String,
    val departmentId: Int,
    val year: String
)
