package com.attendance.attendanceapp.ui.screens.common

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.attendanceapp.domain.model.User
import com.attendance.attendanceapp.domain.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<ProfileUiState>(ProfileUiState.Idle)
    val uiState: State<ProfileUiState> = _uiState

    fun updateProfile(user: User) {
        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            try {
                userRepository.updateUser(user)
                _uiState.value = ProfileUiState.Success("Profile updated successfully")
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Failed to update profile")
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            // 1. Re-authenticate first
            val reauthResult = userRepository.reauthenticate(currentPassword)
            reauthResult.onSuccess {
                // 2. If re-auth successful, update password
                val updateResult = userRepository.updatePassword(newPassword)
                updateResult.onSuccess {
                    _uiState.value = ProfileUiState.Success("Password changed successfully")
                }.onFailure { error ->
                    _uiState.value = ProfileUiState.Error(error.message ?: "Failed to update password")
                }
            }.onFailure { error ->
                _uiState.value = ProfileUiState.Error("Incorrect current password")
            }
        }
    }

    fun resetState() {
        _uiState.value = ProfileUiState.Idle
    }
}

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val message: String) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}
