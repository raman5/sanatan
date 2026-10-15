package com.bhakti.app.core.payments

import com.bhakti.app.BuildConfig
import com.bhakti.app.core.session.SessionManager
import com.bhakti.app.data.model.Subscription
import com.bhakti.app.data.model.SubscriptionStatus
import com.bhakti.app.data.repository.AuthRepository
import com.bhakti.app.data.repository.Entitlement
import com.bhakti.app.data.repository.PaymentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

/**
 * Copies the signed-in user's server entitlement into the local session,
 * so the splash/paywall decide instantly (and offline) from the last known
 * state while Firestore keeps it current in the background.
 */
class EntitlementSync(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val paymentRepository: PaymentRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun start(scope: CoroutineScope) {
        scope.launch {
            sessionManager.state
                .map { it.user?.id }
                .distinctUntilChanged()
                .flatMapLatest { uid ->
                    // Only listen for a real signed-in account (not an old name-only profile).
                    if (uid != null && uid == authRepository.currentUserId()) paymentRepository.entitlement(uid) else emptyFlow()
                }
                .collect { entitlement -> save(entitlement) }
        }
    }

    private suspend fun save(entitlement: Entitlement) {
        val current = sessionManager.state.first().subscription
        // The debug-only "skip payment" button grants local access - don't let the
        // (empty) server entitlement undo it while testing.
        if (BuildConfig.DEBUG && current.mandateId == DEBUG_SKIP && !entitlement.isActive()) return
        sessionManager.saveSubscription(entitlement.toSubscription())
    }

    companion object {
        const val DEBUG_SKIP = "debug-skip"

        fun Entitlement.toSubscription(): Subscription {
            val status = if (!isActive()) SubscriptionStatus.NONE else when (status) {
                "authenticated" -> SubscriptionStatus.TRIAL
                "active" -> SubscriptionStatus.ACTIVE
                "pending", "halted" -> SubscriptionStatus.PAST_DUE
                "cancelled", "completed", "paused" -> SubscriptionStatus.CANCELLED
                else -> SubscriptionStatus.NONE
            }
            return Subscription(
                planId = planKey,
                status = status,
                renewalDateIso = currentPeriodEndMs?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toString()
                },
                mandateId = "razorpay"
            )
        }
    }
}
