package com.attendance.attendanceapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.attendanceapp.ui.screens.common.AttendifyTopBar

import com.attendance.attendanceapp.domain.model.Course
import com.attendance.attendanceapp.domain.model.Department

import com.attendance.attendanceapp.ui.screens.auth.AuthDropdownField

@Composable
fun ManageAcademicDataScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Departments", "Courses")

    val departments by viewModel.departments.collectAsState()
    val courses by viewModel.courses.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog States
    var newDeptName by remember { mutableStateOf("") }
    var newCourseName by remember { mutableStateOf("") }
    var selectedDeptForCourse by remember { mutableStateOf("") }
    var deptExpanded by remember { mutableStateOf(false) }
    var selectedYearForCourse by remember { mutableStateOf("") }
    var selectedSemesterForCourse by remember { mutableStateOf("") }
    var yearExpanded by remember { mutableStateOf(false) }
    
    val departmentNames = departments.map { it.name }
    val years = listOf("1", "2", "3", "4", "5")
    
    val filteredDepartments = departments.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val filteredCourses = courses.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = { AttendifyTopBar(title = "Academic Management", onBackClick = onNavigateBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = schoolColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search academic data...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedBorderColor = schoolColor
                    ),
                    singleLine = true
                )
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = schoolColor,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
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
            
            if (selectedTab == 0) {
                DepartmentList(filteredDepartments, onDelete = { viewModel.deleteDepartment(it.id) })
            } else {
                CourseList(filteredCourses, departments, onDelete = { viewModel.deleteCourse(it.id) })
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(if (selectedTab == 0) "Add Department" else "Add Course") },
                text = {
                    Column {
                        if (selectedTab == 0) {
                            OutlinedTextField(
                                value = newDeptName,
                                onValueChange = { newDeptName = it },
                                label = { Text("Department Name") },
                                singleLine = true
                            )
                        } else {
                            OutlinedTextField(
                                value = newCourseName,
                                onValueChange = { newCourseName = it },
                                label = { Text("Course Name") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            AuthDropdownField(
                                label = "Select Department",
                                selectedOption = selectedDeptForCourse,
                                expanded = deptExpanded,
                                onExpandedChange = { deptExpanded = it },
                                options = departmentNames,
                                onOptionSelected = { selectedDeptForCourse = it },
                                leadingIcon = Icons.Default.Business
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            AuthDropdownField(
                                label = "Target Year",
                                selectedOption = selectedYearForCourse,
                                expanded = yearExpanded,
                                onExpandedChange = { yearExpanded = it },
                                options = years,
                                onOptionSelected = { selectedYearForCourse = it },
                                leadingIcon = Icons.Default.MenuBook
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            var semesterExpanded by remember { mutableStateOf(false) }
                            AuthDropdownField(
                                label = "Semester",
                                selectedOption = selectedSemesterForCourse,
                                expanded = semesterExpanded,
                                onExpandedChange = { semesterExpanded = it },
                                options = listOf("Semester 1", "Semester 2"),
                                onOptionSelected = { selectedSemesterForCourse = it },
                                leadingIcon = Icons.Default.Event
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (selectedTab == 0 && newDeptName.isNotBlank()) {
                            viewModel.createDepartment(newDeptName)
                            newDeptName = ""
                            showAddDialog = false
                        } else if (selectedTab == 1 && newCourseName.isNotBlank() && selectedDeptForCourse.isNotBlank()) {
                            val deptId = departments.find { it.name == selectedDeptForCourse }?.id ?: "0"
                            val semester = if (selectedSemesterForCourse == "Semester 1") "1" else "2"
                            viewModel.createCourse(newCourseName, deptId, selectedYearForCourse, semester)
                            newCourseName = ""
                            selectedDeptForCourse = ""
                            selectedYearForCourse = ""
                            selectedSemesterForCourse = ""
                            showAddDialog = false
                        }
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun DepartmentList(departments: List<Department>, onDelete: (Department) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(departments) { dept ->
            AcademicItemCard(
                title = dept.name, 
                subtitle = "Department", 
                icon = Icons.Default.Business,
                onDelete = { onDelete(dept) }
            )
        }
    }
}

@Composable
fun CourseList(courses: List<Course>, departments: List<Department>, onDelete: (Course) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(courses) { course ->
            val deptName = departments.find { it.id == course.departmentId }?.name ?: "Unknown Department"
            AcademicItemCard(
                title = course.name, 
                subtitle = "Dept: $deptName | Year: ${course.year}", 
                icon = Icons.Default.MenuBook,
                onDelete = { onDelete(course) }
            )
        }
    }
}

@Composable
fun AcademicItemCard(
    title: String, 
    subtitle: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0F2F1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF006064))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, color = Color.Gray, fontSize = 14.sp)
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.LightGray)
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { showMenu = false /* TODO: Implement Edit */ },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = { 
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}
