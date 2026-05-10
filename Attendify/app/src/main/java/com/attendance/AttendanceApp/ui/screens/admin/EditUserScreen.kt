package com.attendance.attendanceapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.attendanceapp.domain.model.*
import com.attendance.attendanceapp.ui.screens.auth.AuthDropdownField
import com.attendance.attendanceapp.ui.screens.common.AttendifyTopBar

@Composable
fun EditUserScreen(
    viewModel: AdminViewModel,
    userId: String,
    onNavigateBack: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    val students by viewModel.students.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val departments by viewModel.departments.collectAsState()
    
    val user = (students + teachers).find { it.id == userId }
    
    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var status by remember { mutableStateOf(user.status) }
    
    // Student specific
    var deptId by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }
    
    // Teacher specific
    var specialization by remember { mutableStateOf("") }
    
    val studentDetails by viewModel.getStudentDetails(userId).collectAsState(initial = null)
    val teacherDetails by viewModel.getTeacherDetails(userId).collectAsState(initial = null)
    
    LaunchedEffect(studentDetails) {
        studentDetails?.let {
            deptId = it.departmentId ?: ""
            year = it.year
            semester = it.semester
        }
    }
    
    LaunchedEffect(teacherDetails) {
        teacherDetails?.let {
            deptId = it.departmentId ?: ""
            specialization = it.specialization
        }
    }

    val uiState by viewModel.uiState
    
    LaunchedEffect(uiState) {
        if (uiState is AdminUiState.Success) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = { AttendifyTopBar(title = "Edit User", onBackClick = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "Personal Information", fontWeight = FontWeight.Bold, color = schoolColor, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = false // Usually email shouldn't be changed after creation
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    var statusExpanded by remember { mutableStateOf(false) }
                    AuthDropdownField(
                        label = "User Status",
                        selectedOption = status.name.capitalize(),
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = it },
                        options = UserStatus.values().map { it.name.capitalize() },
                        onOptionSelected = { status = UserStatus.valueOf(it.lowercase()) },
                        leadingIcon = Icons.Default.VerifiedUser
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (user.role == Role.student) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(text = "Academic Details", fontWeight = FontWeight.Bold, color = schoolColor, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        var deptExpanded by remember { mutableStateOf(false) }
                        AuthDropdownField(
                            label = "Department",
                            selectedOption = departments.find { it.id == deptId }?.name ?: "Select Department",
                            expanded = deptExpanded,
                            onExpandedChange = { deptExpanded = it },
                            options = departments.map { it.name },
                            onOptionSelected = { deptName ->
                                deptId = departments.find { it.name == deptName }?.id ?: ""
                            },
                            leadingIcon = Icons.Default.Business
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        var yearExpanded by remember { mutableStateOf(false) }
                        AuthDropdownField(
                            label = "Year",
                            selectedOption = year,
                            expanded = yearExpanded,
                            onExpandedChange = { yearExpanded = it },
                            options = listOf("1", "2", "3", "4", "5"),
                            onOptionSelected = { year = it },
                            leadingIcon = Icons.Default.School
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        var semExpanded by remember { mutableStateOf(false) }
                        AuthDropdownField(
                            label = "Semester",
                            selectedOption = if (semester == "1") "Semester 1" else if (semester == "2") "Semester 2" else "Select Semester",
                            expanded = semExpanded,
                            onExpandedChange = { semExpanded = it },
                            options = listOf("Semester 1", "Semester 2"),
                            onOptionSelected = { semester = if (it == "Semester 1") "1" else "2" },
                            leadingIcon = Icons.Default.Event
                        )
                    }
                }
            } else if (user.role == Role.teacher) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(text = "Employment Details", fontWeight = FontWeight.Bold, color = schoolColor, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        var deptExpanded by remember { mutableStateOf(false) }
                        AuthDropdownField(
                            label = "Department",
                            selectedOption = departments.find { it.id == deptId }?.name ?: "Select Department",
                            expanded = deptExpanded,
                            onExpandedChange = { deptExpanded = it },
                            options = departments.map { it.name },
                            onOptionSelected = { deptName ->
                                deptId = departments.find { it.name == deptName }?.id ?: ""
                            },
                            leadingIcon = Icons.Default.Business
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = specialization,
                            onValueChange = { specialization = it },
                            label = { Text("Specialization") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    val updatedUser = user.copy(name = name, status = status)
                    val updatedStudent = if (user.role == Role.student) {
                        Student(
                            studentId = studentDetails?.studentId ?: "",
                            userId = userId,
                            departmentId = deptId,
                            year = year,
                            semester = semester
                        )
                    } else null
                    
                    val updatedTeacher = if (user.role == Role.teacher) {
                        Teacher(
                            teacherId = teacherDetails?.teacherId ?: "",
                            userId = userId,
                            departmentId = deptId,
                            specialization = specialization
                        )
                    } else null
                    
                    viewModel.updateUserDetails(updatedUser, updatedStudent, updatedTeacher)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = schoolColor)
            ) {
                if (uiState is AdminUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
            
            if (uiState is AdminUiState.Error) {
                Text(
                    text = (uiState as AdminUiState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
