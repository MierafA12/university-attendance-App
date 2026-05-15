package com.attendance.attendanceapp.ui.screens.common

import android.graphics.BitmapFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    val context = LocalContext.current
    val alpha = remember { Animatable(0f) }
    
    val schoolColor = Color(0xFF006064)
    
    // Loading logo from assets
    val logoBitmap = remember {
        try {
            context.assets.open("Attendify logo.png").use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }

    LaunchedEffect(key1 = true) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (logoBitmap != null) {
                Image(
                    bitmap = logoBitmap.asImageBitmap(),
                    contentDescription = "Attendify Logo",
                    modifier = Modifier
                        .size(180.dp)
                        .alpha(alpha.value)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .alpha(alpha.value),
                    tint = schoolColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Attendify",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = schoolColor,
                    modifier = Modifier.alpha(alpha.value)
                )
            }
        }
    }
}
