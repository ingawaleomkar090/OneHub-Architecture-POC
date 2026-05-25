package com.catalent.auth.domain.exceptions

sealed class AuthException(message: String) : Exception(message) {
    class InvalidCredentials(message: String = "Invalid credentials") : AuthException(message)
    class SessionExpired(message: String = "Session expired") : AuthException(message)
    class NetworkError(message: String = "Network unavailable") : AuthException(message)
    class LogoutFailed(message: String = "Logout failed") : AuthException(message)
    class UnknownError(message: String = "Something went wrong") : AuthException(message)
}