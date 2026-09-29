package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = CyberNavyDark,
    primaryContainer = CyberNavySurface,
    onPrimaryContainer = CyberCyan,
    secondary = CyberPurple,
    onSecondary = CyberNavyDark,
    secondaryContainer = CyberNavySurface,
    onSecondaryContainer = CyberPurple,
    tertiary = CyberIndigo,
    onTertiary = TextPrimary,
    background = CyberNavyDark,
    onBackground = TextPrimary,
    surface = CyberNavyCard,
    onSurface = TextPrimary,
    surfaceVariant = CyberNavySurface,
    onSurfaceVariant = TextSecondary,
    outline = CyberNavyBorder,
    outlineVariant = CyberNavyLight,
    error = SecurityRed,
    onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = CyberBlue,
    onPrimary = TextPrimary,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = CyberBlue,
    secondary = CyberIndigo,
    onSecondary = TextPrimary,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = SecurityRed,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Dark-first cybersecurity aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
