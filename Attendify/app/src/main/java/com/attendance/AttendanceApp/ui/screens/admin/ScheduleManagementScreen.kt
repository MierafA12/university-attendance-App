package com.attendance.attendanceapp.ui.screens.admin

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
fun ScheduleManagementScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToViewSchedules: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    
    val teachers by viewModel.teachers.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val uiState by viewModel.uiState
    
    var selectedDay by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedCourse by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf("") }
    var selectedTeacherId by remember { mutableStateOf("") }
    var selectedTeacherName by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }
    
    var dayExpanded by remember { mutableStateOf(false) }
    var timeExpanded by remember { mutableStateOf(false) }
    var courseExpanded by remember { mutableStateOf(false) }
    var departmentExpanded by remember { mutableStateOf(false) }
    var teacherExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }
    
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val timeSlots = listOf("08:30 AM - 10:00 AM", "10:30 AM - 12:00 PM", "01:30 PM - 03:00 PM", "03:30 PM - 05:00 PM")
    val coursesList = courses.map { it.name }
    val departmentsList = departments.map { it.name }
    
    val years = listOf("1", "2", "3", "4", "5")

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is AdminUiState.Success) {
            snackbarHostState.showSnackbar((uiState as AdminUiState.Success).message)
            viewModel.resetState()
        } else if (uiState is AdminUiState.Error) {
            snackbarHostState.showSnackbar((uiState as AdminUiState.Error).message)
            viewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AttendifyTopBar(title = "Schedule Management", onBackClick = onNavigateBack) }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    text = "Create Weekly Schedule",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Assign teachers and courses to specific time slots",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                )

                if (uiState is AdminUiState.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), color = schoolColor)
                }

                AuthDropdownField(
                    label = "Select Day",
                    selectedOption = selectedDay,
                    expanded = dayExpanded,
                    onExpandedChange = { dayExpanded = it },
                    options = days,
                    onOptionSelected = { selectedDay = it },
                    leadingIcon = Icons.Outlined.Today
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthDropdownField(
                    label = "Time Slot",
                    selectedOption = selectedTime,
                    expanded = timeExpanded,
                    onExpandedChange = { timeExpanded = it },
                    options = timeSlots,
                    onOptionSelected = { selectedTime = it },
                    leadingIcon = Icons.Outlined.Schedule
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthDropdownField(
                    label = "Course",
                    selectedOption = selectedCourse,
                    expanded = courseExpanded,
                    onExpandedChange = { courseExpanded = it },
                    options = coursesList,
                    onOptionSelected = { selectedCourse = it },
                    leadingIcon = Icons.Outlined.Book
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthDropdownField(
                    label = "Teacher",
                    selectedOption = selectedTeacherName,
                    expanded = teacherExpanded,
                    onExpandedChange = { teacherExpanded = it },
                    options = teachers.map { it.name },
                    onOptionSelected = { name -> 
                        selectedTeacherName = name
                        selectedTeacherId = teachers.find { it.name == name }?.id ?: ""
                    },
                    leadingIcon = Icons.Outlined.Person
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthDropdownField(
                    label = "Department",
                    selectedOption = selectedDepartment,
                    expanded = departmentExpanded,
                    onExpandedChange = { departmentExpanded = it },
                    options = departmentsList,
                    onOptionSelected = { selectedDepartment = it },
                    leadingIcon = Icons.Outlined.Business
                )

                Spacer(modifier = Modifier.height(16.dp))

                var selectedSemester by remember { mutableStateOf("Semester 1") }
                var semesterExpanded by remember { mutableStateOf(false) }

                AuthDropdownField(
                    label = "Year",
                    selectedOption = selectedYear,
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = it },
                    options = years,
                    onOptionSelected = { selectedYear = it },
                    leadingIcon = Icons.Outlined.CalendarToday
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthDropdownField(
                    label = "Semester",
                    selectedOption = selectedSemester,
                    expanded = semesterExpanded,
                    onExpandedChange = { semesterExpanded = it },
                    options = listOf("Semester 1", "Semester 2"),
                    onOptionSelected = { selectedSemester = it },
                    leadingIcon = Icons.Outlined.Event
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { 
                        val times = selectedTime.split(" - ")
                        val deptId = departments.find { it.name == selectedDepartment }?.id ?: ""
                        val courseId = courses.find { it.name == selectedCourse }?.id ?: ""
                        val semester = if (selectedSemester == "Semester 1") "1" else "2"
                        
                        viewModel.createSchedule(
                            courseId = courseId,
                            teacherId = selectedTeacherId,
                            departmentId = deptId,
                            year = selectedYear,
                            semester = semester,
                            dayOfWeek = selectedDay,
                            startTime = times[0],
                            endTime = times[1]
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = schoolColor),
                    shape = RoundedCornerShape(16.dp),
                    enabled = selectedDay.isNotEmpty() && selectedTime.isNotEmpty() && selectedCourse.isNotEmpty() && selectedTeacherId.isNotEmpty() && selectedDepartment.isNotEmpty() && uiState !is AdminUiState.Loading
                ) {
                    Text("Save to Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = onNavigateToViewSchedules,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("View Existing Schedule", color = Color.Black)
                }
            }
        }
    }
}
