package com.catalent.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalent.auth.domain.AuthRepository
import com.catalent.auth.domain.AuthState
import com.catalent.auth.domain.usecase.LogoutUseCase
import com.catalent.auth.domain.exceptions.AuthException
import com.catalent.core.logging.Loggable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
	private val logoutUseCase: LogoutUseCase,
	authRepository: AuthRepository,
) : ViewModel(), Loggable {

	private val _errorState = MutableStateFlow<AuthException?>(null)

	val authState: StateFlow<AuthState> = authRepository.authState
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = AuthState.Unauthenticated,
		)

	val errorState: StateFlow<AuthException?> = _errorState
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = null,
		)

	fun logout() {
		viewModelScope.launch {
			try {
				logoutUseCase()
			} catch (e: AuthException) {
				e("logout", "AuthException — ${e.message}", e)
				_errorState.value = e
			} catch (e: Throwable) {
				e("logout", "unexpected error — ${e.message}", e)
				_errorState.value = AuthException.UnknownError(e.message ?: "Unknown error")
			}
		}
	}

	fun clearError() {
		_errorState.value = null
	}
}