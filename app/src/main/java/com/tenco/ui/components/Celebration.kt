package com.tenco.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tenco.ui.theme.FreshLime
import com.tenco.ui.theme.StatusFailed
import com.tenco.ui.theme.SuccessGreen
import kotlin.math.sin
import kotlin.random.Random

private data class Confetto(
    val x: Float, val startY: Float, val fall: Float, val w: Float,
    val color: Color, val spin: Float, val wobbleAmp: Float, val wobbleFreq: Float,
)

/** Lightweight confetti burst (no external lib). Draws falling, spinning particles once. */
@Composable
fun Confetti(modifier: Modifier = Modifier, particleCount: Int = 90) {
    val colors = listOf(SuccessGreen, FreshLime, Color(0xFF2E86DE), Color(0xFFF57C00), Color(0xFF7E57C2))
    val particles = remember {
        List(particleCount) {
            Confetto(
                x = Random.nextFloat(),
                startY = -Random.nextFloat() * 0.3f,
                fall = 0.8f + Random.nextFloat() * 0.6f,
                w = 10f + Random.nextFloat() * 14f,
                color = colors[Random.nextInt(colors.size)],
                spin = Random.nextFloat() * 720f,
                wobbleAmp = 20f + Random.nextFloat() * 40f,
                wobbleFreq = 4f + Random.nextFloat() * 6f,
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(2600, easing = LinearEasing)) }

    Canvas(modifier.fillMaxSize()) {
        val p = progress.value
        particles.forEach { c ->
            val y = size.height * (c.startY + p * c.fall)
            val x = size.width * c.x + sin(p * c.wobbleFreq) * c.wobbleAmp
            val alpha = (1f - p).coerceIn(0f, 1f)
            rotate(degrees = p * c.spin, pivot = Offset(x, y)) {
                drawRect(color = c.color.copy(alpha = alpha), topLeft = Offset(x, y), size = Size(c.w, c.w * 1.6f))
            }
        }
    }
}

/** Full-screen success / failure celebration overlay. */
@Composable
fun PaymentResultOverlay(
    success: Boolean,
    amountText: String,
    title: String,
    onDone: () -> Unit,
) {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            val color = if (success) SuccessGreen else StatusFailed
            Surface(shape = CircleShape, color = color, modifier = Modifier.size(96.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (success) Icons.Rounded.Check else Icons.Rounded.Close,
                        contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp),
                    )
                }
            }
            Text(amountText, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 24.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 32.dp)) {
                Text("Done", style = MaterialTheme.typography.titleMedium)
            }
        }
        if (success) Confetti()
    }
}
