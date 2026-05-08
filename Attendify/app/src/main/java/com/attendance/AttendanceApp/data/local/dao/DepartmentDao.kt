package com.attendance.attendanceapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.attendance.attendanceapp.data.local.entity.DepartmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: DepartmentEntity): Long

    @Update
    suspend fun updateDepartment(department: DepartmentEntity)

    @Delete
    suspend fun deleteDepartment(department: DepartmentEntity)

    @Query("SELECT * FROM departments")
    fun getAllDepartments(): Flow<List<DepartmentEntity>>

    @Query("SELECT * FROM departments WHERE id = :id")
    fun getDepartmentById(id: Int): Flow<DepartmentEntity?>

    @Query("DELETE FROM departments WHERE id = :id")
    suspend fun deleteDepartmentById(id: Int)
}
