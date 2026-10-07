package com.bhakti.app.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.bhakti.app.data.repository.OtpRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OtpScreen(navController: NavHostController, phone: String) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()

    var otpRequest by remember { mutableStateOf<OtpRequest?>(null) }
    var code by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }

    suspend fun sendOtp() {
        errorMessage = null
        val result = container.authRepository.requestOtp(phone)
        result.onSuccess { otpRequest = it }.onFailure { errorMessage = it.message }
    }

    LaunchedEffect(Unit) { sendOtp() }

    LaunchedEffect(otpRequest) {
        while (true) {
            delay(1000)
            nowMs = System.currentTimeMillis()
        }
    }

    val secondsToExpiry = ((otpRequest?.expiresAtEpochMs ?: 0L) - nowMs).coerceAtLeast(0) / 1000
    val secondsToResend = ((otpRequest?.resendAvailableAtEpochMs ?: 0L) - nowMs).coerceAtLeast(0) / 1000

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Verify your number", style = MaterialTheme.typography.displaySmall)
        Text(
            "Enter the 6-digit code sent to +91 $phone",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        otpRequest?.let {
            Text(
                "Demo OTP: ${it.devHint} (no SMS gateway wired up yet)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 6) code = it.filter(Char::isDigit) },
            label = { Text("6-digit code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            if (secondsToExpiry > 0) "Code expires in ${secondsToExpiry}s" else "Code expired",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 6.dp)
        )

        Button(
            onClick = {
                errorMessage = null
                isLoading = true
                scope.launch {
                    val result = container.authRepository.verifyOtp(phone, code)
                    isLoading = false
                    result.onSuccess { user ->
                        container.sessionManager.signIn(user)
                        navController.navigate(Routes.AFTER_ONBOARDING) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    }.onFailure { errorMessage = it.message }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            enabled = !isLoading && code.length == 6 && secondsToExpiry > 0
        ) {
            Text("Verify & Continue")
        }

        TextButton(
            onClick = { scope.launch { sendOtp() } },
            enabled = secondsToResend <= 0,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(if (secondsToResend > 0) "Resend OTP in ${secondsToResend}s" else "Resend OTP")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
        }
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
