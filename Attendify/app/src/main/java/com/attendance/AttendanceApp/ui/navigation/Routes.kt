package com.attendance.attendanceapp.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Verify : Screen("verify")
    object StudentDashboard : Screen("student_dashboard")
    object TeacherDashboard : Screen("teacher_dashboard")
    object AdminDashboard : Screen("admin_dashboard")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object EditProfile : Screen("edit_profile")
    object ScheduleManagement : Screen("schedule_management")
    object CreateTeacher : Screen("create_teacher")
    object AcademicManagement : Screen("manage_academic")
    object ViewSchedules : Screen("view_schedules")
    object StudentCourses : Screen("student_schedules")
    object AttendanceHistory : Screen("student_history")
    object AttendanceReports : Screen("attendance_reports")
    object UserManagement : Screen("manage_users")
    object TeacherSchedules : Screen("teacher_schedules")
    object EditUser : Screen("edit_user/{userId}") {
        fun createRoute(userId: String) = "edit_user/$userId"
    }
}
