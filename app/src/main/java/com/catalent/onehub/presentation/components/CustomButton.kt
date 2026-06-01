package com.catalent.onehub.presentation.components

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CustomButton(
	text: String,
	onClick: () -> Unit,
) {
	Button(
		onClick = onClick
	) {
		Text(text = text)
	}
}

@Preview(showBackground = true)
@Composable
fun Preview() {
	MaterialTheme {
		CustomButton(
			text = "Submit",
			onClick = {}
		)
	}
}