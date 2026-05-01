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
    onNavigateBack: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    
    var selectedDay by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedCourse by remember { mutableStateOf("") }
    var selectedTeacher by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }
    
    var dayExpanded by remember { mutableStateOf(false) }
    var timeExpanded by remember { mutableStateOf(false) }
    var courseExpanded by remember { mutableStateOf(false) }
    var teacherExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }
    
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val timeSlots = listOf("08:30 AM - 10:00 AM", "10:30 AM - 12:00 PM", "01:30 PM - 03:00 PM", "03:30 PM - 05:00 PM")
    val courses = listOf("Software Engineering", "Database Systems", "Mobile Development", "Operating Systems")
    val teachers = listOf("Dr. Robert", "Ms. Clara", "Mr. David", "Dr. Sarah")
    val years = listOf("Year 1", "Year 2", "Year 3", "Year 4", "Year 5")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(title = "Schedule Management", onBackClick = onNavigateBack)

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
                    options = courses,
                    onOptionSelected = { selectedCourse = it },
                    leadingIcon = Icons.Outlined.Book
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthDropdownField(
                    label = "Teacher",
                    selectedOption = selectedTeacher,
                    expanded = teacherExpanded,
                    onExpandedChange = { teacherExpanded = it },
                    options = teachers,
                    onOptionSelected = { selectedTeacher = it },
                    leadingIcon = Icons.Outlined.Person
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthDropdownField(
                    label = "Target Year",
                    selectedOption = selectedYear,
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = it },
                    options = years,
                    onOptionSelected = { selectedYear = it },
                    leadingIcon = Icons.Outlined.School
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { /* Save Schedule Logic */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = schoolColor),
                    shape = RoundedCornerShape(16.dp),
                    enabled = selectedDay.isNotEmpty() && selectedTime.isNotEmpty() && selectedCourse.isNotEmpty() && selectedTeacher.isNotEmpty()
                ) {
                    Text("Save to Schedule", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { /* View Current Schedule */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("View Existing Schedule", color = Color.Black)
                }
            }
        }
    }
}
