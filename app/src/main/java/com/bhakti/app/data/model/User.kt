package com.bhakti.app.data.model

/** LOCAL = name-only profile kept on the phone, no account or verification behind it. */
enum class AuthMethod { GOOGLE, MOBILE_OTP, LOCAL }

data class User(
    val id: String,
    val displayName: String,
    val email: String? = null,
    val phone: String? = null,
    val authMethod: AuthMethod,
    val isNewAccount: Boolean = false
)
