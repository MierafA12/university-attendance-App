package com.attendance.attendanceapp.data.repository

import com.attendance.attendanceapp.data.local.dao.AttendanceDao
import com.attendance.attendanceapp.data.local.dao.SessionDao
import com.attendance.attendanceapp.data.local.db.FirebaseManager
import com.attendance.attendanceapp.data.mapper.toDomain
import com.attendance.attendanceapp.data.mapper.toEntity
import com.attendance.attendanceapp.data.mapper.toDto
import com.attendance.attendanceapp.data.remote.dto.SessionDto
import com.attendance.attendanceapp.data.remote.dto.AttendanceDto
import com.attendance.attendanceapp.domain.model.Attendance
import com.attendance.attendanceapp.domain.model.Session
import com.attendance.attendanceapp.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose

class AttendanceRepositoryImpl(
    private val attendanceDao: AttendanceDao,
    private val sessionDao: SessionDao
) : AttendanceRepository {

    private val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        android.util.Log.e("AttendanceRepo", "Background coroutine failed", throwable)
    }
    
    private val repositoryScope = CoroutineScope(Dispatchers.IO + exceptionHandler)

    override fun getAllSessions(): Flow<List<Session>> = channelFlow {
        // 1. Listen for sessions from Firestore in REAL-TIME
        val listenerRegistration = FirebaseManager.sessionsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                repositoryScope.launch {
                    try {
                        val sessions = snapshot.toObjects(SessionDto::class.java)
                        // Batch update Room to avoid flickering
                        sessions.forEach { dto ->
                            sessionDao.insertSession(dto.toDomain().toEntity())
                        }
                    } catch (e: Exception) {
                        // Log or handle deserialization errors
                    }
                }
            }

        // 2. Emit from local Room DB whenever it changes
        sessionDao.getAllSessions().collect { entities ->
            send(entities.map { it.toDomain() })
        }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getSessionById(id: String): Flow<Session?> = channelFlow {
        val listenerRegistration = FirebaseManager.sessionsCollection.document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                try {
                    snapshot?.toObject(SessionDto::class.java)?.let { dto ->
                        repositoryScope.launch {
                            sessionDao.insertSession(dto.toDomain().toEntity())
                        }
                    }
                } catch (e: Exception) {
                    // Log or handle
                }
            }

        sessionDao.getSessionById(id).collect { entity ->
            send(entity?.toDomain())
        }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun insertSession(session: Session): String {
        val id = if (session.id.isEmpty()) java.util.UUID.randomUUID().toString() else session.id
        val finalSession = session.copy(id = id)
        
        sessionDao.insertSession(finalSession.toEntity())
        
        try {
            FirebaseManager.sessionsCollection.document(id)
                .set(finalSession.toDto())
                .await()
        } catch (e: Exception) {}
        
        return id
    }

    override suspend fun deleteSession(id: String) {
        sessionDao.deleteSessionById(id)
        try {
            FirebaseManager.sessionsCollection.document(id).delete().await()
        } catch (e: Exception) {}
    }

    override fun getAllAttendance(): Flow<List<Attendance>> = channelFlow {
        val listenerRegistration = FirebaseManager.attendanceCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                repositoryScope.launch {
                    try {
                        val dtos = snapshot.toObjects(AttendanceDto::class.java)
                        dtos.forEach { dto ->
                            attendanceDao.insertAttendance(dto.toDomain().toEntity())
                        }
                    } catch (e: Exception) {}
                }
            }

        attendanceDao.getAllAttendanceRecords().collect { entities ->
            send(entities.map { it.toDomain() })
        }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getAttendanceBySession(sessionId: String): Flow<List<Attendance>> = channelFlow {
        val listenerRegistration = FirebaseManager.attendanceCollection
            .whereEqualTo("sessionId", sessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                
                repositoryScope.launch {
                    try {
                        val dtos = snapshot.toObjects(AttendanceDto::class.java)
                        dtos.forEach { dto ->
                            attendanceDao.insertAttendance(dto.toDomain().toEntity())
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AttendanceRepo", "Attendance sync failed", e)
                    }
                }
            }

        attendanceDao.getAttendanceBySession(sessionId).collect { entities ->
            send(entities.map { it.toDomain() })
        }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getAttendanceByStudent(studentId: String): Flow<List<Attendance>> = channelFlow {
        val listenerRegistration = FirebaseManager.attendanceCollection
            .whereEqualTo("studentId", studentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                try {
                    snapshot?.toObjects(AttendanceDto::class.java)?.forEach { dto ->
                        repositoryScope.launch {
                            attendanceDao.insertAttendance(dto.toDomain().toEntity())
                        }
                    }
                } catch (e: Exception) {
                    // Log or handle
                }
            }

        attendanceDao.getAttendanceByStudent(studentId).collect { entities ->
            send(entities.map { it.toDomain() })
        }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun markAttendance(attendance: Attendance) {
        attendanceDao.insertAttendance(attendance.toEntity())
        try {
            FirebaseManager.attendanceCollection.document(attendance.id)
                .set(attendance.toDto())
                .await()
        } catch (e: Exception) {}
    }

    override suspend fun deleteAttendance(id: String) {
        attendanceDao.deleteAttendanceById(id)
        try {
            FirebaseManager.attendanceCollection.document(id).delete().await()
        } catch (e: Exception) {}
    }

    override suspend fun deactivateAllSessions() {
        sessionDao.deactivateAllSessions()
        try {
            val activeSessions = FirebaseManager.sessionsCollection.whereEqualTo("isActive", true).get().await()
            activeSessions.documents.forEach { doc ->
                doc.reference.update("isActive", false).await()
            }
        } catch (e: Exception) {}
    }

    override suspend fun syncWithBackend() {
        // Real-time listeners handle sync
    }

    override suspend fun findSessionByQrCode(qrCode: String): Session? {
        // 1. Check Local Room DB first (fastest)
        val localSessions = sessionDao.getAllSessions().first()
        val localMatch = localSessions.find { it.qrCode == qrCode && it.isActive }
        if (localMatch != null) return localMatch.toDomain()
        
        // 2. If not found locally, check Firestore directly (fallback)
        return try {
            val snapshot = FirebaseManager.sessionsCollection
                .whereEqualTo("qrCode", qrCode)
                .whereEqualTo("isActive", true)
                .get().await()
            
            val dto = try { 
                snapshot.toObjects(SessionDto::class.java).firstOrNull()
            } catch (e: Exception) {
                null
            }
            dto?.toDomain()?.also { session ->
                // Cache it locally for next time
                sessionDao.insertSession(session.toEntity())
            }
        } catch (e: Exception) {
            null
        }
    }
}
