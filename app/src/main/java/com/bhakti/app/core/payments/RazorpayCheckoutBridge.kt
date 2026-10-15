package com.bhakti.app.core.payments

import android.app.Activity
import com.bhakti.app.data.repository.CheckoutResult
import com.bhakti.app.data.repository.CheckoutTicket
import com.razorpay.Checkout
import com.razorpay.PaymentData
import kotlinx.coroutines.CompletableDeferred
import org.json.JSONObject

/** The user closed Razorpay Checkout without paying - not an error to show. */
class PaymentCancelledException : Exception("Payment cancelled")

/**
 * Razorpay Checkout reports back to the hosting Activity (MainActivity
 * implements PaymentResultWithDataListener and forwards here), so this
 * turns that callback into a suspend call for the payment screen.
 *
 * If the app is killed mid-payment the result is lost here, but the
 * webhook still records the subscription and the paywall picks it up
 * from Firestore.
 */
object RazorpayCheckoutBridge {

    private var pending: CompletableDeferred<Result<CheckoutResult>>? = null
    private var pendingSubscriptionId: String? = null

    fun preload(activity: Activity) = Checkout.preload(activity.applicationContext)

    suspend fun pay(
        activity: Activity,
        ticket: CheckoutTicket,
        description: String,
        email: String?,
        name: String?
    ): Result<CheckoutResult> {
        val result = CompletableDeferred<Result<CheckoutResult>>()
        pending?.complete(Result.failure(PaymentCancelledException()))
        pending = result
        pendingSubscriptionId = ticket.subscriptionId

        val options = JSONObject().apply {
            put("name", "Bhakti")
            put("description", description)
            put("subscription_id", ticket.subscriptionId)
            put("theme", JSONObject().put("color", "#B44E00"))
            put("prefill", JSONObject().apply {
                email?.let { put("email", it) }
                name?.let { put("name", it) }
            })
        }
        runCatching {
            Checkout().apply { setKeyID(ticket.keyId) }.open(activity, options)
        }.onFailure {
            result.complete(Result.failure(IllegalStateException("Couldn't open the payment screen. Please try again.", it)))
        }
        return result.await()
    }

    fun onPaymentSuccess(paymentId: String?, data: PaymentData?) {
        val subscriptionId = data?.data?.optString("razorpay_subscription_id")?.takeIf { it.isNotBlank() }
            ?: pendingSubscriptionId
        val signature = data?.signature
        val outcome = if (paymentId != null && subscriptionId != null && signature != null) {
            Result.success(CheckoutResult(paymentId, subscriptionId, signature))
        } else {
            Result.failure(IllegalStateException("Payment went through but couldn't be confirmed here. It will update shortly."))
        }
        finish(outcome)
    }

    fun onPaymentError(code: Int, response: String?) {
        val outcome: Result<CheckoutResult> = if (code == Checkout.PAYMENT_CANCELED) {
            Result.failure(PaymentCancelledException())
        } else {
            val description = runCatching {
                JSONObject(response.orEmpty()).optJSONObject("error")?.optString("description")
            }.getOrNull()?.takeIf { it.isNotBlank() }
            Result.failure(IllegalStateException(description ?: "Payment failed. Please try again."))
        }
        finish(outcome)
    }

    private fun finish(outcome: Result<CheckoutResult>) {
        pending?.complete(outcome)
        pending = null
        pendingSubscriptionId = null
    }
}
