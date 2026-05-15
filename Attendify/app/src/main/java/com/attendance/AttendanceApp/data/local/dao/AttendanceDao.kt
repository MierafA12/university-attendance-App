package com.attendance.attendanceapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.attendance.attendanceapp.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) 
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance_records WHERE id = :id")
    fun getAttendanceById(id: String): Flow<AttendanceEntity?>

    @Query("SELECT * FROM attendance_records")
    fun getAllAttendanceRecords(): Flow<List<AttendanceEntity>>

    /** All attendance records for a specific session */
    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    fun getAttendanceBySession(sessionId: String): Flow<List<AttendanceEntity>>

    /** All attendance records for a specific student across all sessions */
    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getAttendanceByStudent(studentId: String): Flow<List<AttendanceEntity>>

    /** Check if a student already has an attendance record for a given session */
    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId AND studentId = :studentId LIMIT 1")
    suspend fun getAttendanceRecord(sessionId: String, studentId: String): AttendanceEntity?

    /** Count of a specific status for a student (e.g., "PRESENT", "ABSENT", "LATE") */
    @Query("""
        SELECT COUNT(*) FROM attendance_records 
        WHERE studentId = :studentId AND status = :status
    """)
    fun countAttendanceByStatus(studentId: String, status: String): Flow<Int>

    /** All records for a student within a schedule (join through sessions) */
    @Query("""
        SELECT ar.* FROM attendance_records ar
        INNER JOIN attendance_sessions s ON ar.sessionId = s.sessionId
        WHERE ar.studentId = :studentId AND s.scheduleId = :scheduleId
        ORDER BY s.date DESC
    """)
    fun getAttendanceForStudentInSchedule(studentId: String, scheduleId: String): Flow<List<AttendanceEntity>>

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteAttendanceById(id: String)
}
