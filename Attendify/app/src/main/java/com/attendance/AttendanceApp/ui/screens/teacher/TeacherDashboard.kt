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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.attendanceapp.ui.screens.common.AttendifyBottomNav

@Composable
fun TeacherDashboard(
    teacherName: String = "Dr. Aman",
    onStartSession: () -> Unit,
    onViewReports: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    val lightTeal = Color(0xFFE0F2F1)

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
                        Column {
                            Text(text = "Welcome back,", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                            Text(text = teacherName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { },
                            modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TeacherActionItem(Icons.Default.AddCircle, "New Session", onClick = onStartSession)
                        TeacherActionItem(Icons.Default.Summarize, "Reports", onClick = onViewReports)
                        TeacherActionItem(Icons.Default.FactCheck, "History")
                        TeacherActionItem(Icons.Default.Settings, "Setup")
                    }
                }
            }

            // Main content
            Column(modifier = Modifier.padding(20.dp)) {
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
                        Text(text = "Software Engineering - L2", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = schoolColor)
                        Text(text = "Room 305 | Started 10:30 AM", fontSize = 14.sp, color = schoolColor.copy(alpha = 0.7f))
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = schoolColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("View Live Attendance")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Today's Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                ScheduleItem("Database Management", "01:00 PM - 03:00 PM", "Lab 2", schoolColor)
                ScheduleItem("System Analysis", "04:00 PM - 05:30 PM", "Room 102", schoolColor)
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
