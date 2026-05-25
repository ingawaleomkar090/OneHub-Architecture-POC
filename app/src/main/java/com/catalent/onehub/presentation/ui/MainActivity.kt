package com.catalent.onehub.presentation.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.catalent.onehub.R
import com.catalent.auth.domain.AuthState
import com.catalent.auth.ui.AuthViewModel
import com.catalent.core.network.ClientProvider
import com.catalent.core.network.NetworkManager
import com.catalent.onehub.presentation.error.toStringRes
import com.catalent.onehub.presentation.screen.ErrorScreen
import com.catalent.onehub.presentation.screen.HomeScreen
import com.catalent.onehub.ui.theme.CatalentOneHubTheme
import com.salesforce.androidsdk.rest.RestClient
import com.salesforce.androidsdk.ui.SalesforceActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : SalesforceActivity() {

    @Inject lateinit var clientProvider: ClientProvider
    @Inject lateinit var networkManager: NetworkManager

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun onResume(client: RestClient) {
        clientProvider.onClientAvailable(client)

        setContent {
            val authState by authViewModel.authState.collectAsState()
            val errorState by authViewModel.errorState.collectAsState()
            val isConnected by networkManager.observeConnectivity.collectAsState(initial = true)

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
                    is AuthState.Authenticated -> HomeScreen(
                        isConnected = isConnected,
                        onLogout = { authViewModel.logout() }
                    )

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
    }

    override fun onPause() {
        super.onPause()
        clientProvider.onClientRemoved()
    }
}


