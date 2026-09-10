package com.bhakti.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.navigation.BhaktiApp
import com.bhakti.app.ui.theme.BhaktiTheme

class MainActivity : ComponentActivity() {

    private var pendingDeepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingDeepLink = intent?.getStringExtra(EXTRA_DEEP_LINK_ROUTE)
        val container = (application as BhaktiApplication).container
        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                BhaktiTheme {
                    BhaktiApp(
                        pendingDeepLink = pendingDeepLink,
                        onDeepLinkConsumed = { pendingDeepLink = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLink = intent.getStringExtra(EXTRA_DEEP_LINK_ROUTE)
    }

    companion object {
        const val EXTRA_DEEP_LINK_ROUTE = "deep_link_route"
    }
}
