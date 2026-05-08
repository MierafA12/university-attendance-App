package com.attendance.attendanceapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.attendance.attendanceapp.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("SELECT * FROM students WHERE studentId = :studentId")
    fun getStudentById(studentId: Int): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE userId = :userId LIMIT 1")
    fun getStudentByUserId(userId: Int): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE departmentId = :departmentId")
    fun getStudentsByDepartment(departmentId: Int): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE year = :year")
    fun getStudentsByYear(year: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("DELETE FROM students WHERE studentId = :studentId")
    suspend fun deleteStudentById(studentId: Int)
}
