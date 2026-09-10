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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.data.model.BillingCycle
import com.bhakti.app.data.model.SubscriptionPlan
import com.bhakti.app.core.navigation.Routes

private val benefits = listOf(
    "Devotional wallpapers refreshed regularly",
    "Bhajans, aartis and chalisas",
    "Mantras with meaning and audio",
    "Daily WhatsApp status content"
)

@Composable
fun PaywallScreen(navController: NavHostController) {
    var selectedPlan by remember { mutableStateOf(SubscriptionPlan.ALL.first { it.cycle == BillingCycle.ANNUAL }) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Bring Bhakti Into Your Everyday Life.", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Mantras, Bhajans, Divine Wallpapers and Daily Devotional Content in one place.",
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

        SubscriptionPlan.ALL.forEach { plan ->
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
