package com.bhakti.app.data.model

enum class AuthMethod { GOOGLE, MOBILE_OTP }

data class User(
    val id: String,
    val displayName: String,
    val email: String? = null,
    val phone: String? = null,
    val authMethod: AuthMethod,
    val isNewAccount: Boolean = false
)
