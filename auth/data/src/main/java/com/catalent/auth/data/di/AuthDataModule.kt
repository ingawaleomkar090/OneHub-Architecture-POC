package com.catalent.auth.data.di

import com.catalent.auth.data.SalesforceAuthRepository
import com.catalent.auth.domain.AuthRepository
import com.catalent.auth.domain.usecase.LogoutUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthDataModule {

	@Binds
	@Singleton
	abstract fun bindAuthRepository(
		impl: SalesforceAuthRepository,
	): AuthRepository

	companion object {
		@Provides
		fun provideLogoutUseCase(
			authRepository: AuthRepository,
		): LogoutUseCase = LogoutUseCase(authRepository)
	}
}