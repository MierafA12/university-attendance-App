package com.attendance.attendanceapp.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import com.attendance.attendanceapp.ui.screens.common.AttendifyBottomNav

@Composable
fun TeacherDashboard(
    viewModel: TeacherViewModel,
    notificationViewModel: com.attendance.attendanceapp.ui.screens.common.NotificationViewModel,
    onStartSession: () -> Unit,
    onViewLiveSession: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onViewReports: () -> Unit,
    onNavigateToSchedules: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    val lightTeal = Color(0xFFE0F2F1)

    val currentUser by viewModel.currentUser.collectAsState()
    val todaySchedules by viewModel.todaySchedules.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()

    Scaffold(
        bottomBar = {
            AttendifyBottomNav(
                selectedRoute = "home",
                onRouteSelected = { route ->
                    when(route) {
                        "attendance" -> onStartSession()
                        "reports" -> onViewReports()
                        "profile" -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(schoolColor)
                    .padding(24.dp)
                    .padding(top = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Welcome back,", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                            Text(text = currentUser?.name ?: "Teacher", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.attendance.attendanceapp.ui.screens.common.NotificationBadgeIcon(
                                viewModel = notificationViewModel,
                                onClick = onNavigateToNotifications
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onLogout,
                                modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        TeacherActionItem(Icons.Default.AddCircle, "New Session", onClick = onStartSession)
                        TeacherActionItem(Icons.Default.Schedule, "Schedules", onClick = onNavigateToSchedules)
                        TeacherActionItem(Icons.Default.Assessment, "Reports", onClick = onViewReports)
                    }
                }
            }

            // Main content
            Column(modifier = Modifier.padding(20.dp)) {
                if (activeSession != null) {
                    // Active Class Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = lightTeal)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Green))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Ongoing Session", color = schoolColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            val courseName by viewModel.getCourseNameByScheduleIdFlow(activeSession?.scheduleId ?: "").collectAsState(initial = "Loading Course...")
                            Text(text = courseName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = schoolColor)
                            Text(text = "Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(activeSession?.date ?: 0))}", fontSize = 14.sp, color = schoolColor.copy(alpha = 0.7f))
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Button(
                                onClick = { activeSession?.let { onViewLiveSession(it.id) } },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = schoolColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("View Live Session")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(text = "Today's Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                if (todaySchedules.isEmpty()) {
                    Text(text = "No classes scheduled for today", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    todaySchedules.forEach { detailedSchedule ->
                        ScheduleItem(
                            title = detailedSchedule.courseName,
                            time = "${detailedSchedule.schedule.startTime} - ${detailedSchedule.schedule.endTime}",
                            room = "Year ${detailedSchedule.schedule.year} | ${detailedSchedule.deptName}",
                            schoolColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherActionItem(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Surface(modifier = Modifier.size(64.dp), shape = RoundedCornerShape(20.dp), color = Color.White) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color(0xFF006064))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun ScheduleItem(title: String, time: String, room: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Event, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "$time | $room", fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
