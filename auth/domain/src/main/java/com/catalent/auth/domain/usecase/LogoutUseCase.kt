package com.catalent.auth.domain.usecase

import com.catalent.auth.domain.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository,
) {
	suspend operator fun invoke() = authRepository.logout()
}