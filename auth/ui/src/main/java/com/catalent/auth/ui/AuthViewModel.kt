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
			initialValue = AuthState.Loading,
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
			} catch (authException: AuthException) {
				e("logout", "AuthException — ${authException.message}", authException)
				_errorState.value = authException
			} catch (throwable: Throwable) {
				e("logout", "unexpected error — ${throwable.message}", throwable)
				_errorState.value = AuthException.UnknownError(throwable.message ?: "Unknown error")
			}
		}
	}

	fun clearError() {
		_errorState.value = null
	}
}