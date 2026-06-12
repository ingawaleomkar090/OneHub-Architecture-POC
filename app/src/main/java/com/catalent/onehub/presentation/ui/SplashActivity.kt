//package com.catalent.onehub.presentation.ui
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.runtime.LaunchedEffect
//import kotlinx.coroutines.delay
//import androidx.core.graphics.toColorInt
//import com.catalent.onehub.presentation.screen.SplashScreen
//import com.catalent.onehub.ui.theme.CatalentOneHubTheme
//
//@SuppressLint("CustomSplashScreen")
//class SplashActivity : ComponentActivity() {
//
//	override fun onCreate(savedInstanceState: Bundle?) {
//		super.onCreate(savedInstanceState)
//		window.statusBarColor = "#1B2A4A".toColorInt()
//		window.navigationBarColor = "#1B2A4A".toColorInt()
//		enableEdgeToEdge()
//		setContent {
//			CatalentOneHubTheme {
//				SplashScreen()
//				LaunchedEffect(Unit) {
//					delay(2000)
//					val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
//					val onboardingDone = prefs.getBoolean("onboarding_done", false)
//					if (onboardingDone) {
//						startActivity(Intent(this@SplashActivity, MainActivity::class.java))
//					} else {
//						startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
//					}
//					finish()
//				}
//			}
//		}
//	}
//}