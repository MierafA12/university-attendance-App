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
    onNavigateBack: () -> Unit,
    onSessionStarted: (String, String, Int) -> Unit
) {
    val schoolColor = Color(0xFF006064)
    
    var selectedCourse by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf(15) }
    
    var courseExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }
    
    val courses = listOf("Software Engineering", "Database Systems", "Mobile Application Development")
    val sections = listOf("Section A", "Section B", "Section 1", "Section 2")

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
                    text = "Configure Session",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Set up the session details for your students to join",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                )

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

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Session Duration (Minutes)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Slider(
                        value = duration.toFloat(),
                        onValueChange = { duration = it.toInt() },
                        valueRange = 5f..60f,
                        steps = 11,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = schoolColor,
                            activeTrackColor = schoolColor,
                            inactiveTrackColor = schoolColor.copy(alpha = 0.2f)
                        )
                    )
                    Text(
                        text = "$duration min",
                        modifier = Modifier.padding(start = 16.dp),
                        fontWeight = FontWeight.Bold,
                        color = schoolColor
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { onSessionStarted(selectedCourse, selectedSection, duration) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = schoolColor),
                    shape = RoundedCornerShape(16.dp),
                    enabled = selectedCourse.isNotEmpty() && selectedSection.isNotEmpty()
                ) {
                    Text("Start Session", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
