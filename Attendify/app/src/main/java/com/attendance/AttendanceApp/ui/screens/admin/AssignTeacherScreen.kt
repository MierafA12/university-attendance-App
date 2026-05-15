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
fun AssignTeacherScreen(
    onNavigateBack: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    
    var selectedTeacher by remember { mutableStateOf("") }
    var selectedCourse by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf("") }
    
    var teacherExpanded by remember { mutableStateOf(false) }
    var courseExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }
    
    val teachers = listOf("Dr. Robert", "Ms. Clara", "Mr. David")
    val courses = listOf("Software Engineering", "Database Systems", "Networking")
    val sections = listOf("Section A", "Section B", "Section 1", "Section 2")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(title = "Assign Teacher", onBackClick = onNavigateBack)

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
                    text = "Allocation Details",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Assign a teacher to a specific course and section",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                )

                AuthDropdownField(
                    label = "Select Teacher",
                    selectedOption = selectedTeacher,
                    expanded = teacherExpanded,
                    onExpandedChange = { teacherExpanded = it },
                    options = teachers,
                    onOptionSelected = { selectedTeacher = it },
                    leadingIcon = Icons.Outlined.Person
                )

                Spacer(modifier = Modifier.height(20.dp))

                AuthDropdownField(
                    label = "Select Course",
                    selectedOption = selectedCourse,
                    expanded = courseExpanded,
                    onExpandedChange = { courseExpanded = it },
                    options = courses,
                    onOptionSelected = { selectedCourse = it },
                    leadingIcon = Icons.Outlined.School
                )

                Spacer(modifier = Modifier.height(20.dp))

                AuthDropdownField(
                    label = "Select Section",
                    selectedOption = selectedSection,
                    expanded = sectionExpanded,
                    onExpandedChange = { sectionExpanded = it },
                    options = sections,
                    onOptionSelected = { selectedSection = it },
                    leadingIcon = Icons.Outlined.Groups
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { /* Assign Logic */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = schoolColor),
                    shape = RoundedCornerShape(16.dp),
                    enabled = selectedTeacher.isNotEmpty() && selectedCourse.isNotEmpty() && selectedSection.isNotEmpty()
                ) {
                    Text("Confirm Assignment", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
