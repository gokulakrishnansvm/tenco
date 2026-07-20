package com.tenco.data.repository

import com.tenco.data.local.ComplaintEntity
import com.tenco.data.local.DealerEntity
import com.tenco.data.local.DeliveryEntity
import com.tenco.data.local.OrderEntity
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
    private val orderDao = db.orderDao()
    private val outboxDao = db.outboxDao()

    // --- Raw observers ---
    fun observeDealers(): Flow<List<DealerEntity>> = dealerDao.observeActive()
    fun observeAllDealers(): Flow<List<DealerEntity>> = dealerDao.observeAll()
    fun observePurchases(): Flow<List<PurchaseEntity>> = purchaseDao.observeAll()
    fun observeVendors(): Flow<List<VendorEntity>> = vendorDao.observeActive()
    fun observeAllVendors(): Flow<List<VendorEntity>> = vendorDao.observeAll()
    fun observePrices(): Flow<List<PriceEntity>> = priceDao.observeAll()
    fun observeDeliveries(): Flow<List<DeliveryEntity>> = deliveryDao.observeAll()
    fun observePayments(): Flow<List<PaymentEntity>> = paymentDao.observeAll()
    fun observeComplaints(): Flow<List<ComplaintEntity>> = complaintDao.observeAll()
    fun observeOrders(): Flow<List<OrderEntity>> = orderDao.observeAll()
    fun observeOrdersForVendor(vendorId: String): Flow<List<OrderEntity>> = orderDao.observeForVendor(vendorId)
    fun observeOrder(id: String): Flow<OrderEntity?> = orderDao.observeById(id)
    fun observeNewOrderCount(): Flow<Int> = orderDao.observeNewCount()
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

    // --- Analytics / insights ---
    fun observeInsights(): Flow<com.tenco.domain.SupplierInsights> = combine(
        deliveryDao.observeAll(),
        paymentDao.observeAll(),
        complaintDao.observeAll(),
        vendorDao.observeAll(),
    ) { deliveries, payments, complaints, vendors ->
        val billed = deliveries.sumOf { it.quantity * it.unitPricePaise }
        val collected = payments.filter { it.status == PaymentStatus.COMPLETED }.sumOf { it.amountPaise }
        val adjustments = complaints.filter { it.status == ComplaintStatus.RESOLVED }.sumOf { it.adjustmentPaise }
        val net = (billed - adjustments).coerceAtLeast(0)
        val rate = if (net > 0) ((collected * 100) / net).toInt().coerceIn(0, 100) else 0
        val top = vendors.map { v ->
            val value = deliveries.filter { it.vendorId == v.id }.sumOf { it.quantity * it.unitPricePaise }
            val paid = payments.filter { it.vendorId == v.id && it.status == PaymentStatus.COMPLETED }.sumOf { it.amountPaise }
            val adj = complaints.filter { it.vendorId == v.id && it.status == ComplaintStatus.RESOLVED }.sumOf { it.adjustmentPaise }
            val qty = deliveries.filter { it.vendorId == v.id }.sumOf { it.quantity }
            VendorDistribution(v.id, v.name, qty, (value - adj - paid).coerceAtLeast(0))
        }.sortedByDescending { it.duesPaise }.take(5)
        com.tenco.domain.SupplierInsights(
            totalBilledPaise = billed,
            totalCollectedPaise = collected,
            outstandingPaise = (net - collected).coerceAtLeast(0),
            collectionRatePercent = rate,
            deliveriesCount = deliveries.size,
            topVendors = top,
        )
    }

    // --- Profit & Loss ---
    fun observePnl(from: Long = 0L, to: Long = Long.MAX_VALUE): Flow<PnlReport> = combine(
        purchaseDao.observeAll(),
        deliveryDao.observeAll(),
        complaintDao.observeAll(),
    ) { purchases, deliveries, complaints ->
        fun inRange(t: Long) = t in from..to
        PnlReport(
            revenuePaise = deliveries.filter { inRange(it.createdAt) }.sumOf { it.quantity * it.unitPricePaise },
            purchaseCostPaise = purchases.filter { inRange(it.createdAt) }.sumOf { it.quantity * it.unitCostPaise },
            complaintLossesPaise = complaints
                .filter { it.status == ComplaintStatus.RESOLVED && inRange(it.createdAt) }
                .sumOf { it.adjustmentPaise },
        )
    }

    // --- Writes ---
    suspend fun addDealer(name: String, location: String) {
        val e = DealerEntity(newId(), name, location); dealerDao.upsert(e); enqueue(OUT_DEALER, e.id)
    }

    suspend fun addPurchase(dealerId: String, quantity: Int, unitCostPaise: Long) {
        val e = PurchaseEntity(newId(), dealerId, quantity, unitCostPaise, now()); purchaseDao.upsert(e); enqueue(OUT_PURCHASE, e.id)
    }

    /** One purchase line: a colour+grade of coconut at a quantity and unit cost. */
    data class PurchaseLine(val color: String, val grade: String, val quantity: Int, val unitCostPaise: Long)

    /** Records multiple colour/grade lines as a single dealer batch. */
    suspend fun addPurchaseBatch(dealerId: String, lines: List<PurchaseLine>) {
        val batch = newId(); val ts = now()
        lines.filter { it.quantity > 0 }.forEach { line ->
            val e = PurchaseEntity(newId(), dealerId, line.quantity, line.unitCostPaise, ts, batchId = batch, color = line.color, grade = line.grade)
            purchaseDao.upsert(e); enqueue(OUT_PURCHASE, e.id)
        }
    }

    suspend fun addVendor(name: String, phone: String, upiVpa: String?, languageTag: String, city: String = "") {
        val e = VendorEntity(newId(), name, phone, upiVpa, languageTag, city = city); vendorDao.upsert(e); enqueue(OUT_VENDOR, e.id)
    }

    /** Sets one price for many vendors at once; skips any whose latest price already equals it. */
    suspend fun setPriceForVendors(vendorIds: List<String>, unitPricePaise: Long) {
        vendorIds.forEach { vid ->
            if (priceDao.latestForVendor(vid)?.unitPricePaise != unitPricePaise) {
                val e = PriceEntity(newId(), vid, unitPricePaise, now()); priceDao.upsert(e); enqueue(OUT_PRICE, e.id)
            }
        }
    }

    suspend fun setPrice(vendorId: String, unitPricePaise: Long) {
        val e = PriceEntity(newId(), vendorId, unitPricePaise, now()); priceDao.upsert(e); enqueue(OUT_PRICE, e.id)
    }

    // ----------------- Vendor orders -----------------

    /** Vendor places an order; prefills the last agreed unit price (if any) so they can pre-pay. */
    suspend fun placeOrder(vendorId: String, quantity: Int) {
        val lastPrice = priceDao.latestForVendor(vendorId)?.unitPricePaise
        val e = OrderEntity(
            id = newId(), vendorId = vendorId, quantity = quantity,
            unitPricePaise = lastPrice, status = com.tenco.domain.OrderStatus.PLACED,
            paid = false, createdAt = now(), updatedAt = now(),
        )
        orderDao.upsert(e)
    }

    /** Supplier sets the unit price and confirms the order. */
    suspend fun setOrderPrice(orderId: String, unitPricePaise: Long) {
        val o = orderDao.getById(orderId) ?: return
        orderDao.upsert(o.copy(unitPricePaise = unitPricePaise, status = com.tenco.domain.OrderStatus.CONFIRMED, updatedAt = now()))
    }

    /** Supplier advances the order along its pipeline. On DELIVERED, records a sale (delivery). */
    suspend fun advanceOrderStatus(orderId: String, status: String) {
        val o = orderDao.getById(orderId) ?: return
        orderDao.upsert(o.copy(status = status, updatedAt = now()))
        if (status == com.tenco.domain.OrderStatus.DELIVERED && o.unitPricePaise != null) {
            val d = DeliveryEntity(newId(), o.vendorId, o.quantity, o.unitPricePaise, DeliveryStatus.DELIVERED, now(), now())
            deliveryDao.upsert(d); enqueue(OUT_DELIVERY, d.id)
        }
    }

    /** Vendor pays for a priced order; records the payment and marks the order paid. */
    suspend fun markOrderPaid(orderId: String) {
        val o = orderDao.getById(orderId) ?: return
        val unit = o.unitPricePaise ?: return
        val total = unit * o.quantity
        val p = PaymentEntity(newId(), o.vendorId, total, com.tenco.domain.PaymentMethod.ORDER, PaymentStatus.COMPLETED, upiRef = null, note = "Order ${o.id.take(6)}", createdAt = now())
        paymentDao.upsert(p); enqueue(OUT_PAYMENT, p.id)
        orderDao.upsert(o.copy(paid = true, updatedAt = now()))
    }

    /** Vendor requests cancellation (allowed only before dispatch). */
    suspend fun requestOrderCancel(orderId: String) {
        val o = orderDao.getById(orderId) ?: return
        if (com.tenco.domain.OrderStatus.cancellable(o.status)) {
            orderDao.upsert(o.copy(status = com.tenco.domain.OrderStatus.CANCEL_REQUESTED, updatedAt = now()))
        }
    }

    /** Supplier confirms a cancellation request. */
    suspend fun confirmOrderCancel(orderId: String) {
        val o = orderDao.getById(orderId) ?: return
        orderDao.upsert(o.copy(status = com.tenco.domain.OrderStatus.CANCELLED, updatedAt = now()))
    }

    suspend fun addDelivery(vendorId: String, quantity: Int, unitPricePaise: Long, color: String = "GREEN", grade: String = "MEDIUM") {
        val e = DeliveryEntity(newId(), vendorId, quantity, unitPricePaise, DeliveryStatus.DELIVERED, now(), null, color = color, grade = grade)
        deliveryDao.upsert(e); enqueue(OUT_DELIVERY, e.id)
    }

    suspend fun confirmDelivery(deliveryId: String) {
        val d = deliveryDao.getById(deliveryId) ?: return
        deliveryDao.update(d.copy(status = DeliveryStatus.CONFIRMED, confirmedAt = now()))
        enqueue(OUT_DELIVERY, deliveryId)
    }

    suspend fun recordPayment(vendorId: String, amountPaise: Long, method: String, status: String, note: String?, id: String? = null) {
        val e = PaymentEntity(id ?: newId(), vendorId, amountPaise, method, status, upiRef = null, note = note, createdAt = now())
        paymentDao.upsert(e); enqueue(OUT_PAYMENT, e.id)
    }

    /** Supplier approves a vendor's pending cash payment; it then counts toward dues/earnings. */
    suspend fun approvePayment(paymentId: String) {
        val p = paymentDao.getById(paymentId) ?: return
        paymentDao.upsert(p.copy(status = PaymentStatus.COMPLETED)); enqueue(OUT_PAYMENT, paymentId)
    }

    /** Supplier rejects a vendor's pending cash payment. */
    suspend fun rejectPayment(paymentId: String) {
        val p = paymentDao.getById(paymentId) ?: return
        paymentDao.upsert(p.copy(status = PaymentStatus.REJECTED)); enqueue(OUT_PAYMENT, paymentId)
    }

    suspend fun raiseComplaint(vendorId: String, deliveryId: String, reason: String, photoUri: String?, shortQuantity: Int = 0) {
        val e = ComplaintEntity(newId(), deliveryId, vendorId, reason, photoUri, 0, ComplaintStatus.OPEN, now(), shortQuantity = shortQuantity)
        complaintDao.upsert(e); enqueue(OUT_COMPLAINT, e.id)
    }

    suspend fun resolveComplaint(complaintId: String, adjustmentPaise: Long) {
        val c = complaintDao.getById(complaintId) ?: return
        complaintDao.update(c.copy(adjustmentPaise = adjustmentPaise, status = ComplaintStatus.RESOLVED))
        enqueue(OUT_COMPLAINT, complaintId)
    }

    /** Dispute lifecycle transition (e.g. OPEN -> UNDER_REVIEW, or -> REJECTED). */
    suspend fun setComplaintStatus(complaintId: String, status: String) {
        val c = complaintDao.getById(complaintId) ?: return
        complaintDao.update(c.copy(status = status))
        enqueue(OUT_COMPLAINT, complaintId)
    }

    private suspend fun enqueue(type: String, id: String) =
        outboxDao.insert(com.tenco.data.local.OutboxEntity(entityType = type, entityId = id, createdAt = now()))

    /**
     * Archives a vendor: removes them from active lists but preserves ALL their
     * transactions (sales/payments/complaints/prices) so ledger history stays intact.
     */
    suspend fun deleteVendor(vendorId: String) {
        vendorDao.setArchived(vendorId, true)
    }

    /** Archives a dealer: removes from active lists but preserves all purchase history. */
    suspend fun deleteDealer(dealerId: String) {
        dealerDao.setArchived(dealerId, true)
    }

    private fun newId() = UUID.randomUUID().toString()
    private fun now() = System.currentTimeMillis()

    // --- Outbox access (client->server sync) ---
    suspend fun outboxAll() = outboxDao.all()
    suspend fun clearOutbox(seqs: List<String>) = outboxDao.deleteBySeqs(seqs.map { it.toLong() })
    suspend fun dealersByIds(ids: List<String>) = dealerDao.getByIds(ids)
    suspend fun purchasesByIds(ids: List<String>) = purchaseDao.getByIds(ids)
    suspend fun vendorsByIds(ids: List<String>) = vendorDao.getByIds(ids)
    suspend fun pricesByIds(ids: List<String>) = priceDao.getByIds(ids)
    suspend fun deliveriesByIds(ids: List<String>) = deliveryDao.getByIds(ids)
    suspend fun complaintsByIds(ids: List<String>) = complaintDao.getByIds(ids)
    suspend fun paymentsByIds(ids: List<String>) = paymentDao.getByIds(ids)

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
        const val OUT_DEALER = "dealer"
        const val OUT_PURCHASE = "purchase"
        const val OUT_VENDOR = "vendor"
        const val OUT_PRICE = "price"
        const val OUT_DELIVERY = "delivery"
        const val OUT_COMPLAINT = "complaint"
        const val OUT_PAYMENT = "payment"
    }
}
