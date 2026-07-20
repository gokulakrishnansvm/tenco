package com.tenco.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DealerEntity::class,
        PurchaseEntity::class,
        VendorEntity::class,
        PriceEntity::class,
        DeliveryEntity::class,
        ComplaintEntity::class,
        PaymentEntity::class,
        OrderEntity::class,
        OutboxEntity::class,
    ],
    version = 9,
    exportSchema = false,
)
abstract class TencoDatabase : RoomDatabase() {
    abstract fun dealerDao(): DealerDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun vendorDao(): VendorDao
    abstract fun priceDao(): PriceDao
    abstract fun deliveryDao(): DeliveryDao
    abstract fun complaintDao(): ComplaintDao
    abstract fun paymentDao(): PaymentDao
    abstract fun orderDao(): OrderDao
    abstract fun outboxDao(): OutboxDao

    companion object {
        const val NAME = "tenco.db"
    }
}
