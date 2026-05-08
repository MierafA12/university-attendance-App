package com.attendance.attendanceapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.attendance.attendanceapp.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("SELECT * FROM attendance_sessions WHERE sessionId = :sessionId")
    fun getSessionById(sessionId: Int): Flow<SessionEntity?>

    @Query("SELECT * FROM attendance_sessions WHERE scheduleId = :scheduleId")
    fun getSessionsBySchedule(scheduleId: Int): Flow<List<SessionEntity>>

    @Query("SELECT * FROM attendance_sessions WHERE isActive = 1 AND scheduleId = :scheduleId LIMIT 1")
    fun getActiveSessionForSchedule(scheduleId: Int): Flow<SessionEntity?>

    @Query("SELECT * FROM attendance_sessions WHERE qrCode = :qrCode LIMIT 1")
    suspend fun getSessionByQrCode(qrCode: String): SessionEntity?

    @Query("UPDATE attendance_sessions SET isActive = 0 WHERE sessionId = :sessionId")
    suspend fun deactivateSession(sessionId: Int)

    @Query("SELECT * FROM attendance_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("DELETE FROM attendance_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSessionById(sessionId: Int)
}
