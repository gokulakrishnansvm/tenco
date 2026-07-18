package com.tenco.data.repository

import com.tenco.data.local.ComplaintEntity
import com.tenco.data.local.DealerEntity
import com.tenco.data.local.DeliveryEntity
import com.tenco.data.local.PaymentEntity
import com.tenco.data.local.PriceEntity
import com.tenco.data.local.PurchaseEntity
import com.tenco.data.local.SeedData
import com.tenco.data.local.TencoDatabase
import com.tenco.data.local.VendorEntity
import com.tenco.domain.ComplaintStatus
import com.tenco.domain.DeliveryStatus
import com.tenco.domain.PaymentStatus
import com.tenco.domain.PnlReport
import com.tenco.domain.SupplierDashboard
import com.tenco.domain.VendorDashboard
import com.tenco.domain.VendorDistribution
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TencoRepository @Inject constructor(
    private val db: TencoDatabase,
) {
    private val seedMutex = Mutex()
    private val dealerDao = db.dealerDao()
    private val purchaseDao = db.purchaseDao()
    private val vendorDao = db.vendorDao()
    private val priceDao = db.priceDao()
    private val deliveryDao = db.deliveryDao()
    private val complaintDao = db.complaintDao()
    private val paymentDao = db.paymentDao()

    // --- Raw observers ---
    fun observeDealers(): Flow<List<DealerEntity>> = dealerDao.observeAll()
    fun observePurchases(): Flow<List<PurchaseEntity>> = purchaseDao.observeAll()
    fun observeVendors(): Flow<List<VendorEntity>> = vendorDao.observeAll()
    fun observePrices(): Flow<List<PriceEntity>> = priceDao.observeAll()
    fun observeDeliveries(): Flow<List<DeliveryEntity>> = deliveryDao.observeAll()
    fun observePayments(): Flow<List<PaymentEntity>> = paymentDao.observeAll()
    fun observeComplaints(): Flow<List<ComplaintEntity>> = complaintDao.observeAll()
    fun observeVendor(id: String): Flow<VendorEntity?> = vendorDao.observeById(id)
    fun observePaymentsForVendor(vendorId: String): Flow<List<PaymentEntity>> =
        paymentDao.observeForVendor(vendorId)
    fun observeComplaintsForVendor(vendorId: String): Flow<List<ComplaintEntity>> =
        complaintDao.observeForVendor(vendorId)
    fun observeDeliveriesForVendor(vendorId: String): Flow<List<DeliveryEntity>> =
        deliveryDao.observeForVendor(vendorId)

    suspend fun firstVendor(): VendorEntity? = vendorDao.firstVendor()

    /**
     * Resolves the vendor for a logged-in phone number by matching the last 10 digits against
     * the supplier's vendor list. Returns null if the number isn't a registered vendor.
     */
    suspend fun findVendorByPhone(phone: String?): VendorEntity? {
        val suffix = phone?.filter(Char::isDigit)?.takeLast(10) ?: return null
        if (suffix.length < 10) return null
        return vendorDao.findByPhoneSuffix(suffix)
    }

    /** Idempotent, serialized seeding of demo data (safe to call from multiple places). */
    suspend fun ensureSeeded() = seedMutex.withLock { SeedData.seed(db) }

    // --- Supplier dashboard aggregate ---
    fun observeSupplierDashboard(): Flow<SupplierDashboard> = combine(
        purchaseDao.observeAll(),
        deliveryDao.observeAll(),
        paymentDao.observeAll(),
        complaintDao.observeAll(),
        vendorDao.observeAll(),
    ) { purchases, deliveries, payments, complaints, vendors ->
        val stock = purchases.sumOf { it.quantity } - deliveries.sumOf { it.quantity }
        val earnings = payments.filter { it.status == PaymentStatus.COMPLETED }.sumOf { it.amountPaise }
        val resolvedAdjust = complaints
            .filter { it.status == ComplaintStatus.RESOLVED }
            .sumOf { it.adjustmentPaise }

        val distribution = vendors.map { v ->
            val delivered = deliveries.filter { it.vendorId == v.id }
            val qty = delivered.sumOf { it.quantity }
            val value = delivered.sumOf { it.quantity * it.unitPricePaise }
            val paid = payments.filter { it.vendorId == v.id && it.status == PaymentStatus.COMPLETED }
                .sumOf { it.amountPaise }
            val adjust = complaints.filter { it.vendorId == v.id && it.status == ComplaintStatus.RESOLVED }
                .sumOf { it.adjustmentPaise }
            val dues = (value - adjust - paid).coerceAtLeast(0)
            VendorDistribution(v.id, v.name, qty, dues)
        }
        SupplierDashboard(
            stockOnHand = stock,
            totalEarningsPaise = earnings,
            duesReceivablePaise = distribution.sumOf { it.duesPaise },
            lossesPaise = resolvedAdjust,
            vendorDistribution = distribution,
        )
    }

    // --- Vendor dashboard aggregate ---
    fun observeVendorDashboard(vendorId: String): Flow<VendorDashboard> = combine(
        deliveryDao.observeForVendor(vendorId),
        paymentDao.observeForVendor(vendorId),
        complaintDao.observeForVendor(vendorId),
        priceDao.observeAll(),
        vendorDao.observeById(vendorId),
    ) { deliveries, payments, complaints, prices, vendor ->
        val received = deliveries.sumOf { it.quantity }
        val value = deliveries.sumOf { it.quantity * it.unitPricePaise }
        val paid = payments.filter { it.status == PaymentStatus.COMPLETED }.sumOf { it.amountPaise }
        val adjust = complaints.filter { it.status == ComplaintStatus.RESOLVED }.sumOf { it.adjustmentPaise }
        val lastPrice = prices.filter { it.vendorId == vendorId }.maxByOrNull { it.effectiveFrom }?.unitPricePaise
            ?: deliveries.maxByOrNull { it.createdAt }?.unitPricePaise
            ?: 0L
        VendorDashboard(
            vendorId = vendorId,
            vendorName = vendor?.name ?: "",
            supplierVpa = SUPPLIER_VPA,
            receivedQty = received,
            lastUnitPricePaise = lastPrice,
            pendingDuesPaise = (value - adjust - paid).coerceAtLeast(0),
        )
    }

    // --- Profit & Loss ---
    fun observePnl(): Flow<PnlReport> = combine(
        purchaseDao.observeAll(),
        deliveryDao.observeAll(),
        complaintDao.observeAll(),
    ) { purchases, deliveries, complaints ->
        PnlReport(
            revenuePaise = deliveries.sumOf { it.quantity * it.unitPricePaise },
            purchaseCostPaise = purchases.sumOf { it.quantity * it.unitCostPaise },
            complaintLossesPaise = complaints
                .filter { it.status == ComplaintStatus.RESOLVED }
                .sumOf { it.adjustmentPaise },
        )
    }

    // --- Writes ---
    suspend fun addDealer(name: String, location: String) =
        dealerDao.upsert(DealerEntity(newId(), name, location))

    suspend fun addPurchase(dealerId: String, quantity: Int, unitCostPaise: Long) =
        purchaseDao.upsert(PurchaseEntity(newId(), dealerId, quantity, unitCostPaise, now()))

    suspend fun addVendor(name: String, phone: String, upiVpa: String?, languageTag: String) =
        vendorDao.upsert(VendorEntity(newId(), name, phone, upiVpa, languageTag))

    suspend fun setPrice(vendorId: String, unitPricePaise: Long) =
        priceDao.upsert(PriceEntity(newId(), vendorId, unitPricePaise, now()))

    suspend fun addDelivery(vendorId: String, quantity: Int, unitPricePaise: Long) =
        deliveryDao.upsert(
            DeliveryEntity(newId(), vendorId, quantity, unitPricePaise, DeliveryStatus.DELIVERED, now(), null)
        )

    suspend fun confirmDelivery(deliveryId: String) {
        val d = deliveryDao.getById(deliveryId) ?: return
        deliveryDao.update(d.copy(status = DeliveryStatus.CONFIRMED, confirmedAt = now()))
    }

    suspend fun recordPayment(vendorId: String, amountPaise: Long, method: String, status: String, note: String?) =
        paymentDao.upsert(
            PaymentEntity(newId(), vendorId, amountPaise, method, status, upiRef = null, note = note, createdAt = now())
        )

    suspend fun raiseComplaint(vendorId: String, deliveryId: String, reason: String, photoUri: String?) =
        complaintDao.upsert(
            ComplaintEntity(newId(), deliveryId, vendorId, reason, photoUri, 0, ComplaintStatus.OPEN, now())
        )

    suspend fun resolveComplaint(complaintId: String, adjustmentPaise: Long) {
        val c = complaintDao.getById(complaintId) ?: return
        complaintDao.update(c.copy(adjustmentPaise = adjustmentPaise, status = ComplaintStatus.RESOLVED))
    }

    private fun newId() = UUID.randomUUID().toString()
    private fun now() = System.currentTimeMillis()

    // --- Batch upserts (used by backend sync) ---
    suspend fun upsertDealers(items: List<DealerEntity>) = items.forEach { dealerDao.upsert(it) }
    suspend fun upsertPurchases(items: List<PurchaseEntity>) = items.forEach { purchaseDao.upsert(it) }
    suspend fun upsertVendors(items: List<VendorEntity>) = items.forEach { vendorDao.upsert(it) }
    suspend fun upsertPrices(items: List<PriceEntity>) = items.forEach { priceDao.upsert(it) }
    suspend fun upsertDeliveries(items: List<DeliveryEntity>) = items.forEach { deliveryDao.upsert(it) }
    suspend fun upsertComplaints(items: List<ComplaintEntity>) = items.forEach { complaintDao.upsert(it) }
    suspend fun upsertPayments(items: List<PaymentEntity>) = items.forEach { paymentDao.upsert(it) }

    companion object {
        /** Phase-1 demo supplier VPA used for UPI deep links. */
        const val SUPPLIER_VPA = "tenco.supplier@upi"
    }
}
