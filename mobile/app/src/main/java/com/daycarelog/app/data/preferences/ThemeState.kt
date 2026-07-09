package com.daycarelog.app.data.preferences

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.first

object ThemeState {
    var isDarkMode by mutableStateOf(false)
        private set

    suspend fun init(context: Context) {
        isDarkMode = TokenDataStore.getTheme(context).first() == "dark"
    }

    suspend fun toggle(context: Context) {
        isDarkMode = !isDarkMode
        TokenDataStore.saveTheme(context, if (isDarkMode) "dark" else "light")
    }
}
