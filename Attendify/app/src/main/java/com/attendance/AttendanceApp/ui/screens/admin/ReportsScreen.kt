package com.attendance.attendanceapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.outlined.Description
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
fun ReportsScreen(
    onNavigateBack: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    
    // Mock Data
    val reports = listOf(
        AttendanceReport("Monthly Attendance - April", "All Departments", "27 April 2026"),
        AttendanceReport("CS Dept Weekly", "Computer Science", "26 April 2026"),
        AttendanceReport("Teacher Performance", "Staff", "25 April 2026"),
        AttendanceReport("Low Attendance Alert", "Mechanical", "24 April 2026")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(title = "Reports", onBackClick = onNavigateBack)

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = "Generated Reports", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(reports) { report ->
                        ReportItem(report, schoolColor)
                    }
                }
            }
        }
    }
}

data class AttendanceReport(val name: String, val scope: String, val date: String)

@Composable
fun ReportItem(report: AttendanceReport, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Description, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = report.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${report.scope} • ${report.date}", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.FileDownload, contentDescription = "Download", tint = color)
            }
        }
    }
}
