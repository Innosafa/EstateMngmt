package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Indigo500,
    onPrimary = Color.White,
    primaryContainer = Indigo900,
    onPrimaryContainer = Indigo100,
    secondary = Emerald500,
    onSecondary = Color.White,
    secondaryContainer = Slate800,
    onSecondaryContainer = Indigo200,
    tertiary = Amber500,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Slate400,
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    primaryContainer = Indigo50,
    onPrimaryContainer = Indigo900,
    secondary = Emerald600,
    onSecondary = Color.White,
    secondaryContainer = Emerald50,
    onSecondaryContainer = Emerald700,
    tertiary = Amber600,
    background = PolishBackground,
    surface = PolishSurface,
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = PolishSurfaceVariant,
    onSurfaceVariant = Slate600,
    outline = PolishBorder
)

@Composable
fun EstateIQTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    EstateIQTheme(darkTheme = darkTheme, content = content)
}

