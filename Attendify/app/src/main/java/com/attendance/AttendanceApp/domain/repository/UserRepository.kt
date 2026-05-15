package com.attendance.attendanceapp.domain.repository

import com.attendance.attendanceapp.domain.model.User
import com.attendance.attendanceapp.domain.model.Student
import com.attendance.attendanceapp.domain.model.Role
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserById(id: String): Flow<User?>
    suspend fun getUserByEmail(email: String): User?
    fun getAllUsers(): Flow<List<User>>
    suspend fun insertUser(user: User): String
    suspend fun updateUser(user: User)
    suspend fun deleteUser(id: String)
    fun getAllStudentProfiles(): Flow<List<Student>>
    fun getStudentByUserId(userId: String): Flow<Student?>
    fun getTeacherByUserId(userId: String): Flow<com.attendance.attendanceapp.domain.model.Teacher?>
    suspend fun updateStudent(student: com.attendance.attendanceapp.domain.model.Student)
    suspend fun updateTeacher(teacher: com.attendance.attendanceapp.domain.model.Teacher)

    // Auth methods
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(user: User, password: String, student: Student? = null): Result<User>
    suspend fun signOut()
    fun getCurrentUser(): User?
    fun getUsersByRole(role: Role): Flow<List<User>>
    
    // Password management
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun updatePassword(newPassword: String): Result<Unit>
    suspend fun reauthenticate(password: String): Result<Unit>
}
