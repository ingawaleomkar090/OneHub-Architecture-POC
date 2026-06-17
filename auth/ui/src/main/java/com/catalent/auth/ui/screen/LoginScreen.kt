package com.catalent.auth.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catalent.auth.ui.LoginViewModel
import com.catalent.auth.ui.state.LoginUiState

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToForgot: () -> Unit
) {
    LoginScreenContent(
        username = viewModel.username,
        password = viewModel.password,
        uiState = viewModel.uiState,
        onUsernameChange = { viewModel.username = it },
        onPasswordChange = { viewModel.password = it },
        onLoginClick = { viewModel.onLoginClicked() },
        onLogoutClick = { viewModel.logout() },
        onNavigateToForgot = onNavigateToForgot
    )
}

@Composable
fun LoginScreenContent(
    username: String,
    password: String,
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToForgot: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is LoginUiState.Loading -> CircularProgressIndicator()
            is LoginUiState.Success -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "🎉 Successfully Authenticated!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("SDK State Hydrated.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onLogoutClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Log Out")
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("OneHub Headless Sign-In", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = username,
                        onValueChange = onUsernameChange,
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState is LoginUiState.Error) {
                        Text(uiState.message, color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Log In")
                    }
                    TextButton(
                        onClick = onNavigateToForgot,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Forgot Password?")
                    }
                }
            }
        }
    }
}

// Previews for different states of the Login Screen.
// These are used to verify the UI in Android Studio's Design View.

@Preview(showBackground = true, name = "1. Idle")
@Composable
fun LoginScreenIdlePreview() {
    MaterialTheme {
        Surface {
            LoginScreenContent(
                username = "user@example.com",
                password = "password",
                uiState = LoginUiState.Idle,
                onUsernameChange = {},
                onPasswordChange = {},
                onLoginClick = {},
                onLogoutClick = {},
                onNavigateToForgot = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "2. Loading")
@Composable
fun LoginScreenLoadingPreview() {
    MaterialTheme {
        Surface {
            LoginScreenContent(
                username = "user@example.com",
                password = "password",
                uiState = LoginUiState.Loading,
                onUsernameChange = {},
                onPasswordChange = {},
                onLoginClick = {},
                onLogoutClick = {},
                onNavigateToForgot = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "3. Error")
@Composable
fun LoginScreenErrorPreview() {
    MaterialTheme {
        Surface {
            LoginScreenContent(
                username = "user@example.com",
                password = "password",
                uiState = LoginUiState.Error("Invalid credentials"),
                onUsernameChange = {},
                onPasswordChange = {},
                onLoginClick = {},
                onLogoutClick = {},
                onNavigateToForgot = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "4. Success")
@Composable
fun LoginScreenSuccessPreview() {
    MaterialTheme {
        Surface {
            LoginScreenContent(
                username = "user@example.com",
                password = "password",
                uiState = LoginUiState.Success(1),
                onUsernameChange = {},
                onPasswordChange = {},
                onLoginClick = {},
                onLogoutClick = {},
                onNavigateToForgot = {}
            )
        }
    }
}
