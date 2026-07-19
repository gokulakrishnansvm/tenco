package com.tenco.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class OtpRequestBody(val phone: String)

@Serializable
data class OtpRequestResponse(val sent: Boolean = false, val devOtp: String? = null)

@Serializable
data class OtpVerifyBody(
    val phone: String,
    val code: String,
    val name: String? = null,
    val role: String? = null,
)

@Serializable
data class RoleBody(val role: String)

@Serializable
data class AuthResponse(
    val userId: String,
    val role: String,
    val accessToken: String,
    val refreshToken: String,
)
