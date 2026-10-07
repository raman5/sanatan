package com.bhakti.app.feature.paywall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.BuildConfig
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.data.model.BillingCycle
import com.bhakti.app.data.model.Subscription
import com.bhakti.app.data.model.SubscriptionPlan
import com.bhakti.app.data.model.SubscriptionStatus
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val benefits = listOf(
    "Devotional wallpapers refreshed regularly",
    "Daily Pooja and Naam Japa with a japa counter",
    "Mantras with meaning and audio",
    "Daily WhatsApp status content"
)

@Composable
fun PaywallScreen(navController: NavHostController) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    // Seeded with the bundled defaults so the screen isn't empty while plans load,
    // then replaced with whatever the backend actually configures (see
    // ContentRepository.subscriptionPlans) - pricing changes need no app release.
    var plans by remember { mutableStateOf(SubscriptionPlan.ALL) }
    var selectedPlan by remember {
        mutableStateOf(SubscriptionPlan.ALL.firstOrNull { it.cycle == BillingCycle.ANNUAL } ?: SubscriptionPlan.ALL.first())
    }
    LaunchedEffect(Unit) {
        plans = container.contentRepository.subscriptionPlans()
        selectedPlan = plans.firstOrNull { it.cycle == BillingCycle.ANNUAL } ?: plans.first()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Bring Bhakti Into Your Everyday Life.", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Daily Pooja, Naam Japa, Mantras, Divine Wallpapers and Daily Devotional Content in one place.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        benefits.forEach { benefit ->
            Text("• $benefit", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 6.dp))
        }

        Text(
            "Choose your plan",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
        )

        plans.forEach { plan ->
            PlanCard(
                plan = plan,
                selected = plan.id == selectedPlan.id,
                onSelect = { selectedPlan = plan }
            )
        }

        Button(
            onClick = { navController.navigate(Routes.payment(selectedPlan.id)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Start My Bhakti Journey")
        }

        Text(
            "Subscription renews automatically at ₹${selectedPlan.priceRupees} every " +
                "${if (selectedPlan.cycle == BillingCycle.MONTHLY) "month" else "year"} until cancelled.",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 12.dp)
        )

        if (BuildConfig.DEBUG) {
            TextButton(
                onClick = {
                    scope.launch {
                        val renewalDate = when (selectedPlan.cycle) {
                            BillingCycle.MONTHLY -> LocalDate.now().plusMonths(1)
                            BillingCycle.ANNUAL -> LocalDate.now().plusYears(1)
                        }
                        container.sessionManager.saveSubscription(
                            Subscription(
                                planId = selectedPlan.id,
                                status = SubscriptionStatus.ACTIVE,
                                renewalDateIso = renewalDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                mandateId = "debug-skip"
                            )
                        )
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.PAYWALL) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Skip payment (debug build only)")
            }
        }
    }
}

@Composable
private fun PlanCard(plan: SubscriptionPlan, selected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .selectable(selected = selected, onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selected, onClick = onSelect)
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(plan.label, style = MaterialTheme.typography.titleMedium)
                    Text("₹${plan.perMonthEquivalent}/month equivalent", style = MaterialTheme.typography.labelSmall)
                    plan.badge?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Text("₹${plan.priceRupees}", style = MaterialTheme.typography.titleMedium)
        }
    }
}
