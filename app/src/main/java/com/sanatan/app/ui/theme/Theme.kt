package com.sanatan.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = SaffronDeep,
    onPrimary = Color_White,
    primaryContainer = SaffronLight,
    onPrimaryContainer = Ink,
    secondary = Vermilion,
    onSecondary = Color_White,
    background = Sandal,
    onBackground = Ink,
    surface = Color_White,
    onSurface = Ink,
    surfaceVariant = Sandal,
    onSurfaceVariant = InkSoft
)

private val DarkColors = darkColorScheme(
    primary = Marigold,
    onPrimary = Ink,
    primaryContainer = SaffronDeep,
    onPrimaryContainer = Sandal,
    secondary = SaffronLight,
    onSecondary = Ink,
    background = NightBrown,
    onBackground = Sandal,
    surface = NightSurface,
    onSurface = Sandal,
    surfaceVariant = NightSurface,
    onSurfaceVariant = SaffronLight
)

@Composable
fun SanatanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = SanatanTypography,
        content = content
    )
}
