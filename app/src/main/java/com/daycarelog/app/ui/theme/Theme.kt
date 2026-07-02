package com.daycarelog.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Fixed brand color scheme — intentionally NOT using dynamicColorScheme/dynamicLightColorScheme.
// Brand consistency with the web app matters more than adapting to the device wallpaper.
private val LightColorScheme = lightColorScheme(
    primary             = Green40,
    onPrimary           = White,
    primaryContainer    = Green95,
    onPrimaryContainer  = Green10,
    secondary           = Green30,
    onSecondary         = White,
    background          = OffWhite,
    onBackground        = Charcoal,
    surface             = White,
    onSurface           = Charcoal,
    surfaceVariant      = CardSurface,
    onSurfaceVariant    = MutedGray,
    outline             = BorderGray,
    error               = Red40,
    onError             = White,
)

@Composable
fun DaycareLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        content     = content
    )
}
