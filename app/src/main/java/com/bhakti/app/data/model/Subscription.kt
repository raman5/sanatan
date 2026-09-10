package com.bhakti.app.data.model

enum class BillingCycle { MONTHLY, ANNUAL }

data class SubscriptionPlan(
    val id: String,
    val cycle: BillingCycle,
    val label: String,
    val priceRupees: Int,
    val perMonthEquivalent: Int,
    val badge: String? = null
) {
    companion object {
        val ALL = listOf(
            SubscriptionPlan(
                id = "monthly",
                cycle = BillingCycle.MONTHLY,
                label = "Monthly",
                priceRupees = 99,
                perMonthEquivalent = 99
            ),
            SubscriptionPlan(
                id = "annual",
                cycle = BillingCycle.ANNUAL,
                label = "Annual",
                priceRupees = 599,
                perMonthEquivalent = 599 / 12,
                badge = "Best value"
            )
        )
    }
}

enum class SubscriptionStatus { NONE, TRIAL, ACTIVE, PAST_DUE, CANCELLED }

data class Subscription(
    val planId: String?,
    val status: SubscriptionStatus,
    val renewalDateIso: String?,
    val mandateId: String?,
    val trialAmountRupees: Int = 1
)
