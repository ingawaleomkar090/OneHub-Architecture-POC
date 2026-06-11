package com.catalent.onehub.presentation.biometric

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricEnrollmentBottomSheet(
    status: BiometricStatus,
    onEnable: () -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        BiometricEnrollmentContent(
            status = status,
            onEnable = onEnable,
            onSkip = onSkip
        )
    }
}

@Composable
fun BiometricEnrollmentContent(
    status: BiometricStatus,
    onEnable: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFF001F3F) // Dark blue from the image
        )

        Spacer(modifier = Modifier.height(24.dp))

        val title = when (status) {
            BiometricStatus.SUCCESS -> "Enable Biometric Login"
            BiometricStatus.NOT_ENROLLED -> "Biometric Not Configured"
            BiometricStatus.NO_HARDWARE, BiometricStatus.UNSUPPORTED -> "Biometric Not Supported"
            BiometricStatus.HW_UNAVAILABLE -> "Biometric Hardware Unavailable"
            BiometricStatus.SECURITY_UPDATE_REQUIRED -> "Security Update Required"
        }

        val description = when (status) {
            BiometricStatus.SUCCESS -> "Sign in faster next time using your biometric credentials. You can change this in the app settings."
            BiometricStatus.NOT_ENROLLED -> "Your device supports biometric login, but you haven't enrolled any face or fingerprint yet."
            BiometricStatus.NO_HARDWARE, BiometricStatus.UNSUPPORTED -> "Your device does not support biometric authentication."
            BiometricStatus.HW_UNAVAILABLE -> "Biometric hardware is currently unavailable. Please try again later."
            BiometricStatus.SECURITY_UPDATE_REQUIRED -> "A security update is required to use biometric authentication."
        }

        val actionButtonText = when (status) {
            BiometricStatus.SUCCESS -> "Enable Biometric"
            BiometricStatus.NOT_ENROLLED -> "Open Settings"
            else -> null
        }

        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF001F3F),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = description,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (actionButtonText != null) {
            Button(
                onClick = onEnable,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F3F))
            ) {
                Text(text = actionButtonText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                text = "Skip for now",
                color = Color(0xFF001F3F),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
