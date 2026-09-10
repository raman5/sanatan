package com.bhakti.app.data.repository

import com.bhakti.app.data.model.BillingCycle
import com.bhakti.app.data.model.Subscription
import com.bhakti.app.data.model.SubscriptionPlan
import com.bhakti.app.data.model.SubscriptionStatus
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * UPI AutoPay is the only supported payment method for launch, per the PRD:
 * a Re.1 trial charge now, then the plan amount auto-debited on renewal.
 * Swap the body of [setupAutoPayMandate] for a real gateway (e.g. Razorpay
 * UPI AutoPay) call once merchant credentials exist - the interface and the
 * payment screen stay the same.
 */
interface PaymentRepository {
    suspend fun setupAutoPayMandate(upiId: String, plan: SubscriptionPlan): Result<Subscription>
}

class FakeUpiPaymentRepository : PaymentRepository {

    private val upiIdPattern = Regex("^[\\w.\\-]{2,256}@[a-zA-Z]{2,64}$")

    override suspend fun setupAutoPayMandate(upiId: String, plan: SubscriptionPlan): Result<Subscription> {
        if (!upiIdPattern.matches(upiId)) {
            return Result.failure(IllegalArgumentException("Enter a valid UPI ID, e.g. name@bank"))
        }
        delay(1200)
        val renewalDate = when (plan.cycle) {
            BillingCycle.MONTHLY -> LocalDate.now().plusMonths(1)
            BillingCycle.ANNUAL -> LocalDate.now().plusYears(1)
        }
        return Result.success(
            Subscription(
                planId = plan.id,
                status = SubscriptionStatus.TRIAL,
                renewalDateIso = renewalDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                mandateId = "mandate-${System.currentTimeMillis()}",
                trialAmountRupees = 1
            )
        )
    }
}
