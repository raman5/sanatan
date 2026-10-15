package com.bhakti.app.core.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** The Activity behind a Compose [Context] - Google sign-in and Razorpay Checkout need one to host their UI. */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
