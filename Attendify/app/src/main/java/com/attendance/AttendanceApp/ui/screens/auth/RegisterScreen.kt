package com.attendance.attendanceapp.ui.screens.auth

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
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

    var yearExpanded by remember { mutableStateOf(false) }
    var deptExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }

    // Mock Data
    val years = listOf("Freshman (Year 1)", "Year 2", "Year 3", "Year 4", "Year 5")
    val departmentsMap = mapOf(
        "Freshman (Year 1)" to listOf("Engineering", "Social Sciences", "Medicine"),
        "Year 2" to listOf("Software Engineering", "Mechanical", "Electrical", "Civil"),
        "Year 3" to listOf("Software Engineering", "Mechanical", "Electrical", "Civil"),
        "Year 4" to listOf("Software Engineering", "Mechanical", "Electrical", "Civil"),
        "Year 5" to listOf("Software Engineering", "Mechanical", "Electrical", "Civil")
    )
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
                // Logo removed
                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Sign up",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Let's keep it quick, just a few steps and you're in",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
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
                    options = years,
                    onOptionSelected = {
                        selectedYear = it
                        selectedDept = "" // Reset Dept
                        selectedSection = "" // Reset Section
                    },
                    leadingIcon = Icons.Outlined.CalendarToday
                )

                if (selectedYear.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Dept Dropdown
                    val depts = departmentsMap[selectedYear] ?: emptyList()
                    AuthDropdownField(
                        label = "Department",
                        selectedOption = selectedDept,
                        expanded = deptExpanded,
                        onExpandedChange = { deptExpanded = it },
                        options = depts,
                        onOptionSelected = {
                            selectedDept = it
                            selectedSection = "" // Reset Section
                        },
                        leadingIcon = Icons.Outlined.School
                    )
                }

                if (selectedDept.isNotEmpty()) {
                    val sections = sectionsMap[selectedDept] ?: emptyList()
                    if (sections.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        // Section Dropdown
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

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = onRegisterSuccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006064)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Register", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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


