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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.data.model.SubscriptionPlan
import kotlinx.coroutines.launch

@Composable
fun PaymentScreen(navController: NavHostController, planId: String) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val plan = SubscriptionPlan.ALL.firstOrNull { it.id == planId } ?: SubscriptionPlan.ALL.first()

    var upiId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Set up UPI AutoPay", style = MaterialTheme.typography.headlineMedium)
        Text(
            "The only payment method for launch is a UPI AutoPay mandate.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("${plan.label} plan - ₹${plan.priceRupees}", style = MaterialTheme.typography.titleMedium)
                Text(
                    "You'll be charged ₹1 today to verify your mandate. " +
                        "₹${plan.priceRupees} auto-debits on renewal, and every cycle after, until you cancel.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        OutlinedTextField(
            value = upiId,
            onValueChange = { upiId = it },
            label = { Text("UPI ID") },
            placeholder = { Text("name@bank") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )

        Button(
            onClick = {
                errorMessage = null
                isLoading = true
                scope.launch {
                    val result = container.paymentRepository.setupAutoPayMandate(upiId, plan)
                    isLoading = false
                    result.onSuccess { subscription ->
                        container.sessionManager.saveSubscription(subscription)
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.PAYWALL) { inclusive = true }
                        }
                    }.onFailure { errorMessage = it.message }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = !isLoading && upiId.isNotBlank()
        ) {
            Text("Confirm ₹1 & Set Up AutoPay")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
        }
    }
}
