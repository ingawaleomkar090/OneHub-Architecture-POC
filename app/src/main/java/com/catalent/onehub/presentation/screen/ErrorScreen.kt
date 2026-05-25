package com.catalent.onehub.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.catalent.onehub.R
import com.catalent.onehub.ui.theme.CatalentOneHubTheme
import com.catalent.onehub.ui.theme.Dimen.inputHeight
import com.catalent.onehub.ui.theme.Dimen.space48
import com.catalent.onehub.ui.theme.Dimen.spaceLg
import com.catalent.onehub.ui.theme.Dimen.spaceMd
import com.catalent.onehub.ui.theme.Dimen.spaceXl
import com.catalent.onehub.ui.theme.Dimen.spaceXs
import com.catalent.onehub.ui.theme.Dimen.spaceXxl

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spaceXxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(inputHeight)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.WifiOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(spaceXl),
            )
        }
        Spacer(Modifier.height(spaceLg))
        Text(stringResource(R.string.error_unknown), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(spaceXs))
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spaceXl))
        Button(onClick = onRetry) { Text("Log in again") }
    }
}

@Composable
fun InlineErrorCard(message: String, onDismiss: () -> Unit, onRetry: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(spaceLg)) {
        Column(
            modifier = Modifier.padding(spaceLg),
            verticalArrangement = Arrangement.spacedBy(spaceMd)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(spaceMd)) {
                Icon(
                    Icons.Default.Warning, contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Column {
                    Text(
                        "Network unavailable", style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        message, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Dismiss") }
                TextButton(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}

@Composable
fun EmptyErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spaceXxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Default.CloudOff, contentDescription = null,
            modifier = Modifier.size(space48),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(spaceMd))
        Text("No local data found", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(spaceXs))
        Text(
            message, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(spaceLg))
        OutlinedButton(onClick = onRetry) { Text("Retry") }
    }
}

@Preview(name = "Error Screen — Light", showBackground = true)
@Preview(
    name = "Error Screen — Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ErrorScreenPreview() {
    CatalentOneHubTheme {
        Surface {
            ErrorScreen(
                message = "Session expired. Please log in again.",
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Inline Error Card — Light", showBackground = true)
@Preview(
    name = "Inline Error Card — Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun InlineErrorCardPreview() {
    CatalentOneHubTheme {
        Surface {
            InlineErrorCard(
                message = "Could not load records. Check your connection.",
                onDismiss = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Empty Error State — Light", showBackground = true)
@Preview(
    name = "Empty Error State — Dark",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun EmptyErrorStatePreview() {
    CatalentOneHubTheme {
        Surface {
            EmptyErrorState(
                message = "You appear to be offline. Connect to the internet to load records.",
                onRetry = {},
            )
        }
    }
}