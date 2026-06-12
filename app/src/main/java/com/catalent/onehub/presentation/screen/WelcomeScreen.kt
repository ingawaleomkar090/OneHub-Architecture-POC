package com.catalent.onehub.presentation.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catalent.onehub.R
import com.catalent.onehub.presentation.data.WelcomePage

@Composable
fun WelcomeScreen(onFinish: () -> Unit) {
	val notificationPermissionLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission()
	) { _ ->
		onFinish() // allow or deny both go to home
	}
	val pages = listOf(
		WelcomePage(
			imageRes = R.drawable.container,
			title = stringResource(R.string.onboarding_title_1),
			description = stringResource(R.string.onboarding_desc_1),
			buttonText = stringResource(R.string.onboarding_button_1),
			showSkip = true,
		),
		WelcomePage(
			imageRes = R.drawable.container_2,
			title = stringResource(R.string.onboarding_title_2),
			description = stringResource(R.string.onboarding_desc_2),
			buttonText = stringResource(R.string.onboarding_button_2),
			showSkip = true,
		),
		WelcomePage(
			imageRes = R.drawable.container_3,
			title = stringResource(R.string.onboarding_title_3),
			description = stringResource(R.string.onboarding_desc_3),
			buttonText = stringResource(R.string.onboarding_button_3),
			showSkip = false,
		),
	)
	var currentPage by remember { mutableIntStateOf(0) }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.White)
			.padding(horizontal = 24.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Spacer(modifier = Modifier.weight(1f))

		androidx.compose.foundation.Image(
			painter = painterResource(id = pages[currentPage].imageRes),
			contentDescription = null,
			modifier = Modifier.size(240.dp),
		)

		Spacer(modifier = Modifier.height(48.dp))

		Text(
			text = pages[currentPage].title,
			fontSize = 22.sp,
			fontWeight = FontWeight.Bold,
			color = Color(0xFF1B2A4A),
			textAlign = TextAlign.Center,
		)

		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = pages[currentPage].description,
			fontSize = 15.sp,
			color = Color.Gray,
			textAlign = TextAlign.Center,
			lineHeight = 22.sp,
		)

		Spacer(modifier = Modifier.weight(1f))
		// dot indicators
		Row(
			horizontalArrangement = Arrangement.Center,
			modifier = Modifier.fillMaxWidth(),
		) {
			pages.forEachIndexed { index, _ ->
				Box(
					modifier = Modifier
						.padding(horizontal = 4.dp)
						.size(if (index == currentPage) 10.dp else 8.dp)
						.background(
							color = if (index == currentPage) Color(0xFF1B2A4A) else Color.LightGray,
							shape = RoundedCornerShape(50),
						)
				)
			}
		}

		Spacer(modifier = Modifier.height(24.dp))

		Button(
			onClick = {
				if (currentPage < pages.size - 1) {
					currentPage++
				} else {
					// last page — trigger system notification permission popup
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
						notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
					} else {
						onFinish()
					}
				}
			},
			modifier = Modifier
				.fillMaxWidth()
				.height(52.dp),
			shape = RoundedCornerShape(50),
			colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2A4A)),
		) {
			Text(
				text = pages[currentPage].buttonText,
				fontSize = 16.sp,
				fontWeight = FontWeight.SemiBold,
				color = Color.White,
			)
		}

		Spacer(modifier = Modifier.height(16.dp))
		// show Skip on first two pages, Not Now on last page
		if (pages[currentPage].showSkip) {
			TextButton(onClick = onFinish) {
				Text(text = "Skip", color = Color.Gray, fontSize = 15.sp)
			}
		} else {
			TextButton(onClick = onFinish) {
				Text(text = "Not Now", color = Color.Gray, fontSize = 15.sp)
			}
		}

		Spacer(modifier = Modifier.height(24.dp))
	}
}