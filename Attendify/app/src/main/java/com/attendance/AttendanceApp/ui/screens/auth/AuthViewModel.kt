package com.attendance.attendanceapp.ui.screens.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.attendanceapp.domain.model.Role
import com.attendance.attendanceapp.domain.model.User
import com.attendance.attendanceapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.attendance.attendanceapp.domain.model.Student

class AuthViewModel(
    private val userRepository: UserRepository,
    private val academicRepository: com.attendance.attendanceapp.domain.repository.AcademicRepository
) : ViewModel() {

    val departments = academicRepository.getAllDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = userRepository.signIn(email, password)
            result.onSuccess { user ->
                _authState.value = AuthState.Authenticated(user)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Login failed")
            }
        }
    }

    fun signUp(
        name: String,
        email: String,
        password: String,
        role: Role,
        studentId: String? = null,
        departmentId: String? = null,
        year: String? = null,
        semester: String? = null
    ) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val status = if (role == Role.teacher) com.attendance.attendanceapp.domain.model.UserStatus.active 
                         else com.attendance.attendanceapp.domain.model.UserStatus.pending
            val user = User(id = "", name = name, email = email, role = role, status = status)
            val student = if (role == Role.student && studentId != null) {
                Student(studentId = studentId, userId = "", departmentId = departmentId, year = year ?: "", semester = semester ?: "1")
            } else null
            
            val result = userRepository.signUp(user, password, student)
            result.onSuccess { registeredUser ->
                _authState.value = AuthState.Authenticated(registeredUser)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Sign up failed")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = userRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                _authState.value = AuthState.PasswordResetSent
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Failed to send reset email")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    object PasswordResetSent : AuthState()
    data class Error(val message: String) : AuthState()
}
