package com.attendance.attendanceapp.data.repository

import com.attendance.attendanceapp.data.local.dao.UserDao
import com.attendance.attendanceapp.data.mapper.*
import com.attendance.attendanceapp.domain.model.User
import com.attendance.attendanceapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.attendance.attendanceapp.data.local.db.FirebaseManager
import com.attendance.attendanceapp.domain.model.Role
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepositoryImpl(
    private val userDao: com.attendance.attendanceapp.data.local.dao.UserDao,
    private val studentDao: com.attendance.attendanceapp.data.local.dao.StudentDao,
    private val teacherDao: com.attendance.attendanceapp.data.local.dao.TeacherDao
) : UserRepository {

    private val auth = FirebaseManager.auth
    private val firestore = FirebaseManager.firestore

    override fun getUserById(id: String): Flow<User?> {
        return userDao.getUserById(id).map { it?.toDomain() }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toDomain()
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertUser(user: User): String {
        val id = userDao.insertUser(user.toEntity())
        return id.toString()
    }

    override suspend fun updateUser(user: User) {
        // Update local DB
        userDao.insertUser(user.toEntity()) // insertUser uses REPLACE or similar? Let's check UserDao
        // Update Firestore
        FirebaseManager.usersCollection.document(user.id)
            .set(user.toDto())
            .await()
    }

    override suspend fun deleteUser(id: String) {
        userDao.deleteUserById(id)
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("User not found")
            
            // Fetch user details from Firestore
            val doc = FirebaseManager.usersCollection.document(firebaseUser.uid).get().await()
            val userDto = try {
                doc.toObject(com.attendance.attendanceapp.data.remote.dto.UserDto::class.java)
            } catch (e: Exception) {
                null
            }
            
            if (userDto != null) {
                val user = userDto.toDomain()
                // Cache in local DB
                userDao.insertUser(user.toEntity())
                Result.success(user)
            } else {
                // Fallback or create minimal user if not in Firestore
                val user = User(firebaseUser.uid, firebaseUser.displayName ?: "", firebaseUser.email ?: "", Role.student)
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(user: User, password: String, student: com.attendance.attendanceapp.domain.model.Student?): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(user.email, password).await()
            val firebaseUser = result.user ?: throw Exception("Signup failed")
            
            val finalUser = user.copy(id = firebaseUser.uid)
            
            // Save to Firestore users collection
            FirebaseManager.usersCollection.document(firebaseUser.uid)
                .set(finalUser.toDto())
                .await()
            
            // If student data provided, save to students collection
            student?.let {
                val finalStudent = it.copy(userId = firebaseUser.uid)
                FirebaseManager.studentsCollection.document(firebaseUser.uid)
                    .set(finalStudent.toDto())
                    .await()
            }
            
            // Cache in local DB
            userDao.insertUser(finalUser.toEntity())
            
            Result.success(finalUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        // This is a synchronous call, so we might return a partial user or fetch from local DB
        // For now, let's return null if not cached or implement a better way
        return null 
    }

    override fun getUsersByRole(role: Role): Flow<List<User>> = kotlinx.coroutines.flow.channelFlow {
        // 1. Setup real-time listener from Firestore
        val listenerRegistration = FirebaseManager.usersCollection
            .whereEqualTo("role", role.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        val dtos = snapshot.toObjects(com.attendance.attendanceapp.data.remote.dto.UserDto::class.java)
                        dtos.forEach { dto ->
                            userDao.insertUser(dto.toDomain().toEntity())
                        }
                    } catch (e: Exception) {
                        // Sync failed
                    }
                }
            }

        // 2. Emit from local Room DB
        userDao.getUsersByRole(role.name).collect { entities ->
            send(entities.map { it.toDomain() })
        }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getAllStudentProfiles(): Flow<List<com.attendance.attendanceapp.domain.model.Student>> = kotlinx.coroutines.flow.channelFlow {
        // 1. Setup real-time listener from Firestore
        val listenerRegistration = FirebaseManager.studentsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        val dtos = snapshot.toObjects(com.attendance.attendanceapp.data.remote.dto.StudentDto::class.java)
                        dtos.forEach { dto ->
                            studentDao.insertStudent(dto.toDomain().toEntity())
                        }
                    } catch (e: Exception) {
                        // Sync failed
                    }
                }
            }

        // 2. Emit from local Room DB
        studentDao.getAllStudents().collect { entities ->
            send(entities.map { it.toDomain() })
        }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getStudentByUserId(userId: String): Flow<com.attendance.attendanceapp.domain.model.Student?> = kotlinx.coroutines.flow.channelFlow {
        if (userId.isEmpty()) {
            send(null)
            return@channelFlow
        }
        
        // 1. Launch background sync from Firestore
        launch {
            try {
                val doc = FirebaseManager.studentsCollection.document(userId).get().await()
                val dto = doc.toObject(com.attendance.attendanceapp.data.remote.dto.StudentDto::class.java)
                if (dto != null) {
                    studentDao.insertStudent(dto.toDomain().toEntity())
                }
            } catch (e: Exception) {
                android.util.Log.e("UserRepo", "Student sync failed for $userId", e)
            }
        }

        // 2. Continuously emit from local Room DB
        studentDao.getStudentByUserId(userId).collect { entity ->
            try {
                send(entity?.toDomain())
            } catch (e: Exception) {
                send(null)
            }
        }
    }

    override fun getTeacherByUserId(userId: String): Flow<com.attendance.attendanceapp.domain.model.Teacher?> = kotlinx.coroutines.flow.channelFlow {
        try {
            val doc = FirebaseManager.teachersCollection.document(userId).get().await()
            val dto = try {
                doc.toObject(com.attendance.attendanceapp.data.remote.dto.TeacherDto::class.java)
            } catch (e: Exception) {
                null
            }
            if (dto != null) {
                val teacher = dto.toDomain()
                teacherDao.insertTeacher(teacher.toEntity())
                send(teacher)
            } else {
                teacherDao.getTeacherByUserId(userId).collect { entity ->
                    send(entity?.toDomain())
                }
            }
        } catch (e: Exception) {
            teacherDao.getTeacherByUserId(userId).collect { entity ->
                send(entity?.toDomain())
            }
        }
    }

    override suspend fun updateStudent(student: com.attendance.attendanceapp.domain.model.Student) {
        // Update local DB
        studentDao.insertStudent(student.toEntity())
        // Update Firestore
        FirebaseManager.studentsCollection.document(student.userId)
            .set(student.toDto())
            .await()
    }

    override suspend fun updateTeacher(teacher: com.attendance.attendanceapp.domain.model.Teacher) {
        // Update local DB
        teacherDao.insertTeacher(teacher.toEntity())
        // Update Firestore
        FirebaseManager.teachersCollection.document(teacher.userId)
            .set(teacher.toDto())
            .await()
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            android.util.Log.d("Auth", "Password reset email sent successfully to: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("Auth", "Failed to send password reset email to: $email", e)
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return try {
            auth.currentUser?.updatePassword(newPassword)?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reauthenticate(password: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
