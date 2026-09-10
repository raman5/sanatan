package com.bhakti.app.data.repository

import com.bhakti.app.data.model.AuthMethod
import com.bhakti.app.data.model.User
import kotlinx.coroutines.delay

data class OtpRequest(
    val phone: String,
    val expiresAtEpochMs: Long,
    val resendAvailableAtEpochMs: Long,
    val attemptsRemaining: Int,
    /** Only present because no SMS gateway is wired up yet; remove once one is. */
    val devHint: String
)

/**
 * Wraps both login paths from the PRD. The fake implementation below owns
 * real OTP business rules (expiry, resend cooldown, attempt limits) so only
 * the delivery mechanism (SMS gateway, Google Sign-In SDK) needs swapping
 * out later - the interface and screens don't change.
 */
interface AuthRepository {
    suspend fun signInWithGoogle(): Result<User>
    suspend fun requestOtp(phone: String): Result<OtpRequest>
    suspend fun resendOtp(phone: String): Result<OtpRequest>
    suspend fun verifyOtp(phone: String, code: String): Result<User>
    suspend fun signOut()
}

class FakeAuthRepository : AuthRepository {

    private data class OtpSession(
        val code: String,
        val expiresAt: Long,
        val resendAvailableAt: Long,
        var attemptsUsed: Int
    )

    private val otpExpiryMs = 2 * 60 * 1000L
    private val resendCooldownMs = 30 * 1000L
    private val maxAttempts = 5
    private val sessions = mutableMapOf<String, OtpSession>()

    override suspend fun signInWithGoogle(): Result<User> {
        delay(700)
        return Result.success(
            User(
                id = "google-${System.currentTimeMillis()}",
                displayName = "Bhakt",
                email = "devotee@gmail.com",
                authMethod = AuthMethod.GOOGLE,
                isNewAccount = true
            )
        )
    }

    override suspend fun requestOtp(phone: String): Result<OtpRequest> {
        if (!phone.matches(Regex("^[6-9]\\d{9}$"))) {
            return Result.failure(IllegalArgumentException("Enter a valid 10-digit mobile number"))
        }
        delay(500)
        val now = System.currentTimeMillis()
        val existing = sessions[phone]
        if (existing != null && now < existing.resendAvailableAt) {
            return Result.failure(IllegalStateException("Please wait before requesting another OTP"))
        }
        val code = (100000..999999).random().toString()
        sessions[phone] = OtpSession(
            code = code,
            expiresAt = now + otpExpiryMs,
            resendAvailableAt = now + resendCooldownMs,
            attemptsUsed = 0
        )
        return Result.success(
            OtpRequest(
                phone = phone,
                expiresAtEpochMs = now + otpExpiryMs,
                resendAvailableAtEpochMs = now + resendCooldownMs,
                attemptsRemaining = maxAttempts,
                devHint = code
            )
        )
    }

    override suspend fun resendOtp(phone: String): Result<OtpRequest> = requestOtp(phone)

    override suspend fun verifyOtp(phone: String, code: String): Result<User> {
        delay(400)
        val session = sessions[phone]
            ?: return Result.failure(IllegalStateException("Request an OTP first"))
        val now = System.currentTimeMillis()
        if (now > session.expiresAt) {
            sessions.remove(phone)
            return Result.failure(IllegalStateException("OTP expired - request a new one"))
        }
        if (session.attemptsUsed >= maxAttempts) {
            sessions.remove(phone)
            return Result.failure(IllegalStateException("Too many attempts - request a new OTP"))
        }
        session.attemptsUsed++
        if (code != session.code) {
            val left = maxAttempts - session.attemptsUsed
            return Result.failure(IllegalArgumentException("Incorrect OTP, $left attempt(s) left"))
        }
        sessions.remove(phone)
        return Result.success(
            User(
                id = "otp-$phone",
                displayName = "Bhakt",
                phone = phone,
                authMethod = AuthMethod.MOBILE_OTP,
                isNewAccount = true
            )
        )
    }

    override suspend fun signOut() {
        sessions.clear()
    }
}
