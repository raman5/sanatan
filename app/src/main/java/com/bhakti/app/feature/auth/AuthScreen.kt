package com.bhakti.app.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.BuildConfig
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.core.ui.findActivity
import com.bhakti.app.data.model.AuthMethod
import com.bhakti.app.data.model.User
import com.bhakti.app.data.repository.SignInCancelledException
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * First-run sign-in. With subscriptions on, the user signs in with Google so
 * their subscription follows their account across reinstalls and phones.
 * With subscriptions off, there's no account at all - only a name.
 */
@Composable
fun AuthScreen(navController: NavHostController) {
    if (BuildConfig.SUBSCRIPTIONS_ENABLED) GoogleSignIn(navController) else NameOnlyWelcome(navController)
}

@Composable
private fun GoogleSignIn(navController: NavHostController) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var signingIn by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun signIn() {
        val activity = context.findActivity() ?: return
        if (signingIn) return
        signingIn = true
        error = null
        scope.launch {
            container.authRepository.signInWithGoogle(activity)
                .onSuccess { user ->
                    container.sessionManager.signIn(user)
                    navController.navigate(Routes.AFTER_ONBOARDING) { popUpTo(Routes.AUTH) { inclusive = true } }
                }
                .onFailure { if (it !is SignInCancelledException) error = it.message }
            signingIn = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("🙏 Welcome to Bhakti", style = MaterialTheme.typography.displaySmall)
        Text(
            "Sign in to begin your daily devotional journey.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
        )

        Button(
            onClick = { signIn() },
            enabled = !signingIn,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (signingIn) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text("Continue with Google")
            }
        }

        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Text(
            "We use your Google account only to keep your subscription linked to you. " +
                "Your routine and japa progress stay on this phone.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun NameOnlyWelcome(navController: NavHostController) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    fun begin() {
        if (saving) return
        saving = true
        scope.launch {
            container.sessionManager.signIn(
                User(
                    id = "local-${UUID.randomUUID()}",
                    displayName = name.trim().ifBlank { "Bhakt" },
                    email = null,
                    phone = null,
                    authMethod = AuthMethod.LOCAL,
                    isNewAccount = true
                )
            )
            navController.navigate(Routes.AFTER_ONBOARDING) { popUpTo(Routes.AUTH) { inclusive = true } }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("🙏 Welcome to Bhakti", style = MaterialTheme.typography.displaySmall)
        Text(
            "What should we call you?",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 30) name = it },
            label = { Text("Your name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { begin() }),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { begin() },
            enabled = !saving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Begin my journey")
        }

        Text(
            "No account needed - your routine and japa progress stay on this phone.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
