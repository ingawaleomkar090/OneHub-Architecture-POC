package com.catalent.auth.data

import android.util.Log
import com.catalent.auth.data.mapper.AuthExceptionMapper
import com.catalent.auth.domain.AuthRepository
import com.catalent.auth.domain.AuthState
import com.catalent.auth.domain.model.AuthSession
import com.catalent.core.auth.PkceGenerator
import com.catalent.core.common.AppDispatchers
import com.catalent.core.network.ClientProvider
import com.catalent.core.network.RawClientWrapper
import com.salesforce.androidsdk.accounts.UserAccountBuilder
import com.salesforce.androidsdk.app.SalesforceSDKManager
import com.salesforce.androidsdk.rest.RestClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLDecoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesforceAuthRepository @Inject constructor(
    private val clientProvider: ClientProvider,
    private val dispatchers: AppDispatchers,
) : AuthRepository {

    private val baseSiteUrl = BuildConfig.SALESFORCE_COMMUNITY_BASE_URL
    private val clientId = BuildConfig.SALESFORCE_CONSUMER_KEY
    private val redirectUri = BuildConfig.SALESFORCE_REDIRECT_AUTHURI

    private val forgotPasswordUrl = "$baseSiteUrl/services/auth/headless/forgot_password"

    private val httpClient = OkHttpClient.Builder()
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    override val authState: Flow<AuthState> = clientProvider.client.map { clientWrapper ->
        val client = (clientWrapper as? RawClientWrapper)?.rawClient as? RestClient

        if (client == null) {
            Log.d("SalesforceAuthRepository", "AuthState: Unauthenticated (Client is null)")
            AuthState.Unauthenticated
        } else {
            try {
                val info = client.clientInfo
                Log.d("SalesforceAuthRepository", "AuthState: Authenticated for user: ${info.username}")
                AuthState.Authenticated(
                    userId = info.userId ?: "unknown",
                    orgId = info.orgId ?: "unknown",
                    username = info.username ?: "unknown",
                    instanceUrl = info.instanceUrl?.toString() ?: "",
                )
            } catch (exception: Exception) {
                Log.e("SalesforceAuthRepository", "AuthState: Error mapping client info", exception)
                AuthState.Error(AuthExceptionMapper.map(exception))
            }
        }
    }

    override suspend fun loginHeadless(username: String, password: String): AuthSession = withContext(dispatchers.io) {
        val pkceVerifier = PkceGenerator.generateCodeVerifier()
        val pkceChallenge = PkceGenerator.generateCodeChallenge(pkceVerifier)

        val authBody = FormBody.Builder()
            .add("response_type", "code_credentials")
            .add("client_id", clientId)
            .add("redirect_uri", redirectUri)
            .add("username", username)
            .add("password", password)
            .add("code_challenge", pkceChallenge)
            .add("code_challenge_method", "S256")
            .build()

        val authRequest = Request.Builder()
            .url("$baseSiteUrl/services/oauth2/authorize")
            .header("Auth-Request-Type", "named-user")
            .post(authBody)
            .build()

        httpClient.newCall(authRequest).execute().use { response ->
            if (response.code != 302) throw Exception("Authentication Rejected: HTTP ${response.code}")
            val locationHeader = response.header("Location")
            val authCode = extractQueryParam(locationHeader, "code") ?: throw Exception("Auth code missing from context.")
            Log.d("SalesforceAuthRepository", "loginHeadless: Obtained auth code")
            return@withContext exchangeCodeForTokens(authCode, pkceVerifier, username)
        }
    }

    private suspend fun exchangeCodeForTokens(authCode: String, verifier: String, username: String): AuthSession = withContext(dispatchers.io) {
        val tokenBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", clientId)
            .add("redirect_uri", redirectUri)
            .add("code", authCode)
            .add("code_verifier", verifier)
            .build()

        val tokenRequest = Request.Builder()
            .url("$baseSiteUrl/services/oauth2/token")
            .post(tokenBody)
            .build()

        httpClient.newCall(tokenRequest).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Token exchange failed: HTTP ${response.code}")
            val bodyString = response.body?.string() ?: throw Exception("Empty token response body")
            val json = JSONObject(bodyString)

            // Extract userId and orgId from the 'id' URL
            // Format: https://login.salesforce.com/id/ORG_ID/USER_ID
            val idUrl = json.optString("id")
            val (extractedOrgId, extractedUserId) = if (idUrl.isNotEmpty()) {
                val parts = idUrl.trimEnd('/').split("/")
                val uId = parts.last()
                val oId = parts[parts.size - 2]
                oId to uId
            } else {
                "00D000000000000AAA" to "005000000000000AAA"
            }

            val session = AuthSession(
                userId = extractedUserId,
                orgId = extractedOrgId,
                username = username,
                accessToken = json.getString("access_token"),
                refreshToken = json.getString("refresh_token"),
                instanceUrl = json.getString("instance_url"),
                communityId = json.optString("sfdc_community_id"),
                communityUrl = json.optString("sfdc_community_url"),
                idUrl = idUrl
            )

            // Async handover to SDK to avoid blocking the main flow
            withContext(dispatchers.io) {
                hydrateSalesforceSdk(session)
            }
            
            return@withContext session
        }
    }

    private fun hydrateSalesforceSdk(session: AuthSession) {
        val sdkManager = SalesforceSDKManager.getInstance()

        val account = UserAccountBuilder.getInstance()
            .userId(session.userId)
            .orgId(session.orgId)
            .username(session.username)
            .accountName("${session.username} (${session.instanceUrl})")
            .authToken(session.accessToken)
            .refreshToken(session.refreshToken)
            .instanceServer(session.instanceUrl)
            .loginServer(baseSiteUrl)
            .clientId(clientId)
            .communityUrl(session.communityUrl)
            .idUrl(session.idUrl)
            .build()

        try {
            val accountManager = sdkManager.userAccountManager
            val existingAccount = accountManager.getUserFromOrgAndUserId(account.orgId, account.userId)
            
            if (existingAccount == null) {
                accountManager.createAccount(account)
            }

            if (accountManager.currentUser?.userId != account.userId) {
                accountManager.switchToUser(account)
            }
            
            val restClient = sdkManager.clientManager.peekRestClient()
            if (restClient != null) {
                clientProvider.onClientAvailable(RawClientWrapper(restClient))
            }
        } catch (e: Exception) {
            Log.e("SalesforceAuthRepository", "Error during SDK hydration", e)
        }
    }

    override suspend fun initializeForgotPassword(username: String): Boolean = withContext(dispatchers.io) {
        val body = FormBody.Builder().add("username", username).build()
        val request = Request.Builder().url(forgotPasswordUrl).post(body).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext false
            return@withContext JSONObject(response.body?.string() ?: "").optString("status_code") == "otp_sent"
        }
    }

    override suspend fun submitNewPassword(username: String, otpCode: String, newPassword: String): Boolean = withContext(dispatchers.io) {
        val body = FormBody.Builder()
            .add("username", username)
            .add("otp", otpCode)
            .add("newpassword", newPassword)
            .build()
        val request = Request.Builder().url(forgotPasswordUrl).post(body).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext false
            return@withContext JSONObject(response.body?.string() ?: "").optString("status_code") == "success"
        }
    }

    override suspend fun logout() {
        withContext(dispatchers.io) {
            try {
                val sdkManager = SalesforceSDKManager.getInstance()
                val currentUser = sdkManager.userAccountManager.currentUser
                if (currentUser != null) {
                    sdkManager.userAccountManager.signoutUser(currentUser, null)
                }
                clientProvider.onClientRemoved()
                Log.d("SalesforceAuthRepository", "logout: User signed out and client removed")
            } catch (authException: Throwable) {
                throw AuthExceptionMapper.map(authException)
            }
        }
    }

    private fun extractQueryParam(url: String?, paramName: String): String? {
        if (url == null) return null
        val query = url.split("?").getOrNull(1) ?: return null
        return query.split("&")
            .map { it.split("=") }
            .firstOrNull { it.size == 2 && URLDecoder.decode(it[0], "UTF-8") == paramName }
            ?.get(1)?.let { URLDecoder.decode(it, "UTF-8") }
    }
}
