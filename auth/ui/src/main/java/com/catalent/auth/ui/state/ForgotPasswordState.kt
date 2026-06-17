package com.catalent.auth.ui.state

sealed interface ForgotPasswordState {
    object Idle : ForgotPasswordState
    object Loading : ForgotPasswordState
    object OtpSent : ForgotPasswordState
    object ResetSuccess : ForgotPasswordState
    data class Error(val message: String) : ForgotPasswordState
}
