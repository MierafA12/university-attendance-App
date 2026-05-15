package com.attendance.attendanceapp.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.horizontalScroll
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
import com.attendance.attendanceapp.domain.model.*
import java.text.SimpleDateFormat
import java.util.*
import com.attendance.attendanceapp.ui.screens.common.AttendifyBottomNav

@Composable
fun StudentDashboard(
    viewModel: StudentViewModel,
    notificationViewModel: com.attendance.attendanceapp.ui.screens.common.NotificationViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onViewCourses: () -> Unit,
    onViewHistory: () -> Unit,
    onViewReports: () -> Unit,
    onLogout: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    val lightTeal = Color(0xFFE0F2F1)
    
    val currentUser by viewModel.currentUser.collectAsState()
    val todaySchedules by viewModel.todaySchedules.collectAsState()
    val attendanceHistory by viewModel.attendanceHistory.collectAsState()
    val overallAttendance by viewModel.overallAttendance.collectAsState()
    val criticalCourses by viewModel.criticalCourses.collectAsState()
    val courseNames by viewModel.courseNames.collectAsState()
    val studentName = currentUser?.name ?: "Student"

    Scaffold(
        bottomBar = {
            AttendifyBottomNav(
                selectedRoute = "home",
                onRouteSelected = { route ->
                    when(route) {
                        "attendance" -> onNavigateToScan()
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
            // Header Section
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
                            Text(
                                text = "Good morning,",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                            Text(
                                text = studentName,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            val profile by viewModel.studentProfile.collectAsState()
                            if (profile != null) {
                                Row(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.School,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${profile?.departmentId ?: "No Dept"} | Year ${profile?.year} | Sem ${profile?.semester}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.attendance.attendanceapp.ui.screens.common.NotificationBadgeIcon(
                                viewModel = notificationViewModel,
                                onClick = onNavigateToNotifications
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onLogout,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Quick Action Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DashboardActionItem(Icons.Default.QrCodeScanner, "Scan QR", onClick = onNavigateToScan)
                        DashboardActionItem(Icons.Default.History, "History", onClick = onViewHistory)
                        DashboardActionItem(Icons.Default.Assignment, "Courses", onClick = onViewCourses)
                    }
                }
            }

            // Main Content
            Column(modifier = Modifier.padding(20.dp)) {
                // Notifications for critical courses
                if (criticalCourses.isNotEmpty()) {
                    criticalCourses.forEach { report ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Attendance Alert",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "You have ${report.absences} absences in ${report.courseName}",
                                        fontSize = 12.sp,
                                        color = Color.Black.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Attendance Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = lightTeal)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(text = "Overall Attendance", color = schoolColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format("%.1f%%", overallAttendance),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = schoolColor
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = onViewReports,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = schoolColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("See Full Attendance Report")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Upcoming Classes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Class Cards
                if (todaySchedules.isEmpty()) {
                    Text(text = "No classes scheduled for today", color = Color.Gray, fontSize = 14.sp)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        todaySchedules.forEach { schedule ->
                            ClassInfoCard(
                                Modifier.width(200.dp),
                                courseNames[schedule.courseId] ?: schedule.courseId,
                                "Year ${schedule.year}",
                                "${schedule.startTime} - ${schedule.endTime}"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Attendance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    TextButton(onClick = onViewHistory) {
                        Text("View All", color = schoolColor)
                    }
                }

                // Attendance List
                if (attendanceHistory.isEmpty()) {
                    Text(text = "No attendance records found", color = Color.Gray, fontSize = 14.sp)
                } else {
                    val allSessions by viewModel.allSessions.collectAsState()
                    val studentSchedules by viewModel.studentSchedules.collectAsState()
                    
                    attendanceHistory.take(5).forEach { record ->
                        val dateStr = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(record.timestamp))
                        
                        // Resolve course name for the record
                        val session = allSessions.find { it.id == record.sessionId }
                        val schedule = studentSchedules.find { it.scheduleId == session?.scheduleId }
                        val courseName = if (schedule != null) courseNames[schedule.courseId] ?: schedule.courseId else "Unknown Class"
                        
                        AttendanceLogItem(
                            courseName,
                            record.status.name,
                            dateStr,
                            if (record.status == AttendanceStatus.Present) schoolColor else Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardActionItem(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color(0xFF006064))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ClassInfoCard(modifier: Modifier, title: String, room: String, time: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = room, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = time, fontSize = 12.sp, color = Color(0xFF006064), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AttendanceLogItem(title: String, status: String, time: String, statusColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(status) {
                        "Present" -> Icons.Default.CheckCircle
                        "Permission" -> Icons.Default.Verified
                        else -> Icons.Default.Cancel
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = time, fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = status,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
