package com.attendance.attendanceapp.domain.repository

import com.attendance.attendanceapp.domain.model.Course
import com.attendance.attendanceapp.domain.model.Department
import com.attendance.attendanceapp.domain.model.Schedule
import com.attendance.attendanceapp.domain.model.Section
import kotlinx.coroutines.flow.Flow

interface AcademicRepository {
    
    // Departments
    fun getAllDepartments(): Flow<List<Department>>
    fun getDepartmentById(id: String): Flow<Department?>
    suspend fun insertDepartment(department: Department): String
    suspend fun deleteDepartment(id: String)
    
    // Courses
    fun getAllCourses(): Flow<List<Course>>
    fun getCoursesByDepartment(departmentId: String): Flow<List<Course>>
    fun getCourseById(id: String): Flow<Course?>
    suspend fun insertCourse(course: Course): String
    suspend fun deleteCourse(id: String)
    
    // Sections
    fun getSectionsByCourse(courseId: String): Flow<List<Section>>
    suspend fun insertSection(section: Section): String
    suspend fun deleteSection(id: String)
    
    // Schedules
    fun getAllSchedules(): Flow<List<Schedule>>
    fun getSchedulesByCourse(courseId: String): Flow<List<Schedule>>
    fun getSchedulesByTeacher(teacherId: String): Flow<List<Schedule>>
    fun getScheduleById(id: String): Flow<Schedule?>
    suspend fun insertSchedule(schedule: Schedule): String
    suspend fun deleteSchedule(id: String)
}
