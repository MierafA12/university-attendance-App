package com.attendance.attendanceapp.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.graphics.BitmapFactory

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit, // Changed to pass role
    onNavigateToRegister: () -> Unit,
    onNavigateToForgot: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val authState by viewModel.authState
    val context = LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            val user = (authState as AuthState.Authenticated).user
            onLoginSuccess(user.role.name)
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .statusBarsPadding()
    ) {
        // Back Button
        IconButton(
            onClick = { /* Handle back */ },
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
                    .verticalScroll(rememberScrollState()),
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
                            .size(80.dp)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Fit
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Login",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Please login to your account",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email Field
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "E-mail Address",
                    placeholder = "Enter your email",
                    leadingIcon = Icons.Outlined.Email
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Password Field
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "Enter your password",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordToggle = { passwordVisible = !passwordVisible }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color.Black)
                        )
                        Text(text = "Remember me", fontSize = 14.sp, color = Color.Gray)
                    }
                    Text(
                        text = "Forgot password?",
                        fontSize = 14.sp,
                        color = Color(0xFF006064),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToForgot() }
                    )
                }

                if (authState is AuthState.Error) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            viewModel.login(email.trim(), password.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = authState !is AuthState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006064)), // School brand color
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Don't have account? ", color = Color.Gray)
                    Text(
                        text = "Sign up",
                        color = Color(0xFF006064),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}
