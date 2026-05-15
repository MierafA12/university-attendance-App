package com.attendance.attendanceapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import com.attendance.attendanceapp.ui.screens.common.SplashScreen
import com.attendance.attendanceapp.ui.screens.onboarding.OnboardingScreen
import com.attendance.attendanceapp.ui.screens.auth.LoginScreen
import com.attendance.attendanceapp.ui.screens.auth.RegisterScreen
import com.attendance.attendanceapp.ui.screens.auth.ForgotPasswordScreen
import com.attendance.attendanceapp.ui.screens.auth.VerifyScreen
import com.attendance.attendanceapp.ui.screens.auth.PendingApprovalScreen
import com.attendance.attendanceapp.ui.screens.student.StudentDashboard
import com.attendance.attendanceapp.ui.screens.teacher.TeacherDashboard
import com.attendance.attendanceapp.ui.screens.admin.AdminDashboard
import com.attendance.attendanceapp.ui.screens.common.ProfileScreen
import com.attendance.attendanceapp.ui.screens.common.EditProfileScreen
import com.attendance.attendanceapp.ui.screens.common.ProfileViewModel
import com.attendance.attendanceapp.ui.screens.common.NotificationScreen
import com.attendance.attendanceapp.ui.screens.common.NotificationViewModel
import com.attendance.attendanceapp.ui.screens.student.QRScannerScreen
import com.attendance.attendanceapp.ui.screens.student.StudentCourseSchedulesScreen
import com.attendance.attendanceapp.ui.screens.student.AttendanceHistoryScreen
import com.attendance.attendanceapp.ui.screens.teacher.StartSessionScreen
import com.attendance.attendanceapp.ui.screens.teacher.TeacherReportsScreen
import com.attendance.attendanceapp.ui.screens.admin.ManageUsersScreen
import com.attendance.attendanceapp.ui.screens.admin.ReportsScreen as AdminReports
import com.attendance.attendanceapp.ui.screens.admin.ScheduleManagementScreen
import com.attendance.attendanceapp.ui.screens.admin.CreateTeacherScreen
import com.attendance.attendanceapp.ui.screens.admin.ManageAcademicDataScreen
import com.attendance.attendanceapp.ui.screens.admin.EditUserScreen
import com.attendance.attendanceapp.App
import com.attendance.attendanceapp.ui.ViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.attendance.attendanceapp.ui.screens.auth.AuthViewModel
import com.attendance.attendanceapp.ui.screens.auth.AuthState
import com.attendance.attendanceapp.data.local.db.FirebaseManager
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Text
import com.attendance.attendanceapp.ui.screens.teacher.TeacherViewModel
import com.attendance.attendanceapp.ui.screens.teacher.QRDisplayScreen
import com.attendance.attendanceapp.ui.screens.teacher.TeacherReportsScreen
import com.attendance.attendanceapp.ui.screens.teacher.AttendanceListScreen
import com.attendance.attendanceapp.ui.screens.teacher.TeacherSchedulesScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as? App
    
    // Use remember to keep factory and ViewModels stable across recompositions
    val factory = remember(app) {
        app?.let {
            try {
                ViewModelFactory(
                    userRepository = it.userRepository,
                    academicRepository = it.academicRepository,
                    attendanceRepository = it.attendanceRepository,
                    notificationRepository = it.notificationRepository
                )
            } catch (e: Exception) {
                android.util.Log.e("NavGraph", "Failed to create ViewModelFactory", e)
                null
            }
        }
    }

    // Global ViewModels for Authentication and Notifications
    val authViewModel: AuthViewModel? = factory?.let { viewModel(factory = it) }
    val notificationViewModel: NotificationViewModel? = factory?.let { viewModel(factory = it) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    if (factory == null) {
                        // If factory is null, something went wrong with DB or Firebase initialization
                        navController.navigate(Screen.Login.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                        return@SplashScreen
                    }
                    try {
                        val firebaseUser = FirebaseManager.currentUser
                        if (firebaseUser != null) {
                            FirebaseManager.usersCollection.document(firebaseUser.uid).get().addOnSuccessListener { doc ->
                                try {
                                    val role = doc?.getString("role")?.lowercase() ?: "student"
                                    val status = doc?.getString("status")?.lowercase() ?: "pending"
                                    if (status == "active" || role == "admin") {
                                        val dest = when (role) {
                                            "admin" -> "admin_graph"
                                            "teacher" -> "teacher_graph"
                                            else -> "student_graph"
                                        }
                                        navController.navigate(dest) { popUpTo(Screen.Splash.route) { inclusive = true } }
                                    } else {
                                        navController.navigate(if (status == "pending") "pending_approval" else Screen.Login.route) {
                                            popUpTo(Screen.Splash.route) { inclusive = true }
                                        }
                                    }
                                } catch (e: Exception) {
                                    navController.navigate(Screen.Login.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                                }
                            }.addOnFailureListener {
                                navController.navigate(Screen.Login.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                            }
                        } else {
                            navController.navigate(Screen.Onboarding.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                        }
                    } catch (e: Exception) {
                        navController.navigate(Screen.Login.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinished = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } } })
        }
        
        composable(Screen.Login.route) {
            if (authViewModel != null) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { role ->
                        val dest = when (role) {
                            "admin" -> "admin_graph"
                            "teacher" -> "teacher_graph"
                            else -> "student_graph"
                        }
                        navController.navigate(dest) { popUpTo(Screen.Login.route) { inclusive = true } }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgot = { navController.navigate(Screen.ForgotPassword.route) }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Initialization Error. Please restart the app.")
                }
            }
        }

        composable("pending_approval") {
            PendingApprovalScreen(onLogout = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
            })
        }

        composable(Screen.Register.route) {
            authViewModel?.let { vm ->
                RegisterScreen(viewModel = vm, onRegisterSuccess = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Register.route) { inclusive = true } } }, onNavigateToLogin = { navController.popBackStack() })
            }
        }

        composable(Screen.ForgotPassword.route) {
            authViewModel?.let { vm -> ForgotPasswordScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() }) }
        }

        // ─── STUDENT GRAPH ──────────────────────────────────────────────────
        navigation(startDestination = Screen.StudentDashboard.route, route = "student_graph") {
            composable(Screen.StudentDashboard.route) { entry ->
                if (factory != null && notificationViewModel != null) {
                    val vm: com.attendance.attendanceapp.ui.screens.student.StudentViewModel = viewModel(viewModelStoreOwner = entry, factory = factory)
                    StudentDashboard(
                        viewModel = vm,
                        notificationViewModel = notificationViewModel,
                        onNavigateToScan = { navController.navigate("scan") },
                        onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onViewCourses = { navController.navigate("student_schedules") },
                        onViewHistory = { navController.navigate("student_history") },
                        onViewReports = { navController.navigate("attendance_reports") },
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screen.Login.route) { popUpTo(0) }
                        }
                    )
                }
            }
            composable("scan") { backStackEntry ->
                val parent = try { navController.getBackStackEntry("student_graph") } catch(e: Exception) { backStackEntry }
                if (factory != null) {
                    val vm: com.attendance.attendanceapp.ui.screens.student.StudentViewModel = viewModel(parent, factory = factory)
                    QRScannerScreen(viewModel = vm, onClose = { navController.popBackStack() }) 
                }
            }
            composable("student_schedules") { backStackEntry ->
                val parent = try { navController.getBackStackEntry("student_graph") } catch(e: Exception) { backStackEntry }
                if (factory != null) {
                    val vm: com.attendance.attendanceapp.ui.screens.student.StudentViewModel = viewModel(parent, factory = factory)
                    StudentCourseSchedulesScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
                }
            }
            composable("student_history") { backStackEntry ->
                val parent = try { navController.getBackStackEntry("student_graph") } catch(e: Exception) { backStackEntry }
                if (factory != null) {
                    val vm: com.attendance.attendanceapp.ui.screens.student.StudentViewModel = viewModel(parent, factory = factory)
                    AttendanceHistoryScreen(viewModel = vm, initialTab = 1, onNavigateBack = { navController.popBackStack() })
                }
            }
            composable("attendance_reports") { backStackEntry ->
                val parent = try { navController.getBackStackEntry("student_graph") } catch(e: Exception) { backStackEntry }
                if (factory != null) {
                    val vm: com.attendance.attendanceapp.ui.screens.student.StudentViewModel = viewModel(parent, factory = factory)
                    AttendanceHistoryScreen(viewModel = vm, initialTab = 0, onNavigateBack = { navController.popBackStack() })
                }
            }
        }

        // ─── TEACHER GRAPH ──────────────────────────────────────────────────
        navigation(startDestination = Screen.TeacherDashboard.route, route = "teacher_graph") {
            composable(Screen.TeacherDashboard.route) { entry ->
                if (factory != null && notificationViewModel != null) {
                    val vm: TeacherViewModel = viewModel(viewModelStoreOwner = entry, factory = factory)
                    TeacherDashboard(
                        viewModel = vm,
                        notificationViewModel = notificationViewModel,
                        onStartSession = { navController.navigate("start_session") },
                        onViewLiveSession = { id -> navController.navigate("qr_display/$id") },
                        onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                        onViewReports = { navController.navigate("teacher_reports") },
                        onNavigateToSchedules = { navController.navigate("teacher_schedules") },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                        }
                    )
                } else {
                    Text("Initializing...")
                }
            }
            composable("start_session") { backStackEntry ->
                val parent = try { navController.getBackStackEntry("teacher_graph") } catch(e: Exception) { backStackEntry }
                if (factory != null) {
                    val vm: TeacherViewModel = viewModel(parent, factory = factory)
                    StartSessionScreen(
                        viewModel = vm, 
                        onNavigateBack = { navController.popBackStack() }, 
                        onSessionStarted = { id -> 
                            navController.navigate("qr_display/$id") { 
                                popUpTo("start_session") { inclusive = true } 
                            } 
                        }
                    )
                }
            }
            composable("qr_display/{sessionId}") { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                val parent = try {
                    navController.getBackStackEntry("teacher_graph")
                } catch (e: Exception) {
                    backStackEntry
                }
                
                if (factory != null) {
                    val vm: TeacherViewModel = viewModel(parent, factory = factory)
                    QRDisplayScreen(
                        viewModel = vm,
                        sessionId = sessionId,
                        onClose = { navController.popBackStack() }
                    )
                } else {
                    Text("Error: ViewModel factory not initialized")
                }
            }
            composable("teacher_reports") { backStackEntry ->
                val parent = try { navController.getBackStackEntry("teacher_graph") } catch(e: Exception) { backStackEntry }
                if (factory != null) {
                    val vm: TeacherViewModel = viewModel(parent, factory = factory)
                    TeacherReportsScreen(viewModel = vm, onNavigateToAttendance = { id -> navController.navigate("attendance_list/$id") }, onNavigateBack = { navController.popBackStack() })
                }
            }
            composable("teacher_schedules") { backStackEntry ->
                val parent = try { navController.getBackStackEntry("teacher_graph") } catch(e: Exception) { backStackEntry }
                if (factory != null) {
                    val vm: TeacherViewModel = viewModel(parent, factory = factory)
                    TeacherSchedulesScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
                }
            }
        }

        // ─── ADMIN GRAPH ────────────────────────────────────────────────────
        navigation(startDestination = Screen.AdminDashboard.route, route = "admin_graph") {
            composable(Screen.AdminDashboard.route) { entry ->
                if (factory != null && notificationViewModel != null) {
                    val vm: com.attendance.attendanceapp.ui.screens.admin.AdminViewModel = viewModel(viewModelStoreOwner = entry, factory = factory)
                    AdminDashboard(
                        viewModel = vm,
                        notificationViewModel = notificationViewModel,
                        onManageUsers = { navController.navigate("manage_users") },
                        onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                        onViewReports = { navController.navigate("admin_reports") },
                        onAssignTeachers = { },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onManageSchedule = { navController.navigate(Screen.ScheduleManagement.route) },
                        onManageAcademic = { navController.navigate(Screen.AcademicManagement.route) },
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screen.Login.route) { popUpTo(0) }
                        }
                    )
                }
            }
            composable("manage_users") { backStackEntry ->
                val parent = try { navController.getBackStackEntry("admin_graph") } catch(e: Exception) { backStackEntry }
                if (factory != null) {
                    val vm: com.attendance.attendanceapp.ui.screens.admin.AdminViewModel = viewModel(parent, factory = factory)
                    ManageUsersScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() }, onCreateTeacher = { navController.navigate(Screen.CreateTeacher.route) }, onEditUser = { id -> navController.navigate(Screen.EditUser.createRoute(id)) })
                }
            }
            composable("admin_reports") { backStackEntry ->
                val parent = try { navController.getBackStackEntry("admin_graph") } catch(e: Exception) { backStackEntry }
                if (factory != null) {
                    val vm: com.attendance.attendanceapp.ui.screens.admin.AdminViewModel = viewModel(parent, factory = factory)
                    AdminReports(viewModel = vm, onNavigateBack = { navController.popBackStack() })
                }
            }
        }

        // Common screens
        composable(Screen.Notifications.route) {
            notificationViewModel?.let { vm -> NotificationScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() }) }
        }
        
        composable(Screen.Profile.route) {
            // Re-fetch correct role-based VM if needed, or use a shared one for Profile
            if (factory != null) {
                val vm: com.attendance.attendanceapp.ui.screens.admin.AdminViewModel = viewModel(factory = factory)
                val user by vm.currentUser.collectAsState()
                ProfileScreen(user = user, onEditProfile = { navController.navigate(Screen.EditProfile.route) }, onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }, onLogout = { FirebaseAuth.getInstance().signOut(); navController.navigate(Screen.Login.route) { popUpTo(0) } }, onNavigateBack = { navController.popBackStack() })
            }
        }
        
        composable(Screen.EditProfile.route) {
            if (factory != null) {
                val pVm: ProfileViewModel = viewModel(factory = factory)
                val aVm: com.attendance.attendanceapp.ui.screens.admin.AdminViewModel = viewModel(factory = factory)
                val user by aVm.currentUser.collectAsState()
                val student by if (user?.role == com.attendance.attendanceapp.domain.model.Role.student) aVm.getStudentDetails(user!!.id).collectAsState(null) else remember { androidx.compose.runtime.mutableStateOf(null) }
                val depts by aVm.departments.collectAsState()
                val deptName = depts.find { it.id == student?.departmentId }?.name
                EditProfileScreen(viewModel = pVm, user = user, student = student, departmentName = deptName, onNavigateBack = { navController.popBackStack() })
            }
        }

        composable(Screen.ScheduleManagement.route) {
            if (factory != null) {
                val vm: com.attendance.attendanceapp.ui.screens.admin.AdminViewModel = viewModel(factory = factory)
                ScheduleManagementScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() }, onNavigateToViewSchedules = { navController.navigate(Screen.ViewSchedules.route) })
            }
        }
        
        composable(Screen.AcademicManagement.route) {
            if (factory != null) {
                val vm: com.attendance.attendanceapp.ui.screens.admin.AdminViewModel = viewModel(factory = factory)
                ManageAcademicDataScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
            }
        }

        composable(Screen.CreateTeacher.route) { 
            authViewModel?.let { vm -> CreateTeacherScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() }) } 
        }

        composable(Screen.ViewSchedules.route) {
            if (factory != null) {
                val vm: com.attendance.attendanceapp.ui.screens.admin.AdminViewModel = viewModel(factory = factory)
                com.attendance.attendanceapp.ui.screens.admin.ViewSchedulesScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
            }
        }

        composable(Screen.EditUser.route) { backStackEntry ->
            if (factory != null) {
                val vm: com.attendance.attendanceapp.ui.screens.admin.AdminViewModel = viewModel(factory = factory)
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                EditUserScreen(viewModel = vm, userId = userId, onNavigateBack = { navController.popBackStack() })
            }
        }

        composable("attendance_list/{sessionId}") { backStackEntry ->
            if (factory != null) {
                val vm: TeacherViewModel = viewModel(factory = factory)
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                AttendanceListScreen(viewModel = vm, sessionId = sessionId, onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
