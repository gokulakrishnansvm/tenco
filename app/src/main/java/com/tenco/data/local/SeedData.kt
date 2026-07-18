package com.tenco.data.local

import com.tenco.domain.ComplaintStatus
import com.tenco.domain.DeliveryStatus
import com.tenco.domain.PaymentMethod
import com.tenco.domain.PaymentStatus
import java.util.UUID

/** Populates demo data on first database creation (Phase 1, local-only). */
object SeedData {

    suspend fun seed(db: TencoDatabase) {
        val dealerDao = db.dealerDao()
        if (dealerDao.count() > 0) return

        val purchaseDao = db.purchaseDao()
        val vendorDao = db.vendorDao()
        val priceDao = db.priceDao()
        val deliveryDao = db.deliveryDao()
        val paymentDao = db.paymentDao()
        val complaintDao = db.complaintDao()

        val now = System.currentTimeMillis()
        val day = 24 * 60 * 60 * 1000L

        // Dealers (source markets)
        val pollachi = DealerEntity(id(), "Pollachi Traders", "Pollachi")
        val nellore = DealerEntity(id(), "Nellore Coconuts", "Nellore")
        val theni = DealerEntity(id(), "Theni Farms", "Theni")
        listOf(pollachi, nellore, theni).forEach { dealerDao.upsert(it) }

        // Purchases (stock in) — unit cost in paise
        purchaseDao.upsert(PurchaseEntity(id(), pollachi.id, 500, 2000, now - 5 * day)) // ₹20
        purchaseDao.upsert(PurchaseEntity(id(), nellore.id, 300, 1800, now - 3 * day))  // ₹18
        purchaseDao.upsert(PurchaseEntity(id(), theni.id, 200, 2200, now - 1 * day))    // ₹22

        // Vendors
        val ravi = VendorEntity(id(), "Ravi Stall", "+919876543210", "ravi@upi", "ta")
        val lakshmi = VendorEntity(id(), "Lakshmi Cart", "+919812345678", "lakshmi@upi", "te")
        val kumar = VendorEntity(id(), "Kumar Shop", "+919800011122", "kumar@upi", "hi")
        listOf(ravi, lakshmi, kumar).forEach { vendorDao.upsert(it) }

        // Vendor-specific prices (paise)
        priceDao.upsert(PriceEntity(id(), ravi.id, 2800, now - 4 * day))    // ₹28
        priceDao.upsert(PriceEntity(id(), lakshmi.id, 3000, now - 4 * day)) // ₹30
        priceDao.upsert(PriceEntity(id(), kumar.id, 2900, now - 4 * day))   // ₹29

        // Deliveries (stock out)
        val d1 = DeliveryEntity(id(), ravi.id, 50, 2800, DeliveryStatus.CONFIRMED, now - 3 * day, now - 3 * day)
        val d2 = DeliveryEntity(id(), ravi.id, 40, 2800, DeliveryStatus.DELIVERED, now - 1 * day, null)
        val d3 = DeliveryEntity(id(), lakshmi.id, 60, 3000, DeliveryStatus.CONFIRMED, now - 2 * day, now - 2 * day)
        val d4 = DeliveryEntity(id(), kumar.id, 30, 2900, DeliveryStatus.DELIVERED, now - 6 * 60 * 60 * 1000L, null)
        listOf(d1, d2, d3, d4).forEach { deliveryDao.upsert(it) }

        // Payments
        paymentDao.upsert(
            PaymentEntity(id(), ravi.id, 100000, PaymentMethod.UPI, PaymentStatus.COMPLETED, "UPI123", "Partial", now - 2 * day)
        ) // ₹1000 of ₹1400+1120
        paymentDao.upsert(
            PaymentEntity(id(), lakshmi.id, 180000, PaymentMethod.UPI, PaymentStatus.COMPLETED, "UPI456", null, now - 1 * day)
        ) // ₹1800 == full

        // Complaint (open)
        complaintDao.upsert(
            ComplaintEntity(id(), d2.id, ravi.id, "Spoiled coconuts", null, 0, ComplaintStatus.OPEN, now - 12 * 60 * 60 * 1000L)
        )
    }

    private fun id() = UUID.randomUUID().toString()
}
