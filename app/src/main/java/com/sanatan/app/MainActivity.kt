package com.sanatan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sanatan.app.core.navigation.SanatanApp
import com.sanatan.app.ui.theme.SanatanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SanatanTheme {
                SanatanApp()
            }
        }
    }
}
