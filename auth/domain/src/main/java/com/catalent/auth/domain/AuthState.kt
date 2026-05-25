package com.catalent.auth.domain

import com.catalent.auth.domain.exceptions.AuthException

sealed class AuthState {
	object Unauthenticated : AuthState()
	object Loading : AuthState()
	data class Authenticated(
		val userId: String,
		val orgId: String,
		val username: String,
		val instanceUrl: String,
	) : AuthState()
	data class Error(
		val exception: AuthException,
		val message: String = exception.message ?: "Unknown error"
	) : AuthState()
}