package com.attendance.attendanceapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["courseId"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TeacherEntity::class,
            parentColumns = ["teacherId"],
            childColumns = ["teacherId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DepartmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["departmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["courseId"]),
        Index(value = ["teacherId"]),
        Index(value = ["departmentId"])
    ]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val scheduleId: Int = 0,
    val courseId: Int,
    val teacherId: Int,
    val departmentId: Int,
    val year: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String
)
