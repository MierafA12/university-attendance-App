package com.attendance.attendanceapp

import android.app.Application
import com.attendance.attendanceapp.data.local.db.AppDatabase
import com.attendance.attendanceapp.data.local.db.FirebaseManager
import com.attendance.attendanceapp.data.repository.AttendanceRepositoryImpl
import com.attendance.attendanceapp.data.repository.UserRepositoryImpl
import com.attendance.attendanceapp.domain.repository.AttendanceRepository
import com.attendance.attendanceapp.domain.repository.UserRepository
import com.google.firebase.FirebaseApp

class App : Application() {

    /** Room database — lazily initialized on first access */
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(database.userDao(), database.studentDao(), database.teacherDao())
    }

    val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepositoryImpl(database.attendanceDao(), database.sessionDao())
    }

    val notificationRepository: com.attendance.attendanceapp.domain.repository.NotificationRepository by lazy {
        com.attendance.attendanceapp.data.repository.NotificationRepositoryImpl(database.notificationDao())
    }

    val academicRepository: com.attendance.attendanceapp.domain.repository.AcademicRepository by lazy {
        com.attendance.attendanceapp.data.repository.AcademicRepositoryImpl(
            database.departmentDao(),
            database.courseDao(),
            database.sectionDao(),
            database.scheduleDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase first
        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            android.util.Log.e("App", "Firebase initialization failed", e)
        }
    }
}
