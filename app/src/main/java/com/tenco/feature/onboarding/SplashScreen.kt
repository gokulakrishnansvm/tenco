package com.tenco.feature.onboarding

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tenco.R
import com.tenco.ui.theme.Gradients

@Composable
fun SplashScreen() {
    var started by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (started) 1f else 0.6f, tween(700), label = "logo")
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(tween(1100), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "pulse",
    )
    androidx.compose.runtime.LaunchedEffect(Unit) { started = true }

    Box(Modifier.fillMaxSize().background(Gradients.hero), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.18f), modifier = Modifier.size(120.dp).scale(scale * pulse)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Eco, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                }
            }
            Text(
                stringResource(R.string.app_name),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                modifier = Modifier.padding(top = 24.dp),
            )
            Text(stringResource(R.string.app_tagline), color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.titleMedium)
        }
    }
}
