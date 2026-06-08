package com.catalent.onehub.presentation.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.AlertDialog
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.CircularProgressIndicator
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catalent.auth.domain.AuthState
import com.catalent.auth.ui.AuthViewModel
import com.catalent.core.network.ClientProvider
import com.catalent.core.network.NetworkManager
import com.catalent.core.network.RawClientWrapper
import com.catalent.onehub.R
import com.catalent.onehub.presentation.error.toStringRes
import com.catalent.onehub.presentation.screen.ErrorScreen
import com.catalent.onehub.presentation.screen.HomeScreen
import com.catalent.onehub.ui.theme.CatalentOneHubTheme
import com.salesforce.androidsdk.app.SalesforceSDKManager
import com.salesforce.androidsdk.rest.RestClient
import com.salesforce.androidsdk.ui.SalesforceActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : SalesforceActivity() {

    @Inject lateinit var clientProvider: ClientProvider
    @Inject lateinit var networkManager: NetworkManager

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CatalentOneHubContent(
                clientProvider = clientProvider,
                networkManager = networkManager,
                authViewModel = authViewModel
            )
        }
    }

    /**
     * SalesforceActivity provides the RestClient here.
     * We use LifecycleResumeEffect to manage the ClientProvider lifecycle
     * within the Compose composition, following modern best practices.
     */
    override fun onResume(client: RestClient) {
        // We still need this to capture the client from Salesforce SDK
        clientProvider.onClientAvailable(RawClientWrapper(client))
    }

    override fun onPause() {
        super.onPause()
        clientProvider.onClientRemoved()
    }
}

@Composable
fun CatalentOneHubContent(
    clientProvider: ClientProvider,
    networkManager: NetworkManager,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle(AuthState.Unauthenticated)
    val errorState by authViewModel.errorState.collectAsStateWithLifecycle(null)
    val isConnected by networkManager.observeConnectivity.collectAsStateWithLifecycle(initialValue = true)

    CatalentOneHubTheme {
        errorState?.let { error ->
            AlertDialog(
                onDismissRequest = { authViewModel.clearError() },
                title = { Text(stringResource(R.string.title_error)) },
                text = { Text(stringResource(error.toStringRes())) },
                confirmButton = {
                    TextButton(onClick = { authViewModel.clearError() }) {
                        Text("OK")
                    }
                }
            )
        }

        when (val state = authState) {
            is AuthState.Authenticated -> {
                BiometricPromptLogic()
                HomeScreen(
                    isConnected = isConnected,
                    onLogout = { authViewModel.logout() }
                )
            }

            is AuthState.Error -> ErrorScreen(
                message = stringResource(state.exception.toStringRes()),
                onRetry = { authViewModel.logout() }
            )

            is AuthState.Unauthenticated,
            is AuthState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun BiometricPromptLogic() {
    val context = LocalContext.current
    val biometricManager = remember {
        SalesforceSDKManager.getInstance().biometricAuthenticationManager
    }
    val prefs = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    var showBiometricPrompt by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val optedIn = biometricManager?.hasBiometricOptedIn() == true
        val alreadyPrompted = prefs.getBoolean("biometric_prompt_shown", false)

        Timber.tag("BIO_DEBUG").d("OptedIn: $optedIn")
        Timber.tag("BIO_DEBUG").d("Enabled: ${biometricManager?.hasBiometricOptedIn()}")
        Timber.tag("BIO_DEBUG").d("Locked: ${biometricManager?.locked}")

        if (!optedIn && !alreadyPrompted) {
            showBiometricPrompt = true
        }
    }

    if (showBiometricPrompt) {
        PremiumBiometricDialog(
            onEnable = {
                biometricManager?.biometricOptIn(true)
                prefs.edit { putBoolean("biometric_prompt_shown", true) }
                showBiometricPrompt = false
            },
            onSkip = {
                biometricManager?.biometricOptIn(false)
                prefs.edit { putBoolean("biometric_prompt_shown", true) }
                showBiometricPrompt = false
            }
        )
    }
}

@Composable
fun PremiumBiometricDialog(
    onEnable: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        dismissButton = {},
        text = {
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(text = "SalesforcePOC", fontSize = 25.sp)

                    Text(
                        text = "Enable Biometric Login",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Secure your app with fingerprint or face unlock.",
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color.Gray
                    )

                    Column(modifier = Modifier.padding(top = 16.dp)) {

                        TextButton(onClick = onEnable, modifier = Modifier.fillMaxWidth()) {
                            Text("Enable", fontWeight = FontWeight.Bold)
                        }

                        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                            Text("Not Now")
                        }
                    }
                }
            }
        }
    )
}
