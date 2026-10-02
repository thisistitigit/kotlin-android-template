package com.impostor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.impostor.app.ui.ImpostorApp
import com.impostor.app.ui.theme.ImpostorTheme

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImpostorTheme {
                ImpostorApp()
            }
        }
    }
}
