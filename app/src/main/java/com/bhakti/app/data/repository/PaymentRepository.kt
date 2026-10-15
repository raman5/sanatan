package com.bhakti.app.data.repository

import android.content.Context
import com.bhakti.app.core.firebase.FirebaseConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/**
 * The user's subscription as recorded by the payment functions in Firestore
 * `entitlements/{uid}` (see functions/index.js). [status] is Razorpay's:
 * created, authenticated, active, pending, halted, cancelled, completed, expired, paused.
 */
data class Entitlement(
    val status: String,
    val hasAccess: Boolean,
    val planKey: String?,
    val currentPeriodEndMs: Long?
) {
    /** Access right now - a cancelled plan stays usable until its paid period ends. */
    fun isActive(nowMs: Long = System.currentTimeMillis()): Boolean =
        hasAccess && (currentPeriodEndMs == null || currentPeriodEndMs > nowMs ||
            status in setOf("authenticated", "active", "pending"))

    companion object {
        val NONE = Entitlement(status = "none", hasAccess = false, planKey = null, currentPeriodEndMs = null)
    }
}

/** What Razorpay Checkout needs to open for a new subscription. */
data class CheckoutTicket(val subscriptionId: String, val keyId: String)

/** Razorpay's Checkout success data, passed back to the server to verify. */
data class CheckoutResult(val paymentId: String, val subscriptionId: String, val signature: String)

/**
 * Razorpay subscriptions. The app never holds the key secret: creating,
 * verifying and cancelling all go through the Cloud Functions, and the
 * result arrives via [entitlement].
 */
interface PaymentRepository {
    fun entitlement(uid: String): Flow<Entitlement>
    suspend fun startSubscription(planKey: String): Result<CheckoutTicket>
    suspend fun verifySubscription(result: CheckoutResult): Result<Entitlement>
    suspend fun cancelSubscription(): Result<Entitlement>
}

class RazorpayPaymentRepository(private val context: Context) : PaymentRepository {

    private val functions get() = FirebaseConfig.functions(context)

    override fun entitlement(uid: String): Flow<Entitlement> = callbackFlow {
        val registration = FirebaseConfig.firestore(context)
            .collection("entitlements").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener // keep the last known value
                if (snapshot == null || !snapshot.exists()) {
                    trySend(Entitlement.NONE)
                    return@addSnapshotListener
                }
                trySend(
                    Entitlement(
                        status = snapshot.getString("status") ?: "none",
                        hasAccess = snapshot.getBoolean("hasAccess") ?: false,
                        planKey = snapshot.getString("planKey"),
                        currentPeriodEndMs = snapshot.getTimestamp("currentPeriodEnd")?.toDate()?.time
                    )
                )
            }
        awaitClose { registration.remove() }
    }

    override suspend fun startSubscription(planKey: String): Result<CheckoutTicket> = call("createSubscription", mapOf("planKey" to planKey)) { data ->
        CheckoutTicket(
            subscriptionId = data["subscriptionId"] as String,
            keyId = data["keyId"] as String
        )
    }

    override suspend fun verifySubscription(result: CheckoutResult): Result<Entitlement> = call(
        "verifySubscription",
        mapOf("paymentId" to result.paymentId, "subscriptionId" to result.subscriptionId, "signature" to result.signature)
    ) { it.toEntitlement() }

    override suspend fun cancelSubscription(): Result<Entitlement> = call("cancelSubscription", emptyMap<String, Any>()) { it.toEntitlement() }

    private suspend fun <T> call(name: String, payload: Map<String, Any>, parse: (Map<*, *>) -> T): Result<T> = runCatching {
        val data = functions.getHttpsCallable(name).call(payload).await().getData() as? Map<*, *>
            ?: error("Empty response from server")
        parse(data)
    }.recoverCatching { e ->
        // Callable errors carry the server's user-facing message (HttpsError in functions/index.js).
        throw IllegalStateException(e.message ?: "Something went wrong. Please try again.", e)
    }

    private fun Map<*, *>.toEntitlement() = Entitlement(
        status = this["status"] as? String ?: "none",
        hasAccess = this["hasAccess"] as? Boolean ?: false,
        planKey = null,
        currentPeriodEndMs = (this["currentPeriodEnd"] as? Number)?.toLong()
    )
}

/** Stand-in for builds without firebase.properties: subscribing always succeeds locally. */
class FakePaymentRepository : PaymentRepository {
    override fun entitlement(uid: String): Flow<Entitlement> = flowOf(Entitlement.NONE)

    override suspend fun startSubscription(planKey: String): Result<CheckoutTicket> =
        Result.failure(IllegalStateException("Payments need the Firebase backend (firebase.properties)."))

    override suspend fun verifySubscription(result: CheckoutResult): Result<Entitlement> {
        delay(300)
        return Result.failure(IllegalStateException("Payments need the Firebase backend (firebase.properties)."))
    }

    override suspend fun cancelSubscription(): Result<Entitlement> =
        Result.failure(IllegalStateException("Payments need the Firebase backend (firebase.properties)."))
}
