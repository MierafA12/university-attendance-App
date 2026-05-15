package com.attendance.attendanceapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.attendance.attendanceapp.ui.screens.common.SplashScreen
import com.attendance.attendanceapp.ui.screens.onboarding.OnboardingScreen
import com.attendance.attendanceapp.ui.screens.auth.LoginScreen
import com.attendance.attendanceapp.ui.screens.auth.RegisterScreen
import com.attendance.attendanceapp.ui.screens.auth.ForgotPasswordScreen
import com.attendance.attendanceapp.ui.screens.auth.VerifyScreen
import com.attendance.attendanceapp.ui.screens.student.StudentDashboard
import com.attendance.attendanceapp.ui.screens.teacher.TeacherDashboard
import com.attendance.attendanceapp.ui.screens.admin.AdminDashboard
import com.attendance.attendanceapp.ui.screens.common.ProfileScreen
import com.attendance.attendanceapp.ui.screens.student.QRScannerScreen
import com.attendance.attendanceapp.ui.screens.teacher.StartSessionScreen
import com.attendance.attendanceapp.ui.screens.teacher.TeacherReportsScreen
import com.attendance.attendanceapp.ui.screens.admin.ManageUsersScreen
import com.attendance.attendanceapp.ui.screens.admin.ReportsScreen as AdminReports
import com.attendance.attendanceapp.ui.screens.admin.ScheduleManagementScreen
import com.attendance.attendanceapp.ui.screens.admin.CreateTeacherScreen
import com.attendance.attendanceapp.ui.screens.admin.ManageAcademicDataScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val destination = when (role) {
                        "admin" -> Screen.AdminDashboard.route
                        "teacher" -> Screen.TeacherDashboard.route
                        else -> Screen.StudentDashboard.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgot = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    // Navigate back to login or straight to dashboard
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onGetOTP = { email ->
                    navController.navigate("verify/$email")
                }
            )
        }

        composable("verify/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyScreen(
                email = email,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onVerify = { otp ->
                    // For demo, navigate to student dashboard
                    navController.navigate(Screen.StudentDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.StudentDashboard.route) {
            StudentDashboard(
                onNavigateToScan = { navController.navigate("scan") },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TeacherDashboard.route) {
            TeacherDashboard(
                onStartSession = { navController.navigate("start_session") },
                onViewReports = { navController.navigate("teacher_reports") },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboard(
                onManageUsers = { navController.navigate("manage_users") },
                onViewReports = { navController.navigate("admin_reports") },
                onAssignTeachers = { /* Navigate to Assign */ },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onManageSchedule = { navController.navigate(Screen.ScheduleManagement.route) },
                onManageAcademic = { navController.navigate(Screen.AcademicManagement.route) }
            )
        }

        composable(Screen.AcademicManagement.route) {
            ManageAcademicDataScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Supporting Screens
        composable("scan") { QRScannerScreen(onClose = { navController.popBackStack() }) }
        composable("teacher_reports") { TeacherReportsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("admin_reports") { AdminReports(onNavigateBack = { navController.popBackStack() }) }
        composable("manage_users") { 
            ManageUsersScreen(
                onNavigateBack = { navController.popBackStack() },
                onCreateTeacher = { navController.navigate(Screen.CreateTeacher.route) }
            ) 
        }
        composable(Screen.CreateTeacher.route) { CreateTeacherScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.ScheduleManagement.route) { ScheduleManagementScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("start_session") { 
            StartSessionScreen(
                onNavigateBack = { navController.popBackStack() },
                onSessionStarted = { _, _, _ -> navController.popBackStack() }
            ) 
        }
    }
}
