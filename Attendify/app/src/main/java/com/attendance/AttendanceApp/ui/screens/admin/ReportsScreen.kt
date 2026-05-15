package com.attendance.attendanceapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import com.attendance.attendanceapp.ui.screens.common.AttendifyTopBar
import com.attendance.attendanceapp.domain.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Students", "Sessions")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(title = "Reports", onBackClick = onNavigateBack)

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
                    text = { Text(title, fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            when (selectedTab) {
                0 -> AdminStudentReportsTab(viewModel)
                1 -> AdminSessionsTab(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudentReportsTab(viewModel: AdminViewModel) {
    val schoolColor = Color(0xFF006064)
    val allStudents by viewModel.studentReports.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val context = LocalContext.current

    var selectedDeptId by remember { mutableStateOf("") }
    var selectedSemester by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }

    val courses by viewModel.courses.collectAsState()
    val selectedCourseId by viewModel.selectedCourseId.collectAsState()

    var deptExpanded by remember { mutableStateOf(false) }
    var semExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }
    var courseExpanded by remember { mutableStateOf(false) }
    
    var csvContentToSave by remember { mutableStateOf("") }
    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { output ->
                    output.write(csvContentToSave.toByteArray())
                }
            } catch (e: Exception) {
                android.util.Log.e("Export", "Failed to save CSV", e)
            }
        }
    }

    val filteredStudents = allStudents.filter {
        val studentYearDigits = it.year.filter { c -> c.isDigit() }
        val filterYearDigits = selectedYear.filter { c -> c.isDigit() }
        val studentSemDigits = it.semester.filter { c -> c.isDigit() }
        val filterSemDigits = selectedSemester.filter { c -> c.isDigit() }
        
        val matchesDept = if (selectedDeptId.isEmpty()) true 
        else {
            it.departmentId.equals(selectedDeptId, ignoreCase = true) || 
            departments.find { d -> d.id == selectedDeptId }?.name?.equals(it.departmentId, ignoreCase = true) == true
        }
        
        val matchesSem = selectedSemester.isEmpty() || 
                        studentSemDigits == filterSemDigits || 
                        it.semester.equals(selectedSemester, ignoreCase = true)
                        
        val matchesYear = selectedYear.isEmpty() || 
                         studentYearDigits == filterYearDigits || 
                         it.year.equals(selectedYear, ignoreCase = true)
        
        matchesDept && matchesSem && matchesYear
    }

    val availableCourses = courses.filter {
        (selectedDeptId.isEmpty() || it.departmentId == selectedDeptId) &&
        (selectedSemester.isEmpty() || it.semester == selectedSemester) &&
        (selectedYear.isEmpty() || it.year == selectedYear)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filters Row 1: Dept, Sem, Year
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Department Filter
            ExposedDropdownMenuBox(
                expanded = deptExpanded,
                onExpandedChange = { deptExpanded = !deptExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = departments.find { it.id == selectedDeptId }?.name ?: "All Depts",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                    modifier = Modifier.menuAnchor(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
                )
                ExposedDropdownMenu(
                    expanded = deptExpanded,
                    onDismissRequest = { deptExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("All Depts") }, onClick = { selectedDeptId = ""; deptExpanded = false })
                    departments.forEach { dept ->
                        DropdownMenuItem(text = { Text(dept.name) }, onClick = { selectedDeptId = dept.id; deptExpanded = false })
                    }
                }
            }

            // Semester Filter
            ExposedDropdownMenuBox(
                expanded = semExpanded,
                onExpandedChange = { semExpanded = !semExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = if (selectedSemester.isEmpty()) "All Sems" else "Sem $selectedSemester",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semExpanded) },
                    modifier = Modifier.menuAnchor(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
                )
                ExposedDropdownMenu(
                    expanded = semExpanded,
                    onDismissRequest = { semExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("All Sems") }, onClick = { selectedSemester = ""; semExpanded = false })
                    listOf("1", "2").forEach { sem ->
                        DropdownMenuItem(text = { Text("Sem $sem") }, onClick = { selectedSemester = sem; semExpanded = false })
                    }
                }
            }

            // Year Filter
            ExposedDropdownMenuBox(
                expanded = yearExpanded,
                onExpandedChange = { yearExpanded = !yearExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = if (selectedYear.isEmpty()) "All Years" else "Year $selectedYear",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                    modifier = Modifier.menuAnchor(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
                )
                ExposedDropdownMenu(
                    expanded = yearExpanded,
                    onDismissRequest = { yearExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("All Years") }, onClick = { selectedYear = ""; yearExpanded = false })
                    listOf("1", "2", "3", "4", "5").forEach { yr ->
                        DropdownMenuItem(text = { Text("Year $yr") }, onClick = { selectedYear = yr; yearExpanded = false })
                    }
                }
            }
        }

        // Filters Row 2: Course
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = courseExpanded,
                onExpandedChange = { courseExpanded = !courseExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = availableCourses.find { it.id == selectedCourseId }?.name ?: "All Courses",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                    modifier = Modifier.menuAnchor(),
                    label = { Text("Filter by Course", fontSize = 12.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                )
                ExposedDropdownMenu(
                    expanded = courseExpanded,
                    onDismissRequest = { courseExpanded = false }
                ) {
                    DropdownMenuItem(text = { Text("All Courses") }, onClick = { viewModel.setCourseFilter(""); courseExpanded = false })
                    availableCourses.forEach { course ->
                        DropdownMenuItem(text = { Text(course.name) }, onClick = { viewModel.setCourseFilter(course.id); courseExpanded = false })
                    }
                }
            }
        }

        var csvDetailedContent by remember { mutableStateOf("") }
        val detailedExportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("text/csv")
        ) { uri ->
            uri?.let {
                try {
                    context.contentResolver.openOutputStream(it)?.use { output ->
                        output.write(csvDetailedContent.toByteArray())
                    }
                } catch (e: Exception) {
                    android.util.Log.e("Export", "Failed to save Detailed CSV", e)
                }
            }
        }

        // Export Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Summary Button
            Button(
                onClick = {
                    val courseName = availableCourses.find { it.id == selectedCourseId }?.name ?: "All Courses"
                    csvContentToSave = buildString {
                        append("Attendance Summary for: $courseName\n")
                        append("Student Name,Department,Year,Semester,Present Count,Total Sessions,Percentage\n")
                        filteredStudents.forEach {
                            val deptName = departments.find { d -> d.id == it.departmentId }?.name ?: it.departmentId
                            append("${it.name},${deptName},${it.year},${it.semester},${it.presentCount},${it.totalSessions},${it.attendancePercentage}%\n")
                        }
                    }
                    val fileName = "Summary_${courseName.replace(" ", "_")}_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.csv"
                    exportLauncher.launch(fileName)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = schoolColor),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Summarize, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Summary", fontSize = 12.sp)
            }

            // Detailed Button
            Button(
                onClick = {
                    val courseName = availableCourses.find { it.id == selectedCourseId }?.name ?: "All Courses"
                    val allSessions = viewModel.allSessions.value
                    val allSchedules = viewModel.allSchedules.value
                    val allAttendance = viewModel.allAttendance.value
                    
                    csvDetailedContent = buildString {
                        append("Detailed Attendance Log: $courseName\n")
                        append("Date,Time,Course,Student Name,Status\n")
                        
                        // Sort sessions by date
                        val relevantSessions = allSessions.filter { session ->
                            val schedule = allSchedules.find { it.scheduleId == session.scheduleId }
                            selectedCourseId.isEmpty() || schedule?.courseId == selectedCourseId
                        }.sortedByDescending { it.date }

                        relevantSessions.forEach { session ->
                            val schedule = allSchedules.find { it.scheduleId == session.scheduleId }
                            val sessionCourseName = availableCourses.find { it.id == schedule?.courseId }?.name ?: "Unknown"
                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(session.date))
                            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(session.date))
                            
                            // For each student in this course/report
                            filteredStudents.forEach { student ->
                                val attendance = allAttendance.find { it.sessionId == session.id && it.studentId == student.studentId }
                                val status = attendance?.status?.name ?: "Absent"
                                append("$dateStr,$timeStr,$sessionCourseName,${student.name},$status\n")
                            }
                        }
                    }
                    val fileName = "Detailed_${courseName.replace(" ", "_")}_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.csv"
                    detailedExportLauncher.launch(fileName)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Detailed Log", fontSize = 12.sp)
            }
        }

        if (filteredStudents.isNotEmpty()) {
            Text(
                text = "Registered Students (${filteredStudents.size})",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold,
                color = schoolColor
            )
        }

        // List
        if (filteredStudents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No students match the selected filters.",
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredStudents) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                val deptName = departments.find { it.id == student.departmentId }?.name ?: student.departmentId
                                Text(text = "$deptName | Year: ${student.year} | Sem: ${student.semester}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format("%.1f%%", student.attendancePercentage),
                                    fontWeight = FontWeight.Bold,
                                    color = if (student.attendancePercentage >= 75) Color(0xFF388E3C) else Color(0xFFD32F2F),
                                    fontSize = 16.sp
                                )
                                Text(text = "${student.presentCount}/${student.totalSessions}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSessionsTab(viewModel: AdminViewModel) {
    val schoolColor = Color(0xFF006064)
    val allSessions by viewModel.allSessions.collectAsState()
    val allSchedules by viewModel.allSchedules.collectAsState()
    val courses by viewModel.courses.collectAsState()

    Column(modifier = Modifier.padding(24.dp)) {
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
                        val isTrulyActive = session.isActive && (System.currentTimeMillis() < session.date + (session.durationMinutes.toLong() * 60 * 1000))
                        
                        if (isTrulyActive) {
                            Text(text = "Active", color = Color(0xFF388E3C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text(text = "Closed", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
