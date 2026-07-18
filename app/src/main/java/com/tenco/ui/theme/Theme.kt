package com.tenco.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = CoconutGreen,
    onPrimary = Color.White,
    primaryContainer = CoconutLight,
    onPrimaryContainer = CoconutGreenDark,
    secondary = TenderYellow,
    onSecondary = Color.Black,
    background = Cream,
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = CoconutLight,
    onPrimary = CoconutGreenDark,
    primaryContainer = CoconutGreenDark,
    onPrimaryContainer = CoconutLight,
    secondary = TenderYellow,
    onSecondary = Color.Black,
)

@Composable
fun TencoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = TencoTypography,
        content = content,
    )
}
