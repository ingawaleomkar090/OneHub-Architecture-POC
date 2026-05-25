package com.catalent.onehub.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NetworkBanner() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(Color.Red)
			.padding(4.dp),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = "Network issue",
			color = Color.White,
			style = MaterialTheme.typography.labelSmall,
		)
	}
}