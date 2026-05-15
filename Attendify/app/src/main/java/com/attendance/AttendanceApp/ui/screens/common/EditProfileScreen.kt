package com.attendance.attendanceapp.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.attendanceapp.domain.model.User
import com.attendance.attendanceapp.domain.model.Student
import com.attendance.attendanceapp.ui.screens.auth.AuthTextField

@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    user: User?,
    student: Student? = null,
    departmentName: String? = null,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    // Pre-fill data when user or student changes
    LaunchedEffect(user, student) {
        if (user != null) {
            name = user.name
        }
    }

    var email by remember { mutableStateOf(user?.email ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState
    val schoolColor = Color(0xFF006064)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(title = "Edit Profile", onBackClick = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture Section
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(schoolColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = schoolColor
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Personal Info Section
            Text(
                text = "Personal Information",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false, 
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.LightGray,
                    disabledLabelColor = Color.Gray,
                    disabledLeadingIconColor = Color.Gray
                )
            )

            if (student != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = student.year,
                        onValueChange = {},
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f),
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.LightGray)
                    )
                    OutlinedTextField(
                        value = student.semester,
                        onValueChange = {},
                        label = { Text("Semester") },
                        modifier = Modifier.weight(1f),
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.LightGray)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = departmentName ?: student.departmentId ?: "Not Assigned",
                    onValueChange = {},
                    label = { Text("Department") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Outlined.Business, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.LightGray)
                )

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(schoolColor.copy(alpha = 0.05f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = schoolColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Academic data is managed by Admin/Teacher. Please contact them for any changes.",
                        fontSize = 12.sp,
                        color = schoolColor.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Security Section
            Text(
                text = "Security",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            var currentPassword by remember { mutableStateOf("") }

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Existing Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                placeholder = { Text("Enter current password to make changes") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) }
            )

            if (uiState is ProfileUiState.Error) {
                Text(
                    text = (uiState as ProfileUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (uiState is ProfileUiState.Success) {
                Text(
                    text = (uiState as ProfileUiState.Success).message,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (currentPassword.isBlank() && (name != user?.name || newPassword.isNotBlank())) {
                        // Error: Password required
                        return@Button
                    }

                    if (name != user?.name) {
                        user?.let { viewModel.updateProfile(it.copy(name = name)) }
                    }
                    if (newPassword.isNotBlank()) {
                        if (newPassword == confirmPassword) {
                            viewModel.changePassword(currentPassword, newPassword)
                        } else {
                            // Show error
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = schoolColor),
                shape = RoundedCornerShape(16.dp),
                enabled = uiState !is ProfileUiState.Loading
            ) {
                if (uiState is ProfileUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
