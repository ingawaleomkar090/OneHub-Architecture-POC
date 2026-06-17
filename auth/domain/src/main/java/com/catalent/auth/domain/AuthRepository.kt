package com.catalent.auth.domain

import com.catalent.auth.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>
    
    suspend fun loginHeadless(username: String, password: String): AuthSession
    
    suspend fun initializeForgotPassword(username: String): Boolean
    
    suspend fun submitNewPassword(username: String, otpCode: String, newPassword: String): Boolean
    
    suspend fun logout()
}
