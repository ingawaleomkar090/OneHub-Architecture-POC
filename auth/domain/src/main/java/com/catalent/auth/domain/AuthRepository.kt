package com.catalent.auth.domain

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
	val authState: Flow<AuthState>
	suspend fun logout()
}