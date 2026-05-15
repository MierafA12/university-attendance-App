package com.attendance.attendanceapp.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.graphics.BitmapFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Dropdown States
    var selectedYear by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf("") }
    var selectedSemester by remember { mutableStateOf("") }

    var yearExpanded by remember { mutableStateOf(false) }
    var deptExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }
    var semesterExpanded by remember { mutableStateOf(false) }

    val authState by viewModel.authState
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onRegisterSuccess()
            viewModel.resetState()
        }
    }

    val departments by viewModel.departments.collectAsState()
    val departmentsList = departments.map { it.name }
    
    // Mock Section Data (Could be fetched from repo too)
    val sectionsMap = mapOf(
        "Software Engineering" to listOf("Section 1", "Section 2", "Section 3"),
        "Civil" to listOf("Section A", "Section B"),
        "Electrical" to listOf("Sec 1", "Sec 2")
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .statusBarsPadding()
    ) {
        // Back Button
        IconButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back")
        }

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
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.Start
            ) {
                val logoBitmap = remember {
                    try {
                        context.assets.open("Attendify logo.png").use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

                if (logoBitmap != null) {
                    Image(
                        bitmap = logoBitmap.asImageBitmap(),
                        contentDescription = "Attendify Logo",
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sign up",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Let's keep it quick, just a few steps and you're in",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Student Name
                AuthTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = "Full Name",
                    placeholder = "Enter your full name",
                    leadingIcon = Icons.Outlined.Person
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Student ID
                AuthTextField(
                    value = studentId,
                    onValueChange = { studentId = it },
                    label = "Student ID",
                    placeholder = "e.g. ETS0000/12",
                    leadingIcon = Icons.Outlined.Badge
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-mail Address",
                    placeholder = "Enter your email",
                    leadingIcon = Icons.Outlined.Email
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "Create a password",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordToggle = { passwordVisible = !passwordVisible }
                )

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(24.dp))

                // Academic Info Section
                Text(
                    text = "Academic Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Year Dropdown
                AuthDropdownField(
                    label = "Year",
                    selectedOption = selectedYear,
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = it },
                    options = listOf("1", "2", "3", "4", "5"),
                    onOptionSelected = {
                        selectedYear = it
                    },
                    leadingIcon = Icons.Outlined.CalendarToday
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dept Dropdown
                AuthDropdownField(
                    label = "Department",
                    selectedOption = selectedDept,
                    expanded = deptExpanded,
                    onExpandedChange = { deptExpanded = it },
                    options = departmentsList,
                    onOptionSelected = {
                        selectedDept = it
                        selectedSection = "" // Reset Section
                    },
                    leadingIcon = Icons.Outlined.School
                )

                if (selectedDept.isNotEmpty()) {
                    val sections = sectionsMap[selectedDept] ?: emptyList()
                    if (sections.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        AuthDropdownField(
                            label = "Section",
                            selectedOption = selectedSection,
                            expanded = sectionExpanded,
                            onExpandedChange = { sectionExpanded = it },
                            options = sections,
                            onOptionSelected = { selectedSection = it },
                            leadingIcon = Icons.Outlined.Groups
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Semester Dropdown
                AuthDropdownField(
                    label = "Semester",
                    selectedOption = selectedSemester,
                    expanded = semesterExpanded,
                    onExpandedChange = { semesterExpanded = it },
                    options = listOf("Semester 1", "Semester 2"),
                    onOptionSelected = { selectedSemester = it },
                    leadingIcon = Icons.Outlined.Event
                )

                if (authState is AuthState.Error) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        if (studentName.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                            val semester = if (selectedSemester == "Semester 2") "2" else "1"
                            val deptId = departments.find { it.name == selectedDept }?.id ?: selectedDept
                            
                            viewModel.signUp(
                                name = studentName.trim(),
                                email = email.trim(),
                                password = password.trim(),
                                role = com.attendance.attendanceapp.domain.model.Role.student,
                                studentId = studentId.trim(),
                                departmentId = deptId,
                                year = selectedYear,
                                semester = semester
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = authState !is AuthState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006064)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Register", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Already have an account? ", color = Color.Gray)
                    Text(
                        text = "Login",
                        color = Color(0xFF006064),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}


