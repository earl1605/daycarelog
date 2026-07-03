package com.daycarelog.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import com.daycarelog.app.data.preferences.ThemeState
import com.daycarelog.app.navigation.DaycareLogNavGraph
import com.daycarelog.app.ui.theme.DaycareLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaunchedEffect(Unit) { ThemeState.init(applicationContext) }
            DaycareLogTheme {
                DaycareLogNavGraph()
            }
        }
    }
}
