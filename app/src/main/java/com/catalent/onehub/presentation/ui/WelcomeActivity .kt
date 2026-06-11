package com.catalent.onehub.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.content.Intent
import androidx.core.content.edit
import com.catalent.onehub.presentation.screen.WelcomeScreen

class WelcomeActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			WelcomeScreen(
				onFinish = {
					getSharedPreferences("prefs", MODE_PRIVATE)
						.edit { putBoolean("onboarding_done", true) }
					startActivity(Intent(this, MainActivity::class.java))
					finish()
				}
			)
		}
	}
}