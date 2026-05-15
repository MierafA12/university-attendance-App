package com.attendance.attendanceapp.ui.screens.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.attendanceapp.domain.model.Notification
import com.attendance.attendanceapp.domain.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepository: NotificationRepository,
    private val userRepository: com.attendance.attendanceapp.domain.repository.UserRepository
) : ViewModel() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Fetch the current user to get their role
    private val currentUserRole = userRepository.getUserById(currentUserId)
        .map { it?.role?.name ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val notifications: StateFlow<List<Notification>> = currentUserRole.flatMapLatest { role ->
        notificationRepository.getNotificationsByUser(currentUserId, role)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = currentUserRole.flatMapLatest { role ->
        notificationRepository.getUnreadCount(currentUserId, role)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(id)
        }
    }

    fun addNotification(title: String, message: String, type: String = "info", targetUserId: String = currentUserId) {
        viewModelScope.launch {
            val notification = Notification(
                id = "",
                userId = targetUserId,
                title = title,
                message = message,
                type = type,
                isRead = false,
                createdAt = System.currentTimeMillis()
            )
            notificationRepository.insertNotification(notification)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            notificationRepository.clearAll(currentUserId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notifications.value.forEach { notification ->
                if (!notification.isRead) {
                    notificationRepository.markAsRead(notification.id)
                }
            }
        }
    }
}
