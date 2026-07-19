package com.tenco.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ChartSlice(val label: String, val value: Float, val color: Color)

/** Animated donut chart with a center label. */
@Composable
fun DonutChart(
    slices: List<ChartSlice>,
    centerTitle: String,
    centerValue: String,
    modifier: Modifier = Modifier,
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)
    val anim by animateFloatAsState(targetValue = 1f, animationSpec = tween(900), label = "donut")
    Box(modifier.size(180.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(180.dp)) {
            val stroke = 30f
            val inset = stroke / 2
            var startAngle = -90f
            slices.forEach { s ->
                val sweep = (s.value / total) * 360f * anim
                drawArc(
                    color = s.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(width = stroke, cap = StrokeCap.Butt),
                )
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerValue, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(centerTitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Legend row for a chart slice. */
@Composable
fun ChartLegend(slices: List<ChartSlice>, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        slices.forEach { s ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(s.color))
                Text(s.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

/** Animated horizontal bar (0..1 fraction). Caller's modifier should set the width. */
@Composable
fun StatBar(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    val anim by animateFloatAsState(targetValue = fraction.coerceIn(0.0001f, 1f), animationSpec = tween(700), label = "bar")
    Box(
        modifier
            .androidxFillMaxWidth()
            .height(10.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            Modifier
                .androidxFillMaxWidthFraction(anim)
                .height(10.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}

private fun Modifier.androidxFillMaxWidth(): Modifier = this.then(Modifier.fillMaxWidth())

private fun Modifier.androidxFillMaxWidthFraction(fraction: Float): Modifier =
    this.then(Modifier.fillMaxWidth(fraction.coerceIn(0.0001f, 1f)))
