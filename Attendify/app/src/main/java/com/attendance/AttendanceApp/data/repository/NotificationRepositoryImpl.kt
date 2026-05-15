package com.attendance.attendanceapp.data.repository

import com.attendance.attendanceapp.data.local.dao.NotificationDao
import com.attendance.attendanceapp.data.mapper.toDomain
import com.attendance.attendanceapp.data.mapper.toEntity
import com.attendance.attendanceapp.data.mapper.toDto
import com.attendance.attendanceapp.domain.model.Notification
import com.attendance.attendanceapp.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationRepositoryImpl(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    
    override fun getNotificationsByUser(userId: String, role: String): Flow<List<Notification>> = channelFlow {
        val userIds = listOfNotNull(userId, if (role.isNotBlank()) "ROLE_$role" else null)
        
        // 1. Setup real-time listener from Firestore (only if we have valid IDs)
        val validUserIds = userIds.filter { it.isNotBlank() }
        val listenerRegistration = if (validUserIds.isNotEmpty()) {
            com.attendance.attendanceapp.data.local.db.FirebaseManager.notificationsCollection
                .whereIn("userId", validUserIds)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    
                    repositoryScope.launch {
                        try {
                            val dtos = snapshot.toObjects(com.attendance.attendanceapp.data.remote.dto.NotificationDto::class.java)
                            dtos.forEach { dto ->
                                notificationDao.insertNotification(dto.toDomain().toEntity())
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("NotificationRepo", "Failed to sync notifications", e)
                        }
                    }
                }
        } else null

        // Emit from local DB
        notificationDao.getNotificationsByUserIds(userIds).collect { entities ->
            send(entities.map { it.toDomain() })
        }
        
        awaitClose { listenerRegistration?.remove() }
    }

    override fun getUnreadCount(userId: String, role: String): Flow<Int> {
        val userIds = listOfNotNull(userId, if (role.isNotBlank()) "ROLE_$role" else null)
        return notificationDao.getUnreadCountByUserIds(userIds)
    }

    override suspend fun insertNotification(notification: Notification) {
        val id = if (notification.id.isEmpty()) java.util.UUID.randomUUID().toString() else notification.id
        val finalNotification = notification.copy(id = id)
        
        notificationDao.insertNotification(finalNotification.toEntity())
        
        try {
            com.attendance.attendanceapp.data.local.db.FirebaseManager.notificationsCollection.document(id)
                .set(finalNotification.toDto())
                .await()
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepo", "Failed to upload notification", e)
        }
    }

    override suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
        try {
            com.attendance.attendanceapp.data.local.db.FirebaseManager.notificationsCollection.document(id)
                .update("isRead", true).await()
        } catch (e: Exception) { }
    }

    override suspend fun deleteNotification(id: String) {
        notificationDao.deleteNotificationById(id)
        try {
            com.attendance.attendanceapp.data.local.db.FirebaseManager.notificationsCollection.document(id)
                .delete().await()
        } catch (e: Exception) { }
    }

    override suspend fun clearAll(userId: String) {
        notificationDao.deleteAllNotifications(userId)
        // Note: For a true clearAll, we would need to delete all matching docs in Firestore too,
        // but for safety we'll just let it be a local clear for now.
    }
}
