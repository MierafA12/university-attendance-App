package com.attendance.attendanceapp.ui.screens.student
 
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.attendanceapp.domain.model.*
import com.attendance.attendanceapp.ui.screens.common.AttendifyTopBar
import com.attendance.attendanceapp.ui.screens.student.StudentViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttendanceHistoryScreen(
    viewModel: StudentViewModel,
    initialTab: Int = 0,
    onNavigateBack: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    val attendanceReport by viewModel.attendanceReport.collectAsState()
    val detailedHistory by viewModel.detailedAttendanceHistory.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabs = listOf("Course Report", "Detailed History")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(
            title = "Attendance Records",
            onBackClick = onNavigateBack
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = schoolColor,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = schoolColor
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            color = Color.White
        ) {
            if (selectedTab == 0) {
                // Course-wise Report
                if (attendanceReport.isEmpty()) {
                    EmptyState("No courses found", schoolColor)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(attendanceReport) { report ->
                            CourseReportItem(report, schoolColor)
                        }
                    }
                }
            } else {
                // Detailed History (Grouped by Course and Week)
                if (detailedHistory.isEmpty()) {
                    EmptyState("No history records found", schoolColor)
                } else {
                    val groupedByCourse = detailedHistory.groupBy { it.courseName }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        groupedByCourse.forEach { (courseName, records) ->
                            item {
                                Text(
                                    text = courseName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = schoolColor,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            
                            // Group records for this course by week
                            val calendar = Calendar.getInstance()
                            val groupedByWeek = records.groupBy {
                                calendar.timeInMillis = it.timestamp
                                val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
                                val year = calendar.get(Calendar.YEAR)
                                "$year-W$weekOfYear"
                            }
                            
                            items(groupedByWeek.toList()) { (weekKey, weekRecords) ->
                                WeeklyHistoryItem(weekKey, weekRecords, schoolColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyHistoryItem(weekKey: String, records: List<AttendanceRecord>, schoolColor: Color) {
    // Determine the week range label
    val calendar = Calendar.getInstance()
    val parts = weekKey.split("-W")
    
    // Calculate week label safely outside of composable calls
    val weekLabel = remember(weekKey) {
        try {
            if (parts.size == 2) {
                calendar.set(Calendar.YEAR, parts[0].toIntOrNull() ?: 2024)
                calendar.set(Calendar.WEEK_OF_YEAR, parts[1].toIntOrNull() ?: 1)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val startDate = calendar.time
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                val endDate = calendar.time
                
                val df = SimpleDateFormat("MMM dd", Locale.getDefault())
                "Week of ${df.format(startDate)} - ${df.format(endDate)}"
            } else {
                "Attendance Period"
            }
        } catch (e: Exception) {
            "Attendance Period"
        }
    }

    if (parts.size == 2) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9).copy(alpha = 0.5f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = weekLabel, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
                    
                    val presentCount = records.count { it.status == AttendanceStatus.Present || it.status == AttendanceStatus.Late }
                    Text(
                        text = "$presentCount/${records.size} Sessions",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = schoolColor
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Show each session in that week compactly
                records.sortedByDescending { it.timestamp }.forEach { record ->
                    val dayName = try { 
                        SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(record.timestamp)) 
                    } catch(e: Exception) { "Day" }
                    
                    val timeStr = try { 
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(record.timestamp)) 
                    } catch(e: Exception) { "--:--" }
                    
                    val statusColor = when (record.status) {
                        AttendanceStatus.Present -> schoolColor
                        AttendanceStatus.Late -> Color(0xFFFBC02D)
                        else -> Color.Red
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = dayName, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(text = timeStr, fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = record.status.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(message: String, color: Color) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = Color.Gray)
        }
    }
}

@Composable
fun CourseReportItem(report: CourseAttendanceReport, schoolColor: Color) {
    val progress = report.percentage / 100f
    val color = if (report.absences >= 3) Color.Red else schoolColor
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = report.courseName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    Text(text = "Overall Attendance", fontSize = 12.sp, color = Color.Gray)
                }
                Text(
                    text = String.format("%.1f%%", report.percentage),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = color
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.1f),
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AttendanceStat(label = "Present", value = "${report.presentCount}", icon = Icons.Default.CheckCircle, color = schoolColor)
                AttendanceStat(label = "Absent", value = "${report.absences}", icon = Icons.Default.Cancel, color = if (report.absences >= 3) Color.Red else Color.Gray)
                AttendanceStat(label = "Total", value = "${report.totalSessions}", icon = Icons.AutoMirrored.Filled.EventNote, color = Color.Gray)
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                Text(
                    text = "Weekly Breakdown",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = schoolColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Tip: You can see detailed dates in the 'Detailed History' tab.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun AttendanceStat(label: String, value: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = color)
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Text(text = label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
