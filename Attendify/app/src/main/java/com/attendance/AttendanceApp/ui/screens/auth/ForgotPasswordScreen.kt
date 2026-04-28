package com.attendance.attendanceapp.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Help

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onGetOTP: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back")
            }
            Text(
                text = "Forgot",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp)) // To balance the back button
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Illustration Placeholder
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Using an icon as a placeholder for the illustration
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = Color.LightGray.copy(alpha = 0.5f)
                    )
                }

                Text(
                    text = "Forgot Password?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = "Don't worry! It happens. Please enter the email address associated with your account.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email Field
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Enter your email address",
                    placeholder = "Enter your email",
                    leadingIcon = Icons.Outlined.Email
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onGetOTP(email) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006064)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Get OTP", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
