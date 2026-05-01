package com.attendance.attendanceapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.attendanceapp.ui.screens.common.AttendifyTopBar

@Composable
fun ManageUsersScreen(
    onNavigateBack: () -> Unit,
    onCreateTeacher: () -> Unit
) {
    val schoolColor = Color(0xFF006064)
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf("Students", "Teachers")

    // Mock Data
    val students = listOf(
        User("Aman", "ETS0001/12", "Software Eng."),
        User("Sarah", "ETS0002/12", "Database Sys."),
        User("John", "ETS0003/12", "Mobile App"),
        User("Elena", "ETS0004/12", "Networking")
    )
    val teachers = listOf(
        User("Dr. Robert", "ID-101", "Professor"),
        User("Ms. Clara", "ID-102", "Assistant Professor"),
        User("Mr. David", "ID-103", "Lecturer")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        AttendifyTopBar(title = "Manage Users", onBackClick = onNavigateBack)

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search users...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                    focusedBorderColor = schoolColor
                ),
                singleLine = true,
                trailingIcon = {
                    if (selectedTab == 1) { // Teachers tab
                        IconButton(onClick = onCreateTeacher) {
                            Icon(Icons.Default.Add, contentDescription = "Add Teacher", tint = schoolColor)
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = schoolColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = schoolColor
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            color = Color.White
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val currentList = if (selectedTab == 0) students else teachers
                items(currentList) { user ->
                    UserListItem(user, schoolColor)
                }
            }
        }
    }
}

data class User(val name: String, val id: String, val dept: String)

@Composable
fun UserListItem(user: User, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.first().toString(),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${user.id} • ${user.dept}", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = { /* Remove Logic */ }) {
                Icon(
                    imageVector = if (user.id.startsWith("ETS")) Icons.Default.Delete else Icons.Default.MoreVert, 
                    contentDescription = "Remove", 
                    tint = if (user.id.startsWith("ETS")) Color.Red.copy(alpha = 0.6f) else Color.Gray
                )
            }
        }
    }
}
