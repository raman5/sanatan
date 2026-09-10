package com.bhakti.app.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(navController: NavHostController) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()

    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun goToPaywall() {
        navController.navigate(Routes.PAYWALL) { popUpTo(Routes.AUTH) { inclusive = true } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to Bhakti", style = MaterialTheme.typography.displaySmall)
        Text(
            "Sign in to begin your daily devotional journey",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
        )

        Button(
            onClick = {
                errorMessage = null
                isLoading = true
                scope.launch {
                    val result = container.authRepository.signInWithGoogle()
                    isLoading = false
                    result.onSuccess { user ->
                        container.sessionManager.signIn(user)
                        goToPaywall()
                    }.onFailure { errorMessage = it.message }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Continue with Google")
        }

        Text(
            "or",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 10) phone = it.filter(Char::isDigit) },
            label = { Text("Mobile number") },
            prefix = { Text("+91 ") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick = { navController.navigate(Routes.otp(phone)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            enabled = !isLoading && phone.length == 10
        ) {
            Text("Send OTP")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 20.dp))
        }
        errorMessage?.let {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
