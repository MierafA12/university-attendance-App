package com.attendance.attendanceapp.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.attendance.attendanceapp.ui.screens.common.AttendifyTopBar

@Composable
fun TeacherSchedulesScreen(
    viewModel: TeacherViewModel,
    onNavigateBack: () -> Unit
) {
    val schedules by viewModel.schedulesWithDetails.collectAsState()
    val schoolColor = Color(0xFF006064)

    Scaffold(
        topBar = { AttendifyTopBar(title = "All My Schedules", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (schedules.isEmpty()) {
                item {
                    Text(text = "No schedules found", modifier = Modifier.padding(16.dp), color = Color.Gray)
                }
            } else {
                items(schedules) { detailedSchedule ->
                    ScheduleItem(
                        title = detailedSchedule.courseName,
                        time = "${detailedSchedule.schedule.dayOfWeek} | ${detailedSchedule.schedule.startTime} - ${detailedSchedule.schedule.endTime}",
                        room = "Year ${detailedSchedule.schedule.year} | ${detailedSchedule.deptName}",
                        color = schoolColor
                    )
                }
            }
        }
    }
}
