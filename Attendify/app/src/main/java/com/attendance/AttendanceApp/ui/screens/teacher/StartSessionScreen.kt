package com.attendance.attendanceapp.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.attendanceapp.ui.screens.auth.AuthDropdownField
import com.attendance.attendanceapp.ui.screens.common.AttendifyTopBar

@Composable
fun StartSessionScreen(
    viewModel: TeacherViewModel,
    onNavigateBack: () -> Unit,
    onSessionStarted: (String) -> Unit
) {
    val schoolColor = Color(0xFF006064)
    
    val allSchedules by viewModel.schedulesWithDetails.collectAsState()
    val todaySchedules by viewModel.todaySchedules.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val uiState by viewModel.uiState
    
    var selectedScheduleId by remember { mutableStateOf("") }
    var scheduleExpanded by remember { mutableStateOf(false) }
    var durationMinutes by remember { mutableStateOf(15f) }
    var maxStudents by remember { mutableStateOf(0f) }
    
    val scheduleNames = allSchedules.map { "${it.courseName} (${it.schedule.startTime} - ${it.schedule.dayOfWeek})" }

    LaunchedEffect(uiState) {
        if (uiState is TeacherUiState.Success && (uiState as TeacherUiState.Success).sessionId.isNotEmpty()) {
            // New session successfully created
            onSessionStarted((uiState as TeacherUiState.Success).sessionId)
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(title = "Start Attendance", onBackClick = onNavigateBack)

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Start Session",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Select an assigned schedule and duration to generate a QR code.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                )

                if (allSchedules.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No courses assigned to you yet.", color = Color.Gray)
                    }
                } else {
                    AuthDropdownField(
                        label = "Select Schedule",
                        selectedOption = allSchedules.find { it.schedule.scheduleId == selectedScheduleId }?.let { "${it.courseName} (${it.schedule.startTime})" } ?: "",
                        expanded = scheduleExpanded,
                        onExpandedChange = { scheduleExpanded = it },
                        options = scheduleNames,
                        onOptionSelected = { name ->
                            selectedScheduleId = allSchedules.find { "${it.courseName} (${it.schedule.startTime} - ${it.schedule.dayOfWeek})" == name }?.schedule?.scheduleId ?: ""
                        },
                        leadingIcon = Icons.Outlined.Event
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Session Duration: ${durationMinutes.toInt()} minutes",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    
                    Slider(
                        value = durationMinutes,
                        onValueChange = { durationMinutes = it },
                        valueRange = 1f..500f,
                        steps = 499,
                        colors = SliderDefaults.colors(
                            thumbColor = schoolColor,
                            activeTrackColor = schoolColor,
                            inactiveTrackColor = schoolColor.copy(alpha = 0.2f)
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1m", fontSize = 12.sp, color = Color.Gray)
                        Text("500m", fontSize = 12.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Max Students (0 = No Limit): ${maxStudents.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    
                    Slider(
                        value = maxStudents,
                        onValueChange = { maxStudents = it },
                        valueRange = 0f..200f,
                        steps = 199,
                        colors = SliderDefaults.colors(
                            thumbColor = schoolColor,
                            activeTrackColor = schoolColor,
                            inactiveTrackColor = schoolColor.copy(alpha = 0.2f)
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("No Limit", fontSize = 12.sp, color = Color.Gray)
                        Text("200", fontSize = 12.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    if (uiState is TeacherUiState.Error) {
                        Text(
                            text = (uiState as TeacherUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.startSession(selectedScheduleId, durationMinutes.toInt(), maxStudents.toInt()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = schoolColor),
                        shape = RoundedCornerShape(16.dp),
                        enabled = selectedScheduleId.isNotEmpty() && uiState !is TeacherUiState.Loading
                    ) {
                        if (uiState is TeacherUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Generate QR Code", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
