package com.attendance.attendanceapp.domain.repository

import com.attendance.attendanceapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserById(id: String): Flow<User?>
    suspend fun getUserByEmail(email: String): User?
    fun getAllUsers(): Flow<List<User>>
    suspend fun insertUser(user: User): String
    suspend fun deleteUser(id: String)
}
