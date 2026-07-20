package com.tenco.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "dealers")
data class DealerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val location: String,
    val archived: Boolean = false,
)

@Entity(tableName = "purchases", indices = [Index("dealerId")])
data class PurchaseEntity(
    @PrimaryKey val id: String,
    val dealerId: String,
    val quantity: Int,
    val unitCostPaise: Long,
    val createdAt: Long,
    val batchId: String = "",
    val color: String = "GREEN",
    val grade: String = "MEDIUM",
)

@Entity(tableName = "vendors")
data class VendorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val upiVpa: String?,
    val languageTag: String,
    val city: String = "",
    val archived: Boolean = false,
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
    val color: String = "GREEN",
    val grade: String = "MEDIUM",
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
    val shortQuantity: Int = 0,
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

/** A vendor's coconut order to the supplier, progressing through a fulfilment lifecycle. */
@Entity(tableName = "orders", indices = [Index("vendorId")])
data class OrderEntity(
    @PrimaryKey val id: String,
    val vendorId: String,
    val quantity: Int,
    val unitPricePaise: Long?,   // null until the supplier sets a price
    val status: String,          // PLACED, CONFIRMED, IN_PROGRESS, IN_TRANSIT, DELIVERED, CANCELLED
    val paid: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val color: String = "GREEN",
    val grade: String = "MEDIUM",
)

/** Records a locally-changed entity awaiting push to the backend (client->server sync). */
@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true) val seq: Long = 0,
    val entityType: String,
    val entityId: String,
    val createdAt: Long,
)
