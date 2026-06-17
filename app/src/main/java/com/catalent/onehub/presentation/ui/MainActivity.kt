package com.catalent.onehub.presentation.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.catalent.auth.domain.AuthState
import com.catalent.auth.ui.AuthViewModel
import com.catalent.auth.ui.ForgotPasswordViewModel
import com.catalent.auth.ui.LoginViewModel
import com.catalent.auth.ui.screen.ForgotPasswordScreen
import com.catalent.auth.ui.screen.LoginScreen
import com.catalent.core.network.ClientProvider
import com.catalent.core.network.NetworkManager
import com.catalent.core.network.RawClientWrapper
import com.catalent.onehub.R
import com.catalent.onehub.presentation.error.toStringRes
import com.catalent.onehub.presentation.screen.ErrorScreen
import com.catalent.onehub.presentation.screen.HomeScreen
import com.catalent.onehub.ui.theme.CatalentOneHubTheme
import com.salesforce.androidsdk.app.SalesforceSDKManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var clientProvider: ClientProvider
    @Inject lateinit var networkManager: NetworkManager

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check for existing session immediately
        checkAndInitializeClient()

        setContent {
            CatalentOneHubContent(
                clientProvider = clientProvider,
                networkManager = networkManager,
                authViewModel = authViewModel
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check in case account was added/removed while activity was paused
        checkAndInitializeClient()
    }

    private fun checkAndInitializeClient() {
        Log.d("MainActivity", "checkAndInitializeClient: Starting")
        val sdkManager = SalesforceSDKManager.getInstance()
        val account = sdkManager.userAccountManager.currentUser

        if (account != null) {
            Log.d("MainActivity", "checkAndInitializeClient: Existing account found: ${account.username}")
            val client = sdkManager.clientManager.peekRestClient()
            if (client != null) {
                Log.d("MainActivity", "checkAndInitializeClient: RestClient obtained, notifying ClientProvider")
                clientProvider.onClientAvailable(RawClientWrapper(client))
            } else {
                Log.w("MainActivity", "checkAndInitializeClient: Account exists but peekRestClient() returned null")
            }
        } else {
            Log.d("MainActivity", "checkAndInitializeClient: No account found")
        }
    }
}

@Composable
fun CatalentOneHubContent(
    clientProvider: ClientProvider,
    networkManager: NetworkManager,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle(AuthState.Loading)
    val errorState by authViewModel.errorState.collectAsStateWithLifecycle(null)
    val isConnected by networkManager.observeConnectivity.collectAsStateWithLifecycle(initialValue = true)

    CatalentOneHubMainScreen(
        authState = authState,
        errorState = errorState,
        isConnected = isConnected,
        onClearError = { authViewModel.clearError() },
        onLogout = { authViewModel.logout() }
    )
}

@Composable
fun CatalentOneHubMainScreen(
    authState: AuthState,
    errorState: com.catalent.auth.domain.exceptions.AuthException?,
    isConnected: Boolean,
    onClearError: () -> Unit,
    onLogout: () -> Unit
) {
    CatalentOneHubTheme {
        errorState?.let { error ->
            AlertDialog(
                onDismissRequest = onClearError,
                title = { Text(stringResource(R.string.title_error)) },
                text = { Text(stringResource(error.toStringRes())) },
                confirmButton = {
                    TextButton(onClick = onClearError) {
                        Text("OK")
                    }
                }
            )
        }

        when (val state = authState) {
            is AuthState.Authenticated -> {
                Log.d("MainActivity", "CatalentOneHubContent: AuthState.Authenticated -> Showing HomeScreen")
                HomeScreen(
                    isConnected = isConnected,
                    onLogout = onLogout
                )
            }

            is AuthState.Error -> {
                Log.d("MainActivity", "CatalentOneHubContent: AuthState.Error -> Showing ErrorScreen")
                ErrorScreen(
                    message = stringResource(state.exception.toStringRes()),
                    onRetry = onLogout
                )
            }

            is AuthState.Loading -> {
                Log.d("MainActivity", "CatalentOneHubContent: AuthState.Loading -> Showing ProgressIndicator")
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is AuthState.Unauthenticated -> {
                Log.d("MainActivity", "CatalentOneHubContent: AuthState.Unauthenticated -> Showing AuthNavigation")
                AuthNavigation()
            }
        }
    }
}

@Composable
fun AuthNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToForgot = { navController.navigate("forgot_password") }
            )
        }
        composable("forgot_password") {
            val forgotViewModel: ForgotPasswordViewModel = hiltViewModel()
            ForgotPasswordScreen(
                viewModel = forgotViewModel,
                onBackToLogin = { 
                    forgotViewModel.clear()
                    navController.popBackStack() 
                }
            )
        }
    }
}
