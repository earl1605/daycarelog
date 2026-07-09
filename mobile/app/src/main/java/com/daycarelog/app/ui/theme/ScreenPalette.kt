package com.daycarelog.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.daycarelog.app.data.preferences.ThemeState

data class ScreenPalette(
    val pageBg: Color,
    val cardBg: Color,
    val textColor: Color,
    val mutedColor: Color,
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
