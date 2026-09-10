package com.bhakti.app.feature.profile

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.session.NotificationPrefKey
import com.bhakti.app.notifications.syncScheduledReminders
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun NotificationSettingsScreen(navController: NavHostController) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val session by container.sessionManager.state.collectAsState(initial = null)

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun toggle(key: NotificationPrefKey, enabled: Boolean) {
        scope.launch {
            container.sessionManager.setNotificationPref(key, enabled)
            syncScheduledReminders(container.sessionManager.state.first(), container.contentRepository, container.notificationScheduler)
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text("Notifications", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 8.dp))

        session?.let { s ->
            NotificationRow("Morning devotional reminder", "7:00 AM", s.notifMorning) { enabled ->
                toggle(NotificationPrefKey.MORNING, enabled)
            }
            NotificationRow("Evening aarti reminder", "6:30 PM", s.notifEvening) { enabled ->
                toggle(NotificationPrefKey.EVENING, enabled)
            }
            NotificationRow("Daily mantra notification", "9:00 AM", s.notifMantra) { enabled ->
                toggle(NotificationPrefKey.MANTRA, enabled)
            }
            NotificationRow("Daily WhatsApp status notification", "8:00 PM", s.notifStatus) { enabled ->
                toggle(NotificationPrefKey.STATUS, enabled)
            }
            NotificationRow("Festival-specific notifications", "On festival day", s.notifFestival) { enabled ->
                toggle(NotificationPrefKey.FESTIVAL, enabled)
            }
        }
    }
}

@Composable
private fun NotificationRow(title: String, time: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(time, style = MaterialTheme.typography.labelSmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
