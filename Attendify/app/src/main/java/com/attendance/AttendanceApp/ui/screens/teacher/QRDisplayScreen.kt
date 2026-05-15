package com.attendance.attendanceapp.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.attendance.attendanceapp.ui.screens.common.AttendifyTopBar
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun QRDisplayScreen(
    viewModel: TeacherViewModel,
    sessionId: String,
    onClose: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    val activeSession by viewModel.activeSession.collectAsState()
    val studentCount by viewModel.activeSessionAttendanceCount.collectAsState()
    
    // Use remember to keep the session data even after it becomes inactive
    var lastKnownSession by remember { mutableStateOf<com.attendance.attendanceapp.domain.model.Session?>(null) }
    
    LaunchedEffect(activeSession) {
        activeSession?.let { 
            if (it.id == sessionId) {
                lastKnownSession = it
            }
        }
    }
    
    // If we have no active session matching sessionId, but we have a last known one, use that.
    // Otherwise, we'll try to find it from the repository if it's already ended.
    val currentSession = lastKnownSession ?: activeSession?.takeIf { it.id == sessionId }
    
    var timeLeft by remember { mutableStateOf("00:00") }
    var isTimerFinished by remember { mutableStateOf(false) }
    
    LaunchedEffect(currentSession) {
        if (currentSession != null) {
            val sessionStartTime = currentSession.date
            val sessionDurationMillis = currentSession.durationMinutes * 60 * 1000L
            
            while (!isTimerFinished) {
                val currentTime = System.currentTimeMillis()
                val elapsedMillis = currentTime - sessionStartTime
                val remainingMillis = sessionDurationMillis - elapsedMillis
                
                if (remainingMillis <= 0) {
                    timeLeft = "00:00"
                    isTimerFinished = true
                    // Automatically stop the session when time is up
                    viewModel.stopSession(currentSession.id)
                    break
                }
                
                val minutes = (remainingMillis / 1000) / 60
                val seconds = (remainingMillis / 1000) % 60
                timeLeft = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
                
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val sessionDate = currentSession?.let { dateFormatter.format(Date(it.date)) } ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(title = "Attendance Session", onBackClick = onClose)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isTimerFinished) "Session Finished" else "Session Active",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isTimerFinished) Color.Red else Color.Black
            )
            Text(
                text = sessionDate,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.size(300.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (currentSession != null) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Code",
                            modifier = Modifier.size(150.dp),
                            tint = if (isTimerFinished) Color.Gray else schoolColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Code: ${currentSession.qrCode}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isTimerFinished) Color.Gray else schoolColor,
                            letterSpacing = 4.sp
                        )
                        Text(
                            text = if (isTimerFinished) "Time's up!" else "Scan or enter code manually",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    } else {
                        CircularProgressIndicator(color = schoolColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading session...", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SessionStatItem("Students Joined", studentCount.toString(), Icons.Default.Groups, schoolColor)
                SessionStatItem("Time Remaining", timeLeft, Icons.Default.Timer, if (isTimerFinished) Color.Gray else Color.Red)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    currentSession?.let { viewModel.stopSession(it.id) }
                    onClose() 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isTimerFinished) Color.Gray else schoolColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (isTimerFinished) "Close" else "End Session", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            if (!isTimerFinished) {
                TextButton(
                    onClick = { /* Refresh QR */ },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh QR Code", color = schoolColor)
                }
            }
        }
    }
}

@Composable
fun SessionStatItem(label: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}
