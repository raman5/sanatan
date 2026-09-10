package com.bhakti.app.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.data.model.SubscriptionStatus
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val session by container.sessionManager.state.collectAsState(initial = null)

    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Text("Profile", style = MaterialTheme.typography.displaySmall)

        session?.user?.let { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(user.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(user.email ?: user.phone.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        session?.subscription?.let { sub ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Subscription", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Plan: ${sub.planId?.replaceFirstChar { it.uppercase() } ?: "None"} • ${sub.status.label()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    sub.renewalDateIso?.let {
                        Text("Renews on $it", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        ProfileLink("Notifications") { navController.navigate(Routes.NOTIFICATION_SETTINGS) }
        ProfileLink("Language") { navController.navigate(Routes.LANGUAGE) }
        ProfileLink("Help & Support") { navController.navigate(Routes.HELP_SUPPORT) }
        ProfileLink("Terms & Privacy Policy") { navController.navigate(Routes.LEGAL) }

        Button(
            onClick = {
                scope.launch {
                    container.sessionManager.signOut()
                    navController.navigate(Routes.AUTH) { popUpTo(0) }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Logout")
        }
    }
}

private fun SubscriptionStatus.label(): String = when (this) {
    SubscriptionStatus.NONE -> "Not subscribed"
    SubscriptionStatus.TRIAL -> "Trial"
    SubscriptionStatus.ACTIVE -> "Active"
    SubscriptionStatus.PAST_DUE -> "Payment due"
    SubscriptionStatus.CANCELLED -> "Cancelled"
}

@Composable
private fun ProfileLink(label: String, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    )
}
