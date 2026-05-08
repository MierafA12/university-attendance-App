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
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DepartmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["departmentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["userId"], unique = true),
        Index(value = ["departmentId"])
    ]
)
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true) val teacherId: Int = 0,
    val userId: Int,
    val departmentId: Int?,
    val specialization: String
)
