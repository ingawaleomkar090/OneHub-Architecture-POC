package com.catalent.auth.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalent.auth.domain.AuthRepository
import com.catalent.auth.ui.state.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var uiState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set

    fun onLoginClicked() {
        viewModelScope.launch {
            uiState = LoginUiState.Loading
            try {
                // The repository handles hydration and publishing the client.
                // MainActivity/AuthViewModel react to the Published client.
                authRepository.loginHeadless(username, password)
                uiState = LoginUiState.Success(0) // Success state to stop loading
            } catch (e: Exception) {
                uiState = LoginUiState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            username = ""
            password = ""
            uiState = LoginUiState.Idle
        }
    }
}
