package com.catalent.auth.data

import com.catalent.auth.data.mapper.AuthExceptionMapper
import com.catalent.auth.domain.AuthRepository
import com.catalent.auth.domain.AuthState
import com.catalent.core.network.ClientProvider
import com.salesforce.androidsdk.app.SalesforceSDKManager
import com.salesforce.androidsdk.rest.RestClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SalesforceAuthRepository @Inject constructor(
    private val clientProvider: ClientProvider,
) : AuthRepository, ClientProvider.ClientListener {

    init {
        clientProvider.addListener(this)
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override fun onClientAvailable(client: RestClient) {
        try {
            val info = client.clientInfo
            _authState.value = AuthState.Authenticated(
                userId = info.userId ?: "unknown",
                orgId = info.orgId ?: "unknown",
                username = info.username ?: "unknown",
                instanceUrl = info.instanceUrl?.toString() ?: "",
            )
        } catch (e: Exception) {
            _authState.value = AuthState.Error(AuthExceptionMapper.map(e))
        }
    }

    override fun onClientRemoved() {
        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun logout() {
        try {
            SalesforceSDKManager.getInstance().logout(null)
            clientProvider.onClientRemoved()
        } catch (e: Throwable) {
            throw AuthExceptionMapper.map(e)
        }
    }
}