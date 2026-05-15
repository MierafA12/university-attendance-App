package com.attendance.attendanceapp.domain.model

data class Course(
    val id: String,
    val name: String,
    val departmentId: String,
    val year: String,
    val semester: String
)
