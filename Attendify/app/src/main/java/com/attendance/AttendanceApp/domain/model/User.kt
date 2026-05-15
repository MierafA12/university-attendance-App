package com.attendance.attendanceapp.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: Role,
    val status: UserStatus = UserStatus.pending
)

enum class Role {
    student, teacher, admin
}

enum class UserStatus {
    pending, active, inactive
}
