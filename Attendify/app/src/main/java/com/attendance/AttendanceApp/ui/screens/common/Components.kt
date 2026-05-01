package com.attendance.attendanceapp.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AttendifyTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back")
            }
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            textAlign = if (onBackClick != null) TextAlign.Center else TextAlign.Start,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        if (onBackClick != null) {
            Box(modifier = Modifier.width(48.dp)) {
                actions()
            }
        } else {
            actions()
        }
    }
}

@Composable
fun AttendifySurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            content = content
        )
    }
}

@Composable
fun AttendifyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFF006064),
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled
    ) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AttendifyCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = content
    )
}
@Composable
fun AttendifyBottomNav(
    selectedRoute: String,
    onRouteSelected: (String) -> Unit
) {
    val schoolColor = Color(0xFF006064)
    val lightTeal = Color(0xFFE0F2F1)

    NavigationBar(
        containerColor = Color.White,
        contentColor = schoolColor
    ) {
        NavigationBarItem(
            selected = selectedRoute == "home",
            onClick = { onRouteSelected("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = schoolColor,
                selectedTextColor = schoolColor,
                indicatorColor = lightTeal
            )
        )
        NavigationBarItem(
            selected = selectedRoute == "attendance",
            onClick = { onRouteSelected("attendance") },
            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Attendance") },
            label = { Text("Attendance") }
        )
        NavigationBarItem(
            selected = selectedRoute == "reports",
            onClick = { onRouteSelected("reports") },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Reports") },
            label = { Text("Reports") }
        )
        NavigationBarItem(
            selected = selectedRoute == "profile",
            onClick = { onRouteSelected("profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
