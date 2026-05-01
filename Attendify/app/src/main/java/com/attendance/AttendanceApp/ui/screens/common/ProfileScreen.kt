package com.attendance.attendanceapp.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    name: String = "User Name",
    email: String = "user@gmail.com",
    role: String = "Student",
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val schoolColor = Color(0xFF006064)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(title = "Profile", onBackClick = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(schoolColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = schoolColor
                )
                // Edit Overlay
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(schoolColor)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = role, fontSize = 16.sp, color = schoolColor, fontWeight = FontWeight.Medium)
            Text(text = email, fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(40.dp))

            // Settings List
            ProfileOptionItem(Icons.Outlined.Person, "Edit Profile", schoolColor)
            ProfileOptionItem(Icons.Outlined.Notifications, "Notifications", schoolColor)
            ProfileOptionItem(Icons.Outlined.Security, "Security", schoolColor)
            ProfileOptionItem(Icons.Outlined.HelpOutline, "Help & Support", schoolColor)
            
            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(24.dp))

            // Danger Zone
            ProfileOptionItem(Icons.Default.Logout, "Logout", Color.Black, onClick = onLogout)
            ProfileOptionItem(Icons.Default.DeleteForever, "Delete Account", Color.Red)
        }
    }
}

@Composable
fun ProfileOptionItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = if(color == Color.Red) Color.Red else Color.Black)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
