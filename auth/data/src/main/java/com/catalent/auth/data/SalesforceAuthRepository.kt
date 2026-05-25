package com.catalent.auth.data

import com.catalent.auth.data.mapper.AuthExceptionMapper
import com.catalent.auth.domain.AuthRepository
import com.catalent.auth.domain.AuthState
import com.catalent.core.network.ClientProvider
import com.catalent.core.network.RawClientWrapper
import com.salesforce.androidsdk.app.SalesforceSDKManager
import com.salesforce.androidsdk.rest.RestClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class SalesforceAuthRepository @Inject constructor(
    private val clientProvider: ClientProvider,
) : AuthRepository {

    override val authState: Flow<AuthState> = clientProvider.client.map { clientWrapper ->
        val client = (clientWrapper as? RawClientWrapper)?.rawClient as? RestClient

        if (client == null) {
            AuthState.Unauthenticated
        } else {
            try {
                val info = client.clientInfo
                AuthState.Authenticated(
                    userId = info.userId ?: "unknown",
                    orgId = info.orgId ?: "unknown",
                    username = info.username ?: "unknown",
                    instanceUrl = info.instanceUrl?.toString() ?: "",
                )
            } catch (e: Exception) {
                AuthState.Error(AuthExceptionMapper.map(e))
            }
        }
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