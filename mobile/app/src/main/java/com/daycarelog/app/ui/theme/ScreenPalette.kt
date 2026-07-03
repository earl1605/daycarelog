package com.daycarelog.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.daycarelog.app.data.preferences.ThemeState

// Shared set of theme-aware colors for the per-screen Column/Card layouts across the
// app (Children, Attendance, Health, Guardians, Reports, Settings, Users, ...), which
// use plain Color(0xFF...) literals rather than MaterialTheme.colorScheme references.
// Centralizing the light/dark pick here keeps every screen consistent with the same
// two-tier surface relationship used by MainScreen/DashboardScreen.
data class ScreenPalette(
    val pageBg: Color,     // the light-green-tinted screen background (was Color(0xFFf0fdf4))
    val cardBg: Color,     // white cards (was Color.White)
    val textColor: Color,  // primary text (was Color(0xFF111827) / Color.Black-ish)
    val mutedColor: Color, // secondary/gray text (was Color.Gray)
    val borderColor: Color,
)

private val LightPageBg = Color(0xFFF0FDF4)

@Composable
fun rememberScreenPalette(): ScreenPalette {
    val isDark = ThemeState.isDarkMode
    return ScreenPalette(
        pageBg     = if (isDark) DarkBackground else LightPageBg,
        cardBg     = if (isDark) DarkSurface else White,
        textColor  = if (isDark) DarkOnBackground else Charcoal,
        mutedColor = if (isDark) DarkMutedGray else MutedGray,
        borderColor = if (isDark) DarkBorderGray else BorderGray,
    )
}
