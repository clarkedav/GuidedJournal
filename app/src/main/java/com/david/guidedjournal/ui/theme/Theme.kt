package com.david.guidedjournal.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = White,

    secondary = BlueGrey40,
    onSecondary = White,

    tertiary = Accent40,
    onTertiary = White,

    background = White,
    onBackground = Blue40,

    surface = White,
    onSurface = Blue40,

    primaryContainer = BlueGrey80,
    onPrimaryContainer = Blue40,

    secondaryContainer = Accent40,
    onSecondaryContainer = White
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = White,

    secondary = BlueGrey80,
    onSecondary = Blue40,

    tertiary = Accent80,
    onTertiary = Blue40,

    background = Blue40,
    onBackground = White,

    surface = Blue40,
    onSurface = White,

    primaryContainer = BlueGrey40,
    onPrimaryContainer = White,

    secondaryContainer = Accent40,
    onSecondaryContainer = White
)

@Composable
fun GuidedJournalTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Keep the status bar blue.
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                // White status-bar icons because the status bar is blue.
                isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}