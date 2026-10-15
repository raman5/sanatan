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
import com.bhakti.app.core.payments.RazorpayCheckoutBridge
import com.bhakti.app.ui.theme.BhaktiTheme
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

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

    // Razorpay Checkout calls back on the Activity that opened it.
    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) =
        RazorpayCheckoutBridge.onPaymentSuccess(razorpayPaymentId, paymentData)

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) =
        RazorpayCheckoutBridge.onPaymentError(code, response)

    companion object {
        const val EXTRA_DEEP_LINK_ROUTE = "deep_link_route"
    }
}
