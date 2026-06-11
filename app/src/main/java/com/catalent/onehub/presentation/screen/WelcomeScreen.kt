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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catalent.onehub.R
import com.catalent.onehub.presentation.data.WelcomePage

@Composable
fun WelcomeScreen(onFinish: () -> Unit) {
	val pages = listOf(
		WelcomePage(
			imageRes = R.drawable.container,
			title = "Welcome to OneHub!",
			description = "Manage your biologics and shipments all in one place. Your centralized platform for global logistics.",
			buttonText = "Next",
			showSkip = true,
		),
		WelcomePage(
			imageRes = R.drawable.container_2,
			title = "Track with precision",
			description = "Filter shipments by site, customer, and protocol. Favorite your most important hubs for quick access.",
			buttonText = "Next",
			showSkip = true,
		),
		WelcomePage(
			imageRes = R.drawable.container_3,
			title = "Stay updated",
			description = "Turn on notifications to get real-time alerts on shipment statuses, delays, and critical updates.",
			buttonText = "Allow Notifications",
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
////		 dot indicators
//		Row(
//			horizontalArrangement = Arrangement.Center,
//			modifier = Modifier.fillMaxWidth(),
//		) {
//			pages.forEachIndexed { index, _ ->
//				Box(
//					modifier = Modifier
//						.padding(horizontal = 4.dp)
//						.size(if (index == currentPage) 10.dp else 8.dp)
//						.background(
//							color = if (index == currentPage) Color(0xFF1B2A4A) else Color.LightGray,
//							shape = RoundedCornerShape(50),
//						)
//				)
//			}
//		}
//		Spacer(modifier = Modifier.height(24.dp))
		Button(
			onClick = {
				if (currentPage < pages.size - 1) currentPage++
				else onFinish()
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

		Spacer(modifier = Modifier.height(40.dp))
//		if (pages[currentPage].showSkip) {
//			TextButton(onClick = onFinish) {
//				Text(text = "Skip", color = Color.Gray, fontSize = 15.sp)
//			}
//		} else {
//			TextButton(onClick = onFinish) {
//				Text(text = "Not Now", color = Color.Gray, fontSize = 15.sp)
//			}
//		}

//		Spacer(modifier = Modifier.height(24.dp))
	}
}