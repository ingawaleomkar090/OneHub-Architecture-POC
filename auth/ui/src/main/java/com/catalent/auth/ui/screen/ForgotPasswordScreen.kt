package com.catalent.auth.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catalent.auth.ui.ForgotPasswordViewModel
import com.catalent.auth.ui.state.ForgotPasswordState

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onBackToLogin: () -> Unit
) {
    ForgotPasswordScreenContent(
        username = viewModel.username,
        otpCode = viewModel.otpCode,
        newPassword = viewModel.newPassword,
        state = viewModel.state,
        onUsernameChange = { viewModel.username = it },
        onOtpChange = { viewModel.otpCode = it },
        onNewPasswordChange = { viewModel.newPassword = it },
        onRequestOtp = { viewModel.requestOtp() },
        onResetPassword = { viewModel.resetPassword() },
        onBackToLogin = onBackToLogin
    )
}

@Composable
fun ForgotPasswordScreenContent(
    username: String,
    otpCode: String,
    newPassword: String,
    state: ForgotPasswordState,
    onUsernameChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onRequestOtp: () -> Unit,
    onResetPassword: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Reset Password", style = MaterialTheme.typography.titleLarge)
            when (state) {
                is ForgotPasswordState.Loading -> CircularProgressIndicator()
                is ForgotPasswordState.ResetSuccess -> {
                    Text(
                        "🎉 Password updated successfully!",
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(onClick = onBackToLogin) { Text("Back to Login") }
                }

                is ForgotPasswordState.OtpSent -> {
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = onOtpChange,
                        label = { Text("Enter OTP Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = onNewPasswordChange,
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = onResetPassword, modifier = Modifier.fillMaxWidth()) {
                        Text("Update Password")
                    }
                }

                else -> {
                    OutlinedTextField(
                        value = username,
                        onValueChange = onUsernameChange,
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (state is ForgotPasswordState.Error) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                    Button(onClick = onRequestOtp, modifier = Modifier.fillMaxWidth()) {
                        Text("Send Verification Code")
                    }
                    TextButton(onClick = onBackToLogin) { Text("Cancel") }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    MaterialTheme {
        ForgotPasswordScreenContent(
            username = "user@example.com",
            otpCode = "",
            newPassword = "",
            state = ForgotPasswordState.Idle,
            onUsernameChange = {},
            onOtpChange = {},
            onNewPasswordChange = {},
            onRequestOtp = {},
            onResetPassword = {},
            onBackToLogin = {}
        )
    }
}
