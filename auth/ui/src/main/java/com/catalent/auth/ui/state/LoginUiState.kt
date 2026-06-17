package com.catalent.auth.ui.state

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val accountsCount: Int) : LoginUiState
    data class Error(val message: String) : LoginUiState
}
