package com.attendance.attendanceapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.attendance.attendanceapp.data.local.entity.TeacherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity): Long

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity)

    @Delete
    suspend fun deleteTeacher(teacher: TeacherEntity)

    @Query("SELECT * FROM teachers WHERE teacherId = :teacherId")
    fun getTeacherById(teacherId: String): Flow<TeacherEntity?>

    @Query("SELECT * FROM teachers WHERE userId = :userId LIMIT 1")
    fun getTeacherByUserId(userId: String): Flow<TeacherEntity?>

    @Query("SELECT * FROM teachers WHERE departmentId = :departmentId")
    fun getTeachersByDepartment(departmentId: String): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Query("DELETE FROM teachers WHERE teacherId = :teacherId")
    suspend fun deleteTeacherById(teacherId: String)
}
