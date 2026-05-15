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
import com.attendance.attendanceapp.ui.screens.auth.AuthViewModel
import com.attendance.attendanceapp.ui.screens.auth.AuthState
import com.attendance.attendanceapp.domain.model.Role
import com.attendance.attendanceapp.ui.screens.auth.AuthTextField
import com.attendance.attendanceapp.ui.screens.common.AttendifyTopBar

@Composable
fun CreateTeacherScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    
    var teacherName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }

    val authState by viewModel.authState

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            // Teacher created successfully
            onNavigateBack()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(title = "Create Teacher", onBackClick = onNavigateBack)

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
                    text = "New Teacher Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Create login credentials for a new faculty member",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                )

                AuthTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = "Full Name",
                    placeholder = "Enter teacher's name",
                    leadingIcon = Icons.Outlined.Person
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-mail Address",
                    placeholder = "Enter official email",
                    leadingIcon = Icons.Outlined.Email
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = "Department",
                    placeholder = "e.g. Software Engineering",
                    leadingIcon = Icons.Outlined.AccountTree
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Default Password",
                    placeholder = "Create a temporary password",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(40.dp))

                if (authState is AuthState.Error) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        viewModel.signUp(
                            name = teacherName,
                            email = email,
                            password = password,
                            role = Role.teacher,
                            departmentId = department
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = schoolColor),
                    shape = RoundedCornerShape(16.dp),
                    enabled = teacherName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
