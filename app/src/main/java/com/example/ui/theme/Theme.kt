package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = CardBackground,
    primaryContainer = NavySidebarBorder,
    onPrimaryContainer = DarkTextPrimary,
    secondary = AccentPurple,
    onSecondary = CardBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkBorder,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = ErrorRed,
    onError = CardBackground
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = CardBackground,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = PrimaryBlueHover,
    secondary = AccentPurple,
    onSecondary = CardBackground,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = ErrorRed,
    onError = CardBackground
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
