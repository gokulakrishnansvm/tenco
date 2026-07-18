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
    ],
    version = 1,
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

    companion object {
        const val NAME = "tenco.db"
    }
}
