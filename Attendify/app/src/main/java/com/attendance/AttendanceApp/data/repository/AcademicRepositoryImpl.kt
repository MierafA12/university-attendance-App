package com.attendance.attendanceapp.data.repository

import com.attendance.attendanceapp.data.local.dao.CourseDao
import com.attendance.attendanceapp.data.local.dao.DepartmentDao
import com.attendance.attendanceapp.data.local.dao.ScheduleDao
import com.attendance.attendanceapp.data.local.dao.SectionDao
import com.attendance.attendanceapp.data.mapper.*
import com.attendance.attendanceapp.domain.model.Course
import com.attendance.attendanceapp.domain.model.Department
import com.attendance.attendanceapp.domain.model.Schedule
import com.attendance.attendanceapp.domain.model.Section
import com.attendance.attendanceapp.domain.repository.AcademicRepository
import com.attendance.attendanceapp.data.local.db.FirebaseManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

class AcademicRepositoryImpl(
    private val departmentDao: DepartmentDao,
    private val courseDao: CourseDao,
    private val sectionDao: SectionDao,
    private val scheduleDao: ScheduleDao
) : AcademicRepository {

    override fun getAllDepartments(): Flow<List<Department>> = channelFlow {
        val handler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
            android.util.Log.e("AcademicRepo", "Sync failed for departments", throwable)
        }
        
        // 1. Launch background sync
        launch(handler) {
            try {
                val snapshot = FirebaseManager.departmentsCollection.get().await()
                val remoteDepts = snapshot.toObjects(com.attendance.attendanceapp.data.remote.dto.DepartmentDto::class.java)
                remoteDepts.forEach { dto ->
                    try {
                        departmentDao.insertDepartment(dto.toDomain().toEntity())
                    } catch (e: Exception) {
                        // Single insert failed
                    }
                }
            } catch (e: Exception) {
                // Bulk sync failed
            }
        }

        // 2. Emit local data (this runs indefinitely)
        departmentDao.getAllDepartments().collect { entities ->
            send(entities.map { it.toDomain() })
        }
    }

    override fun getDepartmentById(id: String): Flow<Department?> {
        return departmentDao.getDepartmentById(id).map { it?.toDomain() }
    }

    override suspend fun insertDepartment(department: Department): String {
        val docRef = FirebaseManager.departmentsCollection.document()
        val finalDept = department.copy(id = docRef.id)
        
        com.google.android.gms.tasks.Tasks.await(
            docRef.set(finalDept.toDto())
        )
        
        departmentDao.insertDepartment(finalDept.toEntity())
        return finalDept.id
    }

    override suspend fun deleteDepartment(id: String) {
        departmentDao.deleteDepartmentById(id)
        try {
            FirebaseManager.departmentsCollection.document(id).delete().await()
        } catch (e: Exception) {
            android.util.Log.e("AcademicRepo", "Failed to delete department from Firestore: $id")
        }
    }

    override fun getAllCourses(): Flow<List<Course>> = channelFlow {
        val handler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
            android.util.Log.e("AcademicRepo", "Sync failed for courses", throwable)
        }
        launch(handler) {
            try {
                val snapshot = FirebaseManager.coursesCollection.get().await()
                val remoteCourses = snapshot.toObjects(com.attendance.attendanceapp.data.remote.dto.CourseDto::class.java)
                remoteCourses.forEach { dto ->
                    try {
                        courseDao.insertCourse(dto.toDomain().toEntity())
                    } catch (e: Exception) { }
                }
            } catch (e: Exception) { }
        }

        courseDao.getAllCourses().collect { entities ->
            send(entities.map { it.toDomain() })
        }
    }

    override fun getCoursesByDepartment(departmentId: String): Flow<List<Course>> {
        return courseDao.getCoursesByDepartment(departmentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCourseById(id: String): Flow<Course?> {
        return courseDao.getCourseById(id).map { it?.toDomain() }
    }

    override suspend fun insertCourse(course: Course): String {
        val docRef = FirebaseManager.coursesCollection.document()
        val finalCourse = course.copy(id = docRef.id)
        
        com.google.android.gms.tasks.Tasks.await(
            docRef.set(finalCourse.toDto())
        )
        
        courseDao.insertCourse(finalCourse.toEntity())
        return finalCourse.id
    }

    override suspend fun deleteCourse(id: String) {
        courseDao.deleteCourseById(id)
        try {
            FirebaseManager.coursesCollection.document(id).delete().await()
        } catch (e: Exception) {
            android.util.Log.e("AcademicRepo", "Failed to delete course from Firestore: $id")
        }
    }

    override fun getSectionsByCourse(courseId: String): Flow<List<Section>> {
        return sectionDao.getSectionsByCourse(courseId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertSection(section: Section): String {
        return sectionDao.insertSection(section.toEntity()).toString()
    }

    override suspend fun deleteSection(id: String) {
        sectionDao.deleteSectionById(id)
    }

    override fun getAllSchedules(): Flow<List<Schedule>> = channelFlow {
        val handler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
            android.util.Log.e("AcademicRepo", "Sync failed for all schedules", throwable)
        }
        launch(handler) {
            try {
                val snapshot = FirebaseManager.schedulesCollection.get().await()
                val remoteSchedules = snapshot.toObjects(com.attendance.attendanceapp.data.remote.dto.ScheduleDto::class.java)
                remoteSchedules.forEach { dto ->
                    try {
                        scheduleDao.insertSchedule(dto.toDomain().toEntity())
                    } catch (e: Exception) { }
                }
            } catch (e: Exception) { }
        }

        scheduleDao.getAllSchedules().collect { entities ->
            send(entities.map { it.toDomain() })
        }
    }

    override fun getSchedulesByCourse(courseId: String): Flow<List<Schedule>> {
        return scheduleDao.getSchedulesByCourse(courseId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSchedulesByTeacher(teacherId: String): Flow<List<Schedule>> = channelFlow {
        launch {
            try {
                try {
                    val snapshot = FirebaseManager.schedulesCollection.whereEqualTo("teacherId", teacherId).get().await()
                    val remoteSchedules = snapshot.toObjects(com.attendance.attendanceapp.data.remote.dto.ScheduleDto::class.java)
                    remoteSchedules.forEach { dto ->
                        scheduleDao.insertSchedule(dto.toDomain().toEntity())
                    }
                } catch (e: Exception) {
                    // Sync failed
                }
            } catch (e: Exception) {
                // Sync failed
            }
        }

        scheduleDao.getSchedulesByTeacher(teacherId).collect { entities ->
            send(entities.map { it.toDomain() })
        }
    }

    override fun getScheduleById(id: String): Flow<Schedule?> {
        return scheduleDao.getScheduleById(id).map { it?.toDomain() }
    }

    override suspend fun insertSchedule(schedule: Schedule): String {
        // Sync to Firestore
        val docRef = com.attendance.attendanceapp.data.local.db.FirebaseManager.schedulesCollection.document()
        val finalSchedule = schedule.copy(scheduleId = docRef.id)
        
        com.google.android.gms.tasks.Tasks.await(
            docRef.set(finalSchedule.toDto())
        )
        
        // Save to local Room
        return scheduleDao.insertSchedule(finalSchedule.toEntity()).toString()
    }

    override suspend fun deleteSchedule(id: String) {
        scheduleDao.deleteScheduleById(id)
        try {
            FirebaseManager.schedulesCollection.document(id).delete().await()
        } catch (e: Exception) {
            android.util.Log.e("AcademicRepo", "Failed to delete schedule from Firestore: $id")
        }
    }
}
