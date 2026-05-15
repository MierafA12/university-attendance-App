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
        UserRepositoryImpl(database.userDao())
    }

    val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepositoryImpl(database.attendanceDao(), database.sessionDao())
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase (idempotent — safe to call multiple times)
        FirebaseApp.initializeApp(this)

        // Warm up the FirebaseManager lazy instances so Firestore settings
        // (offline persistence) are applied before the first query
        FirebaseManager.firestore
        FirebaseManager.auth
    }
}
