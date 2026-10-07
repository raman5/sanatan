package com.bhakti.app.feature.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.BuildConfig
import com.bhakti.app.data.model.AuthMethod
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.media.ProfilePhotoStore
import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.data.model.SubscriptionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session by container.sessionManager.state.collectAsState(initial = null)

    var photoVersion by remember { mutableIntStateOf(0) }
    val profileBitmap = remember(photoVersion) { ProfilePhotoStore.loadBitmap(context) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                ProfilePhotoStore.save(context, uri)
                photoVersion++
            }
        }
    }

    var isEditingName by remember { mutableStateOf(false) }
    var nameDraft by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Text("Profile", style = MaterialTheme.typography.displaySmall)

        session?.user?.let { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileBitmap != null) {
                                Image(
                                    bitmap = profileBitmap.asImageBitmap(),
                                    contentDescription = "Your photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    user.displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "B",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                        IconButton(
                            onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                Icons.Filled.PhotoCamera,
                                contentDescription = "Change photo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(Modifier.weight(1f)) {
                        if (isEditingName) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = nameDraft,
                                    onValueChange = { nameDraft = it },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    val newName = nameDraft.trim().ifBlank { user.displayName }
                                    scope.launch { container.sessionManager.updateDisplayName(newName) }
                                    isEditingName = false
                                }) {
                                    Icon(Icons.Filled.Check, contentDescription = "Save name")
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.displayName, style = MaterialTheme.typography.titleMedium)
                                IconButton(
                                    onClick = {
                                        nameDraft = user.displayName
                                        isEditingName = true
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Edit name",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        (user.email ?: user.phone)?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
        }

        session?.subscription?.takeIf { BuildConfig.SUBSCRIPTIONS_ENABLED }?.let { sub ->
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

        if (session?.user?.authMethod != AuthMethod.LOCAL) {
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
