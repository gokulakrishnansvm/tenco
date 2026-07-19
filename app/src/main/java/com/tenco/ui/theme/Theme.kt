package com.tenco.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = DeepCoconutGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEBBF),
    onPrimaryContainer = Color(0xFF10380F),
    secondary = FreshCoconutGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4F3CF),
    onSecondaryContainer = DeepCoconutGreen,
    tertiary = CoconutHusk,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEFE0D4),
    onTertiaryContainer = Color(0xFF3E2C22),
    background = Color(0xFFF4FAE9),
    onBackground = Color(0xFF14210F),
    surface = Color(0xFFFCFEF7),
    onSurface = Color(0xFF16241A),
    surfaceVariant = Color(0xFFE6EFD8),
    onSurfaceVariant = Color(0xFF515F4C),
    outline = Color(0xFFC7D3B8),
)

private val DarkColors = darkColorScheme(
    primary = FreshCoconutGreen,
    onPrimary = Color(0xFF03210B),
    primaryContainer = Color(0xFF1F3A26),
    onPrimaryContainer = FreshLime,
    secondary = FreshLime,
    onSecondary = Color(0xFF12210A),
    tertiary = CoconutHusk,
    background = DarkBg,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = Color(0xFF9FB0A6),
    outline = Color(0xFF33403A),
)

private val TencoShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Brand gradients (usable outside composition). */
object Gradients {
    val hero = Brush.linearGradient(listOf(Color(0xFF1B5E20), Color(0xFF388E3C), Color(0xFF43A047)))
    val lime = Brush.linearGradient(listOf(FreshCoconutGreen, FreshLime))
    val heroDark = Brush.linearGradient(listOf(Color(0xFF13351C), Color(0xFF1E4D28)))
    fun tile(accent: Color) = Brush.linearGradient(listOf(accent.copy(alpha = 0.16f), accent.copy(alpha = 0.06f)))
}

@Composable
fun TencoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = TencoTypography,
        shapes = TencoShapes,
        content = content,
    )
}
