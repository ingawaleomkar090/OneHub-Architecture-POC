package com.catalent.auth.domain

import kotlinx.coroutines.flow.StateFlow

// FIXME: This should be a pure interface, but StateFlow is a Kotlin internal class. In a real app, we'd want to abstract this further so the domain layer doesn't depend on kotlinx.coroutines.

interface AuthRepository {
	val authState: StateFlow<AuthState>
	suspend fun logout()
}