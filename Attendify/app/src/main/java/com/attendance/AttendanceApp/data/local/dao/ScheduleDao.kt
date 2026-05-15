package com.attendance.attendanceapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.attendance.attendanceapp.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    @Query("SELECT * FROM schedules WHERE scheduleId = :scheduleId")
    fun getScheduleById(scheduleId: Int): Flow<ScheduleEntity?>

    @Query("SELECT * FROM schedules WHERE courseId = :courseId")
    fun getSchedulesByCourse(courseId: Int): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE teacherId = :teacherId")
    fun getSchedulesByTeacher(teacherId: Int): Flow<List<ScheduleEntity>>

    @Query("DELETE FROM schedules WHERE scheduleId = :scheduleId")
    suspend fun deleteScheduleById(scheduleId: Int)
}
