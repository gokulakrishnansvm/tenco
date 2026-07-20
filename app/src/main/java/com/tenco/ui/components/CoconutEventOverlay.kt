package com.tenco.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import kotlin.math.sin

enum class CoconutEvent { TRUCK, HARVEST, SPOILED }

/** Brief celebratory/feedback animation overlay for supply-chain events. Auto-dismisses. */
@Composable
fun CoconutEventOverlay(event: CoconutEvent, onEnd: () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(if (event == CoconutEvent.TRUCK) 1500 else 1400, easing = LinearEasing))
        onEnd()
    }
    val p = progress.value
    val scrim = if (event == CoconutEvent.SPOILED) Color(0x33B00020) else Color(0x22000000)
    Box(Modifier.fillMaxSize().background(scrim), contentAlignment = Alignment.Center) {
        when (event) {
            CoconutEvent.TRUCK -> {
                // Drive across the screen with a subtle bob.
                val x = (-320 + 640 * p).dp
                val bob = (sin(p * 20f) * 3f).dp
                Text("🚚", fontSize = 84.sp, modifier = Modifier.offset(x = x, y = bob))
            }
            CoconutEvent.HARVEST -> {
                // Coconut drops raining down + a big bounce coconut.
                Confetti(particleCount = 70)
                val scale = 0.6f + 0.6f * sin(p * Math.PI.toFloat())
                Text("🥥", fontSize = 90.sp, modifier = Modifier.scale(scale.coerceIn(0.4f, 1.1f)))
            }
            CoconutEvent.SPOILED -> {
                // A wilting, shaking coconut that fades.
                val shake = (sin(p * 40f) * 8f).dp
                Text(
                    "🥥",
                    fontSize = 90.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = shake).rotate(p * 25f).alpha((1f - p).coerceIn(0f, 1f)),
                )
            }
        }
    }
}
