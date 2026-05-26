package com.example.livent.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LiventPrimaryDarkTheme,
    onPrimary = LiventOnPrimary,
    secondary = LiventSecondaryDark,
    onSecondary = LiventOnBackgroundDark,
    background = LiventBackgroundDark,
    onBackground = LiventOnBackgroundDark,
    surface = LiventSurfaceDark,
    onSurface = LiventOnSurfaceDark,
    onSurfaceVariant = LiventOnSurfaceDark,
    outline = LiventOutline,
    error = LiventError
)

private val LightColorScheme = lightColorScheme(
    primary = LiventPrimary,
    onPrimary = LiventOnPrimary,
    secondary = LiventSecondary,
    onSecondary = LiventOnSecondary,
    background = LiventBackground,
    onBackground = LiventOnBackground,
    surface = LiventSurface,
    onSurface = LiventOnSurface,
    onSurfaceVariant = LiventOnSurfaceVariant,
    outline = LiventOutline,
    error = LiventError
)

@Composable
fun LiventTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desactivado por defecto para respetar la marca
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
