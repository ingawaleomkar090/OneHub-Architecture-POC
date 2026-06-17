package com.catalent.auth.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalent.auth.domain.AuthRepository
import com.catalent.auth.ui.state.ForgotPasswordState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    var username by mutableStateOf("")
    var otpCode by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var state by mutableStateOf<ForgotPasswordState>(ForgotPasswordState.Idle)
        private set

    fun requestOtp() {
        viewModelScope.launch {
            state = ForgotPasswordState.Loading
            try {
                val success = repository.initializeForgotPassword(username)
                state = if (success) ForgotPasswordState.OtpSent else ForgotPasswordState.Error("Failed sending OTP.")
            } catch (e: Exception) {
                state = ForgotPasswordState.Error(e.message ?: "Error encountered")
            }
        }
    }

    fun resetPassword() {
        viewModelScope.launch {
            state = ForgotPasswordState.Loading
            try {
                val success = repository.submitNewPassword(username, otpCode, newPassword)
                state = if (success) ForgotPasswordState.ResetSuccess else ForgotPasswordState.Error("Invalid OTP validation code.")
            } catch (e: Exception) {
                state = ForgotPasswordState.Error(e.message ?: "Update task configuration broken")
            }
        }
    }

    fun clear() {
        state = ForgotPasswordState.Idle
        otpCode = ""
        newPassword = ""
    }
}
