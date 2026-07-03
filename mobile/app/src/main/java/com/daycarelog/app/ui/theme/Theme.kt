package com.daycarelog.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.daycarelog.app.data.preferences.ThemeState

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

// Same two-tier surface relationship as light mode (page vs. elevated card/drawer),
// just inverted — see the comment above DarkBackground/DarkSurface in Color.kt.
private val DarkColorScheme = darkColorScheme(
    primary             = Green40,
    onPrimary           = White,
    primaryContainer    = Green30,
    onPrimaryContainer  = Green95,
    secondary           = Green80,
    onSecondary         = Green10,
    background          = DarkBackground,
    onBackground        = DarkOnBackground,
    surface             = DarkBackground,
    onSurface           = DarkOnBackground,
    surfaceVariant      = DarkSurface,
    onSurfaceVariant    = DarkMutedGray,
    outline             = DarkBorderGray,
    error               = Red40,
    onError             = White,
)

@Composable
fun DaycareLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (ThemeState.isDarkMode) DarkColorScheme else LightColorScheme,
        typography  = Typography,
        content     = content
    )
}
