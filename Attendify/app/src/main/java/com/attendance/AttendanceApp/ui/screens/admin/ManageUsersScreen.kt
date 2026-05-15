package com.attendance.attendanceapp.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.attendance.attendanceapp.domain.model.*

@Composable
fun ManageUsersScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    onCreateTeacher: () -> Unit,
    onEditUser: (String) -> Unit
) {
    val schoolColor = Color(0xFF006064)
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    
    var selectedUserForDetail by remember { mutableStateOf<User?>(null) }
    
    val tabs = listOf("Students", "Teachers")

    val students by viewModel.students.collectAsState()
    val teachers by viewModel.teachers.collectAsState()

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
                val filteredList = (if (selectedTab == 0) students else teachers).filter {
                    it.name.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true)
                }
                
                items(filteredList) { user ->
                    UserListItem(
                        user = user, 
                        color = schoolColor,
                        onApprove = { viewModel.approveUser(user) },
                        onDelete = { viewModel.deleteUser(user) },
                        onToggleStatus = { 
                            val newStatus = if (user.status == com.attendance.attendanceapp.domain.model.UserStatus.active) 
                                com.attendance.attendanceapp.domain.model.UserStatus.inactive 
                            else com.attendance.attendanceapp.domain.model.UserStatus.active
                            viewModel.updateUserStatus(user, newStatus)
                        },
                        onEdit = { onEditUser(user.id) },
                        onClick = { selectedUserForDetail = user }
                    )
                }
            }
        }
    }

    if (selectedUserForDetail != null) {
        UserDetailDialog(
            user = selectedUserForDetail!!,
            viewModel = viewModel,
            onDismiss = { selectedUserForDetail = null }
        )
    }
}

@Composable
fun UserDetailDialog(
    user: User,
    viewModel: AdminViewModel,
    onDismiss: () -> Unit
) {
    val studentDetails by viewModel.getStudentDetails(user.id).collectAsState(initial = null)
    val teacherDetails by viewModel.getTeacherDetails(user.id).collectAsState(initial = null)
    val departments by viewModel.departments.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = user.name, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                DetailRow(label = "Email", value = user.email)
                DetailRow(label = "Role", value = user.role.name.capitalize())
                DetailRow(label = "Status", value = user.status.name.capitalize())
                
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                if (user.role == Role.student && studentDetails != null) {
                    val deptName = departments.find { it.id == studentDetails?.departmentId }?.name ?: "Unknown"
                    DetailRow(label = "Department", value = deptName)
                    DetailRow(label = "Year", value = studentDetails?.year ?: "-")
                    DetailRow(label = "Semester", value = studentDetails?.semester ?: "-")
                } else if (user.role == Role.teacher && teacherDetails != null) {
                    val deptName = departments.find { it.id == teacherDetails?.departmentId }?.name ?: "Unknown"
                    DetailRow(label = "Department", value = deptName)
                    DetailRow(label = "Specialization", value = teacherDetails?.specialization ?: "-")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "$label: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
        Text(text = value)
    }
}

@Composable
fun UserListItem(
    user: User, 
    color: Color, 
    onApprove: () -> Unit = {},
    onDelete: () -> Unit = {},
    onToggleStatus: () -> Unit = {},
    onEdit: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val isPending = user.status == com.attendance.attendanceapp.domain.model.UserStatus.pending
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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
                    text = if (user.name.isNotEmpty()) user.name.first().toString() else "?",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name.ifEmpty { "Unknown" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${user.email} • ${user.role.name.capitalize()}", fontSize = 12.sp, color = Color.Gray)
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when(user.status) {
                        com.attendance.attendanceapp.domain.model.UserStatus.active -> Color(0xFFE8F5E9)
                        com.attendance.attendanceapp.domain.model.UserStatus.pending -> Color(0xFFFFF3E0)
                        else -> Color(0xFFFFEBEE)
                    },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = user.status.name.capitalize(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = when(user.status) {
                            com.attendance.attendanceapp.domain.model.UserStatus.active -> Color(0xFF2E7D32)
                            com.attendance.attendanceapp.domain.model.UserStatus.pending -> Color(0xFFE65100)
                            else -> Color(0xFFC62828)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Box {
                if (isPending && user.role == com.attendance.attendanceapp.domain.model.Role.student) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Approve", fontSize = 12.sp)
                    }
                } else {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert, 
                            contentDescription = "Options", 
                            tint = Color.Gray
                        )
                    }
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { 
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { 
                            Text(if (user.status == com.attendance.attendanceapp.domain.model.UserStatus.active) "Set Inactive" else "Set Active") 
                        },
                        onClick = { 
                            showMenu = false
                            onToggleStatus()
                        },
                        leadingIcon = { 
                            Icon(
                                if (user.status == com.attendance.attendanceapp.domain.model.UserStatus.active) Icons.Default.Block else Icons.Default.CheckCircle, 
                                contentDescription = null
                            ) 
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = { 
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}
