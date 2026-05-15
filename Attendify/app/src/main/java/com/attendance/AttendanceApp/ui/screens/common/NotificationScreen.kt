package com.attendance.attendanceapp.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.attendanceapp.domain.model.Notification
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    onNavigateBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val schoolColor = Color(0xFF006064)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(
            title = "Notifications",
            onBackClick = onNavigateBack,
            actions = {
                Row {
                    if (notifications.any { !it.isRead }) {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Mark all as read", tint = schoolColor)
                        }
                    }
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAll() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear all", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        )

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Text(
                        "No notifications yet",
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItem(
                        notification = notification,
                        onMarkAsRead = { viewModel.markAsRead(notification.id) },
                        onDelete = { viewModel.deleteNotification(notification.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMarkAsRead() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else schoolColor.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicator
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, end = 12.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(schoolColor)
                )
            } else {
                Spacer(modifier = Modifier.width(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = sdf.format(Date(notification.createdAt)),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = if (notification.isRead) Color.Gray else Color.Black.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!notification.isRead) {
                    IconButton(onClick = onMarkAsRead) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Mark as read", tint = schoolColor, modifier = Modifier.size(22.dp))
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
