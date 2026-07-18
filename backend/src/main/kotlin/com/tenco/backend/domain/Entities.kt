package com.tenco.backend.domain

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()
fun now(): Long = System.currentTimeMillis()

@Entity
@Table(name = "users", indexes = [Index(columnList = "phone", unique = true)])
class AppUser(
    @Id var id: String = newId(),
    var phone: String = "",
    var name: String = "",
    var role: String = "VENDOR", // SUPPLIER | VENDOR
    var createdAt: Long = now(),
)

@Entity
@Table(name = "dealers")
class Dealer(
    @Id var id: String = newId(),
    var name: String = "",
    var location: String = "",
    var updatedAt: Long = now(),
)

@Entity
@Table(name = "purchases", indexes = [Index(columnList = "dealerId")])
class Purchase(
    @Id var id: String = newId(),
    var dealerId: String = "",
    var quantity: Int = 0,
    var unitCostPaise: Long = 0,
    var createdAt: Long = now(),
    var updatedAt: Long = now(),
)

@Entity
@Table(name = "vendors", indexes = [Index(columnList = "phone")])
class Vendor(
    @Id var id: String = newId(),
    var name: String = "",
    var phone: String = "",
    var upiVpa: String? = null,
    var languageTag: String = "en",
    var updatedAt: Long = now(),
)

@Entity
@Table(name = "prices", indexes = [Index(columnList = "vendorId")])
class Price(
    @Id var id: String = newId(),
    var vendorId: String = "",
    var unitPricePaise: Long = 0,
    var effectiveFrom: Long = now(),
    var updatedAt: Long = now(),
)

@Entity
@Table(name = "deliveries", indexes = [Index(columnList = "vendorId")])
class Delivery(
    @Id var id: String = newId(),
    var vendorId: String = "",
    var quantity: Int = 0,
    var unitPricePaise: Long = 0,
    var status: String = "DELIVERED", // DELIVERED | CONFIRMED
    var createdAt: Long = now(),
    var confirmedAt: Long? = null,
    var updatedAt: Long = now(),
)

@Entity
@Table(name = "complaints", indexes = [Index(columnList = "vendorId")])
class Complaint(
    @Id var id: String = newId(),
    var deliveryId: String = "",
    var vendorId: String = "",
    var reason: String = "",
    var photoUrl: String? = null,
    var adjustmentPaise: Long = 0,
    var status: String = "OPEN", // OPEN | RESOLVED
    var createdAt: Long = now(),
    var updatedAt: Long = now(),
)

@Entity
@Table(name = "payments", indexes = [Index(columnList = "vendorId")])
class Payment(
    @Id var id: String = newId(),
    var vendorId: String = "",
    var amountPaise: Long = 0,
    var method: String = "UPI", // UPI | CASH
    var status: String = "PENDING", // PENDING | PENDING_VERIFICATION | COMPLETED | FAILED
    var gatewayOrderId: String? = null,
    var upiRef: String? = null,
    var note: String? = null,
    var createdAt: Long = now(),
    var updatedAt: Long = now(),
)

@Entity
@Table(name = "device_tokens", indexes = [Index(columnList = "userId")])
class DeviceToken(
    @Id var id: String = newId(),
    var userId: String = "",
    var token: String = "",
    var updatedAt: Long = now(),
)
