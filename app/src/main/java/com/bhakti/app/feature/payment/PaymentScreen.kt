package com.bhakti.app.feature.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.core.payments.PaymentCancelledException
import com.bhakti.app.core.payments.RazorpayCheckoutBridge
import com.bhakti.app.core.ui.findActivity
import com.bhakti.app.data.model.BillingCycle
import com.bhakti.app.data.model.SubscriptionPlan
import kotlinx.coroutines.launch

@Composable
fun PaymentScreen(navController: NavHostController, planId: String) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var plan by remember {
        mutableStateOf(SubscriptionPlan.ALL.firstOrNull { it.id == planId } ?: SubscriptionPlan.ALL.first())
    }
    LaunchedEffect(planId) {
        val plans = container.contentRepository.subscriptionPlans()
        plan = plans.firstOrNull { it.id == planId } ?: plans.first()
        context.findActivity()?.let { RazorpayCheckoutBridge.preload(it) }
    }

    val session by container.sessionManager.state.collectAsState(initial = null)
    // The webhook can confirm the subscription even if the in-app callback is
    // lost (e.g. the app was killed during payment) - move on as soon as it does.
    LaunchedEffect(session?.hasActiveAccess) {
        if (session?.hasActiveAccess == true) goHome(navController)
    }

    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val period = if (plan.cycle == BillingCycle.MONTHLY) "month" else "year"

    fun pay() {
        val activity = context.findActivity() ?: return
        busy = true
        message = null
        scope.launch {
            val outcome = container.paymentRepository.startSubscription(plan.id)
                .mapCatching { ticket ->
                    RazorpayCheckoutBridge.pay(
                        activity = activity,
                        ticket = ticket,
                        description = "${plan.label} plan - ₹${plan.priceRupees}/$period",
                        email = session?.user?.email,
                        name = session?.user?.displayName
                    ).getOrThrow()
                }
                .mapCatching { result -> container.paymentRepository.verifySubscription(result).getOrThrow() }
            busy = false
            outcome
                .onSuccess { entitlement ->
                    if (entitlement.isActive()) goHome(navController)
                    else message = "Payment received - activating your plan. This can take a minute."
                }
                .onFailure { if (it !is PaymentCancelledException) message = it.message }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Confirm your plan", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Pay securely with UPI AutoPay, card or net banking through Razorpay.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("${plan.label} plan - ₹${plan.priceRupees}/$period", style = MaterialTheme.typography.titleMedium)
                Text(
                    "₹${plan.priceRupees} is charged today and then automatically every $period until you cancel. " +
                        "Cancel any time from Profile - you keep access until the end of the period you paid for.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Button(
            onClick = { pay() },
            enabled = !busy,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            Text("Pay ₹${plan.priceRupees} & Subscribe")
        }

        if (busy) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
        message?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

private fun goHome(navController: NavHostController) {
    navController.navigate(Routes.HOME) {
        popUpTo(Routes.PAYWALL) { inclusive = true }
        launchSingleTop = true
    }
}
