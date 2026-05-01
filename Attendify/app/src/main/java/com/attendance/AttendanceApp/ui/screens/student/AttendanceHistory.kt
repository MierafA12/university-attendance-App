package com.attendance.attendanceapp.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.attendanceapp.ui.screens.common.AttendifyTopBar

@Composable
fun AttendanceHistoryScreen(
    onNavigateBack: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    
    // Mock Data
    val historyItems = listOf(
        AttendanceRecord("Mobile Application Development", "Present", "27 April 2026", "09:00 AM"),
        AttendanceRecord("Operating Systems", "Late", "26 April 2026", "02:00 PM"),
        AttendanceRecord("Computer Networking", "Absent", "25 April 2026", "10:30 AM"),
        AttendanceRecord("Software Engineering", "Present", "24 April 2026", "08:30 AM"),
        AttendanceRecord("Database Systems", "Present", "23 April 2026", "11:00 AM"),
        AttendanceRecord("Theory of Computation", "Present", "22 April 2026", "02:00 PM")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(
            title = "Attendance History",
            onBackClick = onNavigateBack
        )

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            color = Color.White
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(historyItems) { record ->
                    HistoryItem(record, schoolColor)
                }
            }
        }
    }
}

data class AttendanceRecord(
    val subject: String,
    val status: String,
    val date: String,
    val time: String
)

@Composable
fun HistoryItem(record: AttendanceRecord, schoolColor: Color) {
    val statusColor = when (record.status) {
        "Present" -> schoolColor
        "Late" -> Color(0xFFFBC02D)
        else -> Color.Red
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(record.status) {
                        "Present" -> Icons.Default.CheckCircle
                        "Late" -> Icons.Default.Schedule
                        else -> Icons.Default.Cancel
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = record.subject, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${record.date} • ${record.time}", fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = record.status,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
