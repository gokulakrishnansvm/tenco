package com.tenco.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RemoteDealer(val id: String, val name: String, val location: String)

@Serializable
data class RemotePurchase(
    val id: String, val dealerId: String, val quantity: Int,
    val unitCostPaise: Long, val createdAt: Long,
)

@Serializable
data class RemoteVendor(
    val id: String, val name: String, val phone: String,
    val upiVpa: String? = null, val languageTag: String = "en",
)

@Serializable
data class RemotePrice(
    val id: String, val vendorId: String, val unitPricePaise: Long, val effectiveFrom: Long,
)

@Serializable
data class RemoteDelivery(
    val id: String, val vendorId: String, val quantity: Int, val unitPricePaise: Long,
    val status: String, val createdAt: Long, val confirmedAt: Long? = null,
)

@Serializable
data class RemoteComplaint(
    val id: String, val deliveryId: String, val vendorId: String, val reason: String,
    val photoUrl: String? = null, val adjustmentPaise: Long, val status: String, val createdAt: Long,
)

@Serializable
data class RemotePayment(
    val id: String, val vendorId: String, val amountPaise: Long, val method: String,
    val status: String, val upiRef: String? = null, val note: String? = null, val createdAt: Long,
)

@Serializable
data class RemoteSyncChanges(
    val cursor: Long,
    val dealers: List<RemoteDealer> = emptyList(),
    val purchases: List<RemotePurchase> = emptyList(),
    val vendors: List<RemoteVendor> = emptyList(),
    val prices: List<RemotePrice> = emptyList(),
    val deliveries: List<RemoteDelivery> = emptyList(),
    val complaints: List<RemoteComplaint> = emptyList(),
    val payments: List<RemotePayment> = emptyList(),
)
