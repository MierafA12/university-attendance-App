package com.attendance.attendanceapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a class section — a specific group of students taking a Course.
 * e.g., "Section A", "Section B" of a given course.
 */
@Entity(
    tableName = "course_sections",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["courseId"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["courseId"])]
)
data class SectionEntity(
    @PrimaryKey(autoGenerate = true) val sectionId: Int = 0,
    val courseId: Int,
    val name: String,
    val year: String
)
