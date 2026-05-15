package com.attendance.attendanceapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.attendanceapp.ui.screens.auth.AuthDropdownField
import com.attendance.attendanceapp.ui.screens.common.AttendifyTopBar
import com.attendance.attendanceapp.domain.model.Schedule

@Composable
fun ViewSchedulesScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    
    val allSchedules by viewModel.allSchedules.collectAsState()
    
    var selectedDepartment by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("1") }
    var selectedSemester by remember { mutableStateOf("Semester 1") }
    
    val departments by viewModel.departments.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val teachers by viewModel.teachers.collectAsState()

    // Initialize defaults when data arrives
    LaunchedEffect(departments) {
        if (selectedDepartment.isEmpty() && departments.isNotEmpty()) {
            selectedDepartment = departments.first().name
        }
    }
    
    var departmentExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }
    var semesterExpanded by remember { mutableStateOf(false) }
    
    val departmentsList = departments.map { it.name }
    val years = listOf("1", "2", "3", "4", "5")
    
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val timeSlots = listOf("08:30 AM - 10:00 AM", "10:30 AM - 12:00 PM", "01:30 PM - 03:00 PM", "03:30 PM - 05:00 PM")

    val filteredSchedules = allSchedules.filter {
        val dept = departments.find { d -> d.name == selectedDepartment }
        val semester = if (selectedSemester == "Semester 1") "1" else "2"
        (dept == null || it.departmentId == dept.id) && it.year == selectedYear && it.semester == semester
    }

    Scaffold(
        topBar = { AttendifyTopBar(title = "Existing Schedules", onBackClick = onNavigateBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Filter Schedules", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            AuthDropdownField(
                                label = "Department",
                                selectedOption = selectedDepartment,
                                expanded = departmentExpanded,
                                onExpandedChange = { departmentExpanded = it },
                                options = departmentsList,
                                onOptionSelected = { selectedDepartment = it },
                                leadingIcon = Icons.Outlined.Business
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            AuthDropdownField(
                                label = "Year",
                                selectedOption = selectedYear,
                                expanded = yearExpanded,
                                onExpandedChange = { yearExpanded = it },
                                options = years,
                                onOptionSelected = { selectedYear = it },
                                leadingIcon = Icons.Outlined.School
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            AuthDropdownField(
                                label = "Semester",
                                selectedOption = selectedSemester,
                                expanded = semesterExpanded,
                                onExpandedChange = { semesterExpanded = it },
                                options = listOf("Semester 1", "Semester 2"),
                                onOptionSelected = { selectedSemester = it },
                                leadingIcon = Icons.Outlined.Event
                            )
                        }
                    }
                }
            }

            // Timetable Table
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "$selectedDepartment - Year $selectedYear",
                        fontWeight = FontWeight.Bold, 
                        fontSize = 20.sp, 
                        color = schoolColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val verticalScroll = rememberScrollState()
                    val horizontalScroll = rememberScrollState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(horizontalScroll)
                            .verticalScroll(verticalScroll)
                    ) {
                        Column {
                            // Header Row
                            Row(modifier = Modifier.background(Color(0xFFE0F7FA))) {
                                TableCell(text = "Time \\ Day", isHeader = true, width = 140.dp)
                                days.forEach { day ->
                                    TableCell(text = day, isHeader = true, width = 120.dp)
                                }
                            }

                            // Time Slot Rows
                            timeSlots.forEach { time ->
                                Row(modifier = Modifier.border(1.dp, Color.LightGray)) {
                                    TableCell(text = time, isHeader = true, width = 140.dp)
                                    days.forEach { day ->
                                        val cellSchedule = filteredSchedules.find { it.dayOfWeek == day && "${it.startTime} - ${it.endTime}" == time }
                                        val content = if (cellSchedule != null) {
                                            // Fallback to ID if name not found in case names were saved directly
                                            val courseName = courses.find { it.id == cellSchedule.courseId }?.name ?: cellSchedule.courseId
                                            val teacherName = teachers.find { it.id == cellSchedule.teacherId }?.name ?: cellSchedule.teacherId
                                            
                                            "$courseName\n(Teacher: $teacherName)"
                                        } else {
                                            "-"
                                        }
                                        TableCell(text = content, isHeader = false, width = 120.dp)
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

@Composable
fun TableCell(text: String, isHeader: Boolean, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(if (isHeader) 60.dp else 100.dp)
            .border(0.5.dp, Color.LightGray)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isHeader) 14.sp else 12.sp,
            textAlign = TextAlign.Center,
            color = if (isHeader) Color(0xFF006064) else Color.Black
        )
    }
}
