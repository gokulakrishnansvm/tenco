package com.tenco.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Adds an animated shimmer sweep, for skeleton loading placeholders. */
@Composable
fun Modifier.shimmer(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = -400f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(tween(1300), RepeatMode.Restart),
        label = "x",
    )
    val shimmerColors = listOf(
        Color(0x33B0BEC5), Color(0x66ECEFF1), Color(0x33B0BEC5),
    )
    return this.background(
        Brush.linearGradient(shimmerColors, start = Offset(x, 0f), end = Offset(x + 400f, 400f)),
    )
}

/** A shimmering placeholder block. */
@Composable
fun SkeletonBlock(modifier: Modifier = Modifier, height: Int = 20, corner: Int = 8) {
    Column(modifier.clip(RoundedCornerShape(corner.dp)).height(height.dp).shimmer()) {}
}

/** A skeleton card row (avatar + two lines). */
@Composable
fun SkeletonCard() {
    TencoCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            SkeletonBlock(Modifier.fillMaxWidth(0.5f), height = 18)
            SkeletonBlock(Modifier.fillMaxWidth(0.8f).padding(top = 10.dp), height = 14)
        }
    }
}
