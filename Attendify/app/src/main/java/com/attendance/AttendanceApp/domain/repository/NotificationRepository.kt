package com.attendance.attendanceapp.domain.repository

import com.attendance.attendanceapp.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotificationsByUser(userId: String, role: String = ""): Flow<List<Notification>>
    fun getUnreadCount(userId: String, role: String = ""): Flow<Int>
    suspend fun insertNotification(notification: Notification)
    suspend fun markAsRead(id: String)
    suspend fun deleteNotification(id: String)
    suspend fun clearAll(userId: String)
}
