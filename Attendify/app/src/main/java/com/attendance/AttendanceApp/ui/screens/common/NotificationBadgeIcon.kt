package com.attendance.attendanceapp.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationBadgeIcon(
    viewModel: NotificationViewModel,
    onClick: () -> Unit,
    tint: Color = Color.White
) {
    val unreadCount by viewModel.unreadCount.collectAsState()

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.1f))
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = tint,
                modifier = Modifier.padding(4.dp)
            )
            
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
