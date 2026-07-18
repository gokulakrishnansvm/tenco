package com.tenco.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "dealers")
data class DealerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val location: String,
)

@Entity(tableName = "purchases", indices = [Index("dealerId")])
data class PurchaseEntity(
    @PrimaryKey val id: String,
    val dealerId: String,
    val quantity: Int,
    val unitCostPaise: Long,
    val createdAt: Long,
)

@Entity(tableName = "vendors")
data class VendorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val upiVpa: String?,
    val languageTag: String,
)

@Entity(tableName = "prices", indices = [Index("vendorId")])
data class PriceEntity(
    @PrimaryKey val id: String,
    val vendorId: String,
    val unitPricePaise: Long,
    val effectiveFrom: Long,
)

@Entity(tableName = "deliveries", indices = [Index("vendorId")])
data class DeliveryEntity(
    @PrimaryKey val id: String,
    val vendorId: String,
    val quantity: Int,
    val unitPricePaise: Long,
    val status: String,
    val createdAt: Long,
    val confirmedAt: Long?,
)

@Entity(tableName = "complaints", indices = [Index("vendorId"), Index("deliveryId")])
data class ComplaintEntity(
    @PrimaryKey val id: String,
    val deliveryId: String,
    val vendorId: String,
    val reason: String,
    val photoUri: String?,
    val adjustmentPaise: Long,
    val status: String,
    val createdAt: Long,
)

@Entity(tableName = "payments", indices = [Index("vendorId")])
data class PaymentEntity(
    @PrimaryKey val id: String,
    val vendorId: String,
    val amountPaise: Long,
    val method: String,
    val status: String,
    val upiRef: String?,
    val note: String?,
    val createdAt: Long,
)
