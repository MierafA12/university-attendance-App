package com.attendance.attendanceapp.ui.screens.admin

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
fun AdminDashboard(
    adminName: String = "Admin",
    onManageUsers: () -> Unit,
    onViewReports: () -> Unit,
    onAssignTeachers: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onManageSchedule: () -> Unit
)
{
    val schoolColor = Color(0xFF006064)
    val lightTeal = Color(0xFFE0F2F1)

    Scaffold(
        bottomBar = {
            AttendifyBottomNav(
                selectedRoute = "home",
                onRouteSelected = { route ->
                    when(route) {
                        "attendance" -> onManageUsers() // Admin uses manage users for attendance related management
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
                            Text(text = "System Control,", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                            Text(text = adminName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { },
                            modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AdminActionItem(Icons.Default.PersonAdd, "Add User", onClick = onManageUsers)
                        AdminActionItem(Icons.Default.EventNote, "Schedule", onClick = onManageSchedule)
                        AdminActionItem(Icons.Default.Analytics, "Stats", onClick = onViewReports)
                        AdminActionItem(Icons.Default.CloudDownload, "Export")
                    }
                }
            }

            // Main content
            Column(modifier = Modifier.padding(20.dp)) {
                // Overview Cards
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(Modifier.weight(1f), "Total Students", "1,240", Icons.Default.Groups, schoolColor)
                    StatCard(Modifier.weight(1f), "Active Teachers", "48", Icons.Default.School, schoolColor)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Recent Activities", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                ActivityItem("New teacher registered: Dr. Sarah", "10 minutes ago", Icons.Default.PersonAdd, schoolColor)
                ActivityItem("Report generated for CS Dept", "1 hour ago", Icons.Default.FileDownload, schoolColor)
                ActivityItem("Attendance alert: Low in Sec A", "3 hours ago", Icons.Default.Warning, Color.Red)
            }
        }
    }
}

@Composable
fun AdminActionItem(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
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
fun StatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ActivityItem(text: String, time: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(text = time, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
