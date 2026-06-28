package com.example.samaajbot.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = Blue40,
    onPrimary        = Grey99,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary        = Teal40,
    onSecondary      = Grey99,
    secondaryContainer = Teal90,
    onSecondaryContainer = Teal10,
    error            = Red40,
    onError          = Grey99,
    errorContainer   = Red90,
    onErrorContainer = Red10,
    background       = Grey99,
    onBackground     = Grey10,
    surface          = Grey99,
    onSurface        = Grey10,
    surfaceVariant   = Grey95,
    onSurfaceVariant = Grey20,
    outline          = Grey90,
)

private val DarkColorScheme = darkColorScheme(
    primary          = Blue80,
    onPrimary        = Blue20,
    primaryContainer = Blue10,
    onPrimaryContainer = Blue90,
    secondary        = Teal80,
    onSecondary      = Teal20,
    secondaryContainer = Teal10,
    onSecondaryContainer = Teal90,
    error            = Red80,
    onError          = Red10,
    errorContainer   = Red40,
    onErrorContainer = Red90,
    background       = Grey10,
    onBackground     = Grey90,
    surface          = Grey10,
    onSurface        = Grey90,
    surfaceVariant   = Grey20,
    onSurfaceVariant = Grey90,
    outline          = Grey20,
)

@Composable
fun SamaajBotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
