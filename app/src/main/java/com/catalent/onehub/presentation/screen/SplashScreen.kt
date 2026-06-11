package com.catalent.onehub.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catalent.onehub.R

@Composable
fun SplashScreen() {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color(0xFF1B2A4A)),
		contentAlignment = Alignment.Center
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center,
		) {
			Image(
				painter = painterResource(id = R.drawable.group_2),
				contentDescription = "OneHub Logo",
				modifier = Modifier.size(40.dp),
			)
			Spacer(modifier = Modifier.width(8.dp))
			Row {
				Text(
					text = "ONE",
					fontSize = 28.sp,
					fontWeight = FontWeight.Light,
					color = Color.White,
					letterSpacing = 2.sp,
				)
				Text(
					text = "HUB",
					fontSize = 28.sp,
					fontWeight = FontWeight.Bold,
					color = Color.White,
					letterSpacing = 2.sp,
				)
				Text(
					text = "™",
					fontSize = 12.sp,
					color = Color.White,
					modifier = Modifier.align(Alignment.Top),
				)
			}
		}
	}
}