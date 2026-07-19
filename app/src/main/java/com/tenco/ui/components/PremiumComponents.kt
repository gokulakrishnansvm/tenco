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
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
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

/** Compact summary chip (icon + animated value + label) used in a row under the hero. */
@Composable
fun SummaryChip(
    icon: ImageVector,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    value: @Composable () -> Unit,
) {
    TencoCard(modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.15f), modifier = Modifier.size(34.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.height(10.dp))
            value()
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Animated integer that counts up to [target]. */
@Composable
fun AnimatedCount(target: Int, style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium, color: Color = MaterialTheme.colorScheme.onSurface) {
    val v by androidx.compose.animation.core.animateIntAsState(targetValue = target, animationSpec = androidx.compose.animation.core.tween(900), label = "count")
    Text("$v", style = style, color = color, fontWeight = FontWeight.Bold)
}

/** Animated rupee value (short form) that counts up. */
@Composable
fun AnimatedMoneyShort(paise: Long, style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium, color: Color = MaterialTheme.colorScheme.onSurface) {
    val v by androidx.compose.animation.core.animateFloatAsState(targetValue = paise / 100f, animationSpec = androidx.compose.animation.core.tween(900), label = "money")
    Text(com.tenco.core.Money.formatShort((v * 100).toLong()), style = style, color = color, fontWeight = FontWeight.Bold)
}

/** Wraps content with a subtle staggered fade + slide-up entrance (by [index]). */
@Composable
fun EntranceItem(index: Int, content: @Composable () -> Unit) {
    var visible by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 55L)
        visible = true
    }
    val alpha by androidx.compose.animation.core.animateFloatAsState(if (visible) 1f else 0f, androidx.compose.animation.core.tween(380), label = "entAlpha")
    val ty by androidx.compose.animation.core.animateFloatAsState(if (visible) 0f else 42f, androidx.compose.animation.core.tween(380), label = "entTy")
    Box(Modifier.graphicsLayer { this.alpha = alpha; translationY = ty }) { content() }
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
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy),
        label = "tilePress",
    )
    Column(
        modifier
            .scale(scale)
            .clip(MaterialTheme.shapes.large)
            .background(Gradients.tile(accent))
            .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple()) { onClick() }
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
