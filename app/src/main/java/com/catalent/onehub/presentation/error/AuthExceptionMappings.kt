package com.catalent.onehub.presentation.error

import androidx.annotation.StringRes
import com.catalent.auth.domain.exceptions.AuthException
import com.catalent.onehub.R

@StringRes
fun AuthException.toStringRes(): Int = when (this) {
    is AuthException.InvalidCredentials -> R.string.error_invalid_credentials
    is AuthException.SessionExpired -> R.string.error_session_expired
    is AuthException.NetworkError -> R.string.error_network
    is AuthException.LogoutFailed -> R.string.error_logout_failed
    is AuthException.UnknownError -> R.string.error_unknown
}
