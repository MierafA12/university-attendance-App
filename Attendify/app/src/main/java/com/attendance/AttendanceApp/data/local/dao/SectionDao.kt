package com.attendance.attendanceapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.attendance.attendanceapp.data.local.entity.SectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionEntity): Long

    @Update
    suspend fun updateSection(section: SectionEntity)

    @Delete
    suspend fun deleteSection(section: SectionEntity)

    @Query("SELECT * FROM course_sections WHERE courseId = :courseId")
    fun getSectionsByCourse(courseId: String): Flow<List<SectionEntity>>

    @Query("DELETE FROM course_sections WHERE sectionId = :sectionId")
    suspend fun deleteSectionById(sectionId: String)
}
