package com.attendance.attendanceapp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.attendance.attendanceapp.data.local.dao.AttendanceDao
import com.attendance.attendanceapp.data.local.dao.CourseDao
import com.attendance.attendanceapp.data.local.dao.DepartmentDao
import com.attendance.attendanceapp.data.local.dao.ScheduleDao
import com.attendance.attendanceapp.data.local.dao.SectionDao
import com.attendance.attendanceapp.data.local.dao.NotificationDao
import com.attendance.attendanceapp.data.local.dao.SessionDao
import com.attendance.attendanceapp.data.local.dao.StudentDao
import com.attendance.attendanceapp.data.local.dao.TeacherDao
import com.attendance.attendanceapp.data.local.dao.UserDao
import com.attendance.attendanceapp.data.local.entity.AttendanceEntity
import com.attendance.attendanceapp.data.local.entity.CourseEntity
import com.attendance.attendanceapp.data.local.entity.DepartmentEntity
import com.attendance.attendanceapp.data.local.entity.NotificationEntity
import com.attendance.attendanceapp.data.local.entity.ScheduleEntity
import com.attendance.attendanceapp.data.local.entity.SectionEntity
import com.attendance.attendanceapp.data.local.entity.SessionEntity
import com.attendance.attendanceapp.data.local.entity.StudentEntity
import com.attendance.attendanceapp.data.local.entity.TeacherEntity
import com.attendance.attendanceapp.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        DepartmentEntity::class,
        CourseEntity::class,
        SectionEntity::class,
        StudentEntity::class,
        TeacherEntity::class,
        ScheduleEntity::class,
        SessionEntity::class,
        AttendanceEntity::class,
        NotificationEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun departmentDao(): DepartmentDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun sectionDao(): SectionDao
    abstract fun studentDao(): StudentDao
    abstract fun teacherDao(): TeacherDao
    abstract fun sessionDao(): SessionDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        private const val DATABASE_NAME = "attendify.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade() // Handle version downgrades
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
