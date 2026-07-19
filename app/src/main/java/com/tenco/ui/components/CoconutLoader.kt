package com.tenco.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Themed loading indicator: concentric coconut-water ripples radiating outward. */
@Composable
fun CoconutLoader(modifier: Modifier = Modifier, size: Dp = 48.dp, color: Color = MaterialTheme.colorScheme.primary) {
    val transition = rememberInfiniteTransition(label = "ripple")
    val base by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "base",
    )
    Canvas(modifier.size(size)) {
        val maxR = this.size.minDimension / 2f
        listOf(0f, 0.5f).forEach { offset ->
            val p = (base + offset) % 1f
            drawCircle(color = color.copy(alpha = (1f - p).coerceIn(0f, 1f)), radius = p * maxR, style = Stroke(width = 4f))
        }
        drawCircle(color = color, radius = maxR * 0.16f)
    }
}
