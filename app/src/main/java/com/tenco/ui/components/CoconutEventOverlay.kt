package com.tenco.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.graphicsLayer
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
        val easing = if (event == CoconutEvent.TRUCK) LinearEasing else FastOutSlowInEasing
        progress.animateTo(1f, tween(durationMillis = if (event == CoconutEvent.TRUCK) 1600 else 1500, easing = easing))
        onEnd()
    }
    val p = progress.value
    val scrim = if (event == CoconutEvent.SPOILED) Color(0x33B00020) else Color(0x1F000000)
    Box(Modifier.fillMaxSize().background(scrim), contentAlignment = Alignment.Center) {
        when (event) {
            CoconutEvent.TRUCK -> {
                // Constant-speed drive left→right; mirror the emoji so it faces its direction.
                val x = (-340 + 680 * p).dp
                Text("🚚", fontSize = 88.sp, modifier = Modifier.offset(x = x).graphicsLayer(scaleX = -1f))
            }
            CoconutEvent.HARVEST -> {
                Confetti(particleCount = 70)
                // Smooth pop-in that settles (no jitter).
                val s = 0.5f + 0.6f * (p * 2.4f).coerceAtMost(1f)
                Text("🥥", fontSize = 96.sp, modifier = Modifier.scale(s).alpha((p * 4f).coerceAtMost(1f)))
            }
            CoconutEvent.SPOILED -> {
                // One gentle sway + tilt while fading out.
                val sway = (sin(p * 6.2832f) * 6f).dp
                val tilt = sin(p * 3.1416f) * 14f
                Text(
                    "🥥",
                    fontSize = 96.sp,
                    modifier = Modifier.offset(x = sway).rotate(tilt).alpha((1f - p).coerceIn(0f, 1f)),
                )
            }
        }
    }
}
