package com.catalent.onehub.presentation.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catalent.auth.domain.AuthState
import com.catalent.auth.ui.AuthViewModel
import com.catalent.core.network.ClientProvider
import com.catalent.core.network.NetworkManager
import com.catalent.core.network.RawClientWrapper
import com.catalent.onehub.R
import com.catalent.onehub.presentation.biometric.BiometricEnrollmentBottomSheet
import com.catalent.onehub.presentation.biometric.BiometricHelper
import com.catalent.onehub.presentation.biometric.BiometricStatus
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

    @Inject
    lateinit var clientProvider: ClientProvider

    @Inject
    lateinit var networkManager: NetworkManager

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatalentOneHubContent(clientProvider, networkManager, authViewModel)
        }
    }

    override fun onResume(client: RestClient) {
        clientProvider.onClientAvailable(RawClientWrapper(client))
    }
}

@Composable
fun CatalentOneHubContent(
    clientProvider: ClientProvider,
    networkManager: NetworkManager,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val errorState by authViewModel.errorState.collectAsStateWithLifecycle(null)
    val isConnected by networkManager.observeConnectivity.collectAsStateWithLifecycle(initialValue = true)

    val context = LocalContext.current
    val biometricHelper = remember { BiometricHelper(context) }
    var isAppLocked by remember { mutableStateOf(false) }

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
                BiometricPromptLogic(
                    biometricHelper = biometricHelper,
                    authViewModel = authViewModel,
                    onLockStateChange = { isAppLocked = it }
                )

                if (!isAppLocked) {
                    HomeScreen(
                        isConnected = isConnected,
                        onLogout = { authViewModel.logout() }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            AuthState.Unauthenticated -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is AuthState.Error -> {
                ErrorScreen(
                    message = state.message,
                    onRetry = { /* Handle retry if necessary */ }
                )
            }

            AuthState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun BiometricPromptLogic(
    biometricHelper: BiometricHelper,
    authViewModel: AuthViewModel,
    onLockStateChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val sfBiometricManager = remember {
        SalesforceSDKManager.getInstance().biometricAuthenticationManager
    }
    val prefs = remember {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    var showEnrollmentSheet by remember { mutableStateOf(false) }
    var biometricStatus by remember { mutableStateOf(BiometricStatus.UNSUPPORTED) }

    LaunchedEffect(Unit) {
        val status = biometricHelper.checkBiometricAvailability()
        biometricStatus = status

        val isOptedIn = sfBiometricManager?.hasBiometricOptedIn() == true
        val alreadyPrompted = prefs.getBoolean("biometric_enrollment_prompted", false)

        if (isOptedIn && sfBiometricManager?.locked == true) {
            onLockStateChange(true)
            biometricHelper.showBiometricPrompt(
                activity = activity,
                onSuccess = {
                    Timber.tag("BIO_AUTH").d("Authentication successful, unlocking app")
                    onLockStateChange(false)
                    // Sync Salesforce SDK state by calling internal onUnlock via reflection
                    try {
                        sfBiometricManager.let { manager ->
                            val onUnlockMethod = manager::class.java.getMethod("onUnlock")
                            onUnlockMethod.invoke(manager)
                            Timber.tag("BIO_AUTH").d("Salesforce Biometric Manager unlocked successfully")
                        }
                    } catch (e: Exception) {
                        Timber.tag("BIO_AUTH").e(e, "Failed to call onUnlock via reflection")
                    }
                },
                onError = { errorCode, _ ->
                    if (errorCode == androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        onLockStateChange(false) 
                    }
                },
                onFailed = { }
            )
        } else if (!isOptedIn && !alreadyPrompted && !authViewModel.biometricPromptSkippedThisSession) {
            showEnrollmentSheet = true
        }
    }

    if (showEnrollmentSheet) {
        BiometricEnrollmentBottomSheet(
            status = biometricStatus,
            onEnable = {
                if (biometricStatus == BiometricStatus.SUCCESS) {
                    sfBiometricManager?.biometricOptIn(true)
                    prefs.edit { putBoolean("biometric_enrollment_prompted", true) }
                    showEnrollmentSheet = false
                } else {
                    biometricHelper.openBiometricSettings()
                }
            },
            onSkip = {
                authViewModel.setBiometricPromptSkipped()
                sfBiometricManager?.biometricOptIn(false)
                prefs.edit { putBoolean("biometric_enrollment_prompted", true) }
                showEnrollmentSheet = false
            },
            onDismiss = {
                showEnrollmentSheet = false
            }
        )
    }
}
