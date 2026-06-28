package com.daycarelog.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.daycarelog.app.navigation.DaycareLogNavGraph
import com.daycarelog.app.ui.theme.DaycareLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DaycareLogTheme {
                DaycareLogNavGraph()
            }
        }
    }
}
