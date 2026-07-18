package com.tenco.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class IntentBody(val vendorId: String, val amountPaise: Long)

@Serializable
data class IntentResponse(
    val intentId: String,
    val orderId: String,
    val amountPaise: Long,
    val upiDeepLink: String,
)

@Serializable
data class DeviceRegisterBody(val userId: String, val fcmToken: String)
