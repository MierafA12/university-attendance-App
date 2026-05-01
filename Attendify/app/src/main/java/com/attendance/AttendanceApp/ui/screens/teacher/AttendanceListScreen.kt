package com.attendance.attendanceapp.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
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
fun AttendanceListScreen(
    courseName: String = "Software Engineering",
    onNavigateBack: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    
    // Mock Data
    val students = listOf(
        JoinedStudent("Aman", "ETS0001/12", "09:05 AM"),
        JoinedStudent("Sarah", "ETS0002/12", "09:07 AM"),
        JoinedStudent("John", "ETS0003/12", "09:10 AM"),
        JoinedStudent("Elena", "ETS0004/12", "09:12 AM"),
        JoinedStudent("Michael", "ETS0005/12", "09:15 AM"),
        JoinedStudent("David", "ETS0006/12", "09:20 AM")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(title = "Live Attendance", onBackClick = onNavigateBack)

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(text = courseName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "${students.size} Students Checked-in", color = schoolColor, fontWeight = FontWeight.Medium)
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            color = Color.White
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(students) { student ->
                    StudentCheckInItem(student, schoolColor)
                }
            }
        }
    }
}

data class JoinedStudent(val name: String, val id: String, val time: String)

@Composable
fun StudentCheckInItem(student: JoinedStudent, color: Color) {
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
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = student.id, fontSize = 12.sp, color = Color.Gray)
            }
            Text(text = student.time, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.LightGray)
            }
        }
    }
}
