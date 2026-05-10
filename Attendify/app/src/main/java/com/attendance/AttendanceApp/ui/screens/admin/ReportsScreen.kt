package com.attendance.attendanceapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.attendanceapp.ui.screens.common.AttendifyTopBar
import com.attendance.attendanceapp.domain.model.*

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    
    val departments by viewModel.departments.collectAsState()
    val courses by viewModel.courses.collectAsState()
    
    val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    
    // Generate dynamic reports based on available data
    val reports = buildList {
        add(AttendanceReport("University Wide Weekly Report", "All Departments", currentDate))
        
        departments.forEach { dept ->
            add(AttendanceReport("Weekly Attendance", "${dept.name} Department", currentDate))
        }
        
        courses.forEach { course ->
            add(AttendanceReport("Course Weekly Report", course.name, currentDate))
        }
    }

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
                var selectedTab by androidx.compose.runtime.mutableIntStateOf(0)
                val tabs = listOf("Reports", "Sessions")
                
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = schoolColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = schoolColor
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (selectedTab == 0) {
                    Text(text = "Generated Reports", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(reports) { report ->
                            ReportItem(report, schoolColor)
                        }
                    }
                } else {
                    val allSessions by viewModel.allSessions.collectAsState()
                    val allSchedules by viewModel.allSchedules.collectAsState()
                    
                    Text(text = "Recent Attendance Sessions", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(allSessions.sortedByDescending { it.date }) { session ->
                            val schedule = allSchedules.find { it.scheduleId == session.scheduleId }
                            val courseName = courses.find { it.id == schedule?.courseId }?.name ?: "Unknown Course"
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(schoolColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = schoolColor)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = courseName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(session.date)), fontSize = 12.sp, color = Color.Gray)
                                    }
                                    if (session.isActive) {
                                        Text(text = "Active", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text(text = "Closed", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
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
