package com.attendance.attendanceapp.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.attendanceapp.domain.model.AttendanceStatus
import com.attendance.attendanceapp.domain.model.Role
import com.attendance.attendanceapp.domain.model.SessionStudentReport
import java.text.SimpleDateFormat
import java.util.*
import com.attendance.attendanceapp.ui.screens.common.AttendifyTopBar

@Composable
fun AttendanceListScreen(
    viewModel: TeacherViewModel,
    sessionId: String,
    onNavigateBack: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    
    // Collect attendance records with all expected students
    val studentsReport by viewModel.getAttendanceForSession(sessionId).collectAsState(initial = emptyList())

    val presentCount = studentsReport.count { it.status == AttendanceStatus.Present }
    val absentCount = studentsReport.count { it.status == AttendanceStatus.Absent }
    val permissionCount = studentsReport.count { it.status == AttendanceStatus.Permission }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(title = "Attendance List", onBackClick = onNavigateBack)

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(text = "Session Attendance", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusCounter("Present", presentCount, Color(0xFF388E3C))
                StatusCounter("Absent", absentCount, Color(0xFFD32F2F))
                StatusCounter("Permission", permissionCount, Color(0xFFFBC02D))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            color = Color.White
        ) {
            if (studentsReport.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No students expected for this session's academic group.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(studentsReport) { student ->
                        StudentAttendanceItem(
                            student = student, 
                            color = schoolColor,
                            onStatusChange = { newStatus ->
                                viewModel.updateAttendanceStatus(sessionId, student.studentId, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCounter(label: String, count: Int, color: Color) {
    Column {
        Text(text = count.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}


@Composable
fun StudentAttendanceItem(
    student: SessionStudentReport, 
    color: Color,
    onStatusChange: (AttendanceStatus) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val statusColor = when (student.status) {
        AttendanceStatus.Present -> Color(0xFF388E3C)
        AttendanceStatus.Permission -> Color(0xFF1976D2)
        AttendanceStatus.Absent -> Color(0xFFD32F2F)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
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
                val icon = if (student.status == AttendanceStatus.Present) Icons.Default.CheckCircle else Icons.Default.People
                Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Status: ${student.status.name}", fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.Medium)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(text = student.time, fontSize = 12.sp, color = Color.Gray)
                
                // Only allow editing if the student is NOT already 'Present'
                if (student.status != AttendanceStatus.Present) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Change Status", tint = Color.LightGray)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            AttendanceStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.name) },
                                    onClick = {
                                        onStatusChange(status)
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Show a locked icon for Present students to indicate they cannot be edited
                    Icon(
                        Icons.Default.Check, 
                        contentDescription = "Confirmed", 
                        tint = Color(0xFF388E3C),
                        modifier = Modifier.padding(12.dp).size(20.dp)
                    )
                }
            }
        }
    }
}
