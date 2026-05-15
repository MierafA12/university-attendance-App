package com.attendance.attendanceapp.domain.repository

import com.attendance.attendanceapp.domain.model.Attendance
import com.attendance.attendanceapp.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    
    // Sessions
    fun getAllSessions(): Flow<List<Session>>
    fun getSessionById(id: String): Flow<Session?>
    suspend fun insertSession(session: Session): String
    suspend fun deleteSession(id: String)
    
    // Attendance Records
    fun getAllAttendance(): Flow<List<Attendance>>
    fun getAttendanceBySession(sessionId: String): Flow<List<Attendance>>
    fun getAttendanceByStudent(studentId: String): Flow<List<Attendance>>
    suspend fun markAttendance(attendance: Attendance)
    suspend fun deleteAttendance(id: String)
    
    // Sync (if needed later)
    suspend fun deactivateAllSessions()
    suspend fun syncWithBackend()
    suspend fun findSessionByQrCode(qrCode: String): Session?
}
