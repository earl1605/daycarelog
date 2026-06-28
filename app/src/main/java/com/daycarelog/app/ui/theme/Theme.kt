package com.daycarelog.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary        = Green40,
    onPrimary      = White,
    primaryContainer    = Green90,
    onPrimaryContainer  = Green10,
    secondary      = Green30,
    onSecondary    = White,
    background     = Gray90,
    onBackground   = Gray10,
    surface        = White,
    onSurface      = Gray10,
    error          = Red40,
    onError        = White,
)

@Composable
fun DaycareLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography,
        content     = content
    )
}
