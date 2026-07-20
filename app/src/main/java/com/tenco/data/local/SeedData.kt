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
        val b1 = id()
        purchaseDao.upsert(PurchaseEntity(id(), pollachi.id, 1000, 5000, now - 5 * day, batchId = b1, color = "GREEN", grade = "BIG"))
        purchaseDao.upsert(PurchaseEntity(id(), pollachi.id, 1000, 4000, now - 5 * day, batchId = b1, color = "GREEN", grade = "MEDIUM"))
        purchaseDao.upsert(PurchaseEntity(id(), pollachi.id, 1000, 3000, now - 5 * day, batchId = b1, color = "GREEN", grade = "SMALL"))
        val b2 = id()
        purchaseDao.upsert(PurchaseEntity(id(), nellore.id, 400, 4500, now - 3 * day, batchId = b2, color = "RED", grade = "BIG"))
        purchaseDao.upsert(PurchaseEntity(id(), nellore.id, 300, 3500, now - 3 * day, batchId = b2, color = "RED", grade = "MEDIUM"))
        purchaseDao.upsert(PurchaseEntity(id(), theni.id, 200, 2200, now - 1 * day, batchId = id(), color = "GREEN", grade = "MEDIUM"))

        // Vendors
        val ravi = VendorEntity(id(), "Ravi Stall", "+919876543210", "ravi@upi", "ta", city = "Chennai")
        val lakshmi = VendorEntity(id(), "Lakshmi Cart", "+919812345678", "lakshmi@upi", "te", city = "Hyderabad")
        val kumar = VendorEntity(id(), "Kumar Shop", "+919800011122", "kumar@upi", "hi", city = "Chennai")
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

        // Complaints
        complaintDao.upsert(
            ComplaintEntity(id(), d2.id, ravi.id, "spoiled", null, 0, ComplaintStatus.OPEN, now - 12 * 60 * 60 * 1000L, shortQuantity = 8)
        )
        complaintDao.upsert(
            ComplaintEntity(id(), d3.id, lakshmi.id, "short", null, 15000, ComplaintStatus.RESOLVED, now - 6 * 60 * 60 * 1000L, shortQuantity = 12)
        )
    }

    private fun id() = UUID.randomUUID().toString()
}
