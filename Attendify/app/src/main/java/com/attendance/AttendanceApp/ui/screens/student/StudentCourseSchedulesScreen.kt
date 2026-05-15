package com.attendance.attendanceapp.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.attendanceapp.ui.screens.common.AttendifyTopBar

@Composable
fun StudentCourseSchedulesScreen(
    viewModel: StudentViewModel,
    onNavigateBack: () -> Unit
) {
    val schedules by viewModel.studentSchedules.collectAsState()
    val courses by viewModel.studentCourses.collectAsState()
    val courseNames by viewModel.courseNames.collectAsState()
    val schoolColor = Color(0xFF006064)
    
    var selectedTab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val tabs = listOf("My Courses", "Weekly Schedule")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(title = "Academic Info", onBackClick = onNavigateBack)

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = schoolColor
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        if (selectedTab == 0) {
            // Courses List
            if (courses.isEmpty()) {
                EmptyStateView("No courses found for your program", schoolColor)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(courses) { course ->
                        ScheduleCard(
                            title = course.name,
                            time = "Year ${course.year}",
                            room = "Semester ${course.semester}",
                            schoolColor = schoolColor
                        )
                    }
                }
            }
        } else {
            // Weekly Schedule
            if (schedules.isEmpty()) {
                EmptyStateView("No schedules found for your program", schoolColor)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Group by day of week
                    val groupedSchedules = schedules.groupBy { it.dayOfWeek }
                    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                    
                    days.forEach { day ->
                        val daySchedules = groupedSchedules[day]
                        if (!daySchedules.isNullOrEmpty()) {
                            item {
                                Text(
                                    text = day,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = schoolColor,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(daySchedules) { schedule ->
                                ScheduleCard(
                                    title = courseNames[schedule.courseId] ?: schedule.courseId,
                                    time = "${schedule.startTime} - ${schedule.endTime}",
                                    room = "Year ${schedule.year} - Sem ${schedule.semester}",
                                    schoolColor = schoolColor
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(message: String, color: Color) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, color = Color.Gray)
        }
    }
}


@Composable
fun ScheduleCard(title: String, time: String, room: String, schoolColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = room, fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                text = time,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = schoolColor
            )
        }
    }
}
