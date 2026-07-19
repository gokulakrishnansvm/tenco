package com.tenco.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tenco.core.Money
import com.tenco.ui.theme.Gradients

/** Floating rounded card with soft elevation. */
@Composable
fun TencoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val base = Modifier
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    Card(
        modifier = modifier.then(base),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) { content() }
}

/** An animated rupee amount (counts up from 0 to the target). */
@Composable
fun AnimatedRupees(
    paise: Long,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayMedium,
    color: Color = Color.Unspecified,
) {
    val target = paise / 100f
    val animated by animateFloatAsState(targetValue = target, animationSpec = tween(900), label = "rupees")
    Text(text = Money.formatShort((animated * 100).toLong()), style = style, color = color, fontWeight = FontWeight.Bold)
}

/** Gradient hero earnings card. */
@Composable
fun HeroEarningsCard(
    label: String,
    paise: Long,
    caption: String,
    modifier: Modifier = Modifier,
    gradient: Brush = Gradients.hero,
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(gradient)
            .padding(24.dp),
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(8.dp))
            AnimatedRupees(paise, style = MaterialTheme.typography.displayLarge, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text(caption, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

/** Compact summary chip (icon + value + label) used in a row under the hero. */
@Composable
fun SummaryChip(
    icon: ImageVector,
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    TencoCard(modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.15f), modifier = Modifier.size(34.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Colorful gradient quick-action tile. */
@Composable
fun QuickActionTile(
    icon: ImageVector,
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(MaterialTheme.shapes.large)
            .background(Gradients.tile(accent))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Surface(shape = RoundedCornerShape(14.dp), color = accent, modifier = Modifier.size(44.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
        }
        Spacer(Modifier.height(12.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    }
}

/** Section header with optional trailing action text. */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(
        modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        trailing?.invoke()
    }
}
