package com.tenco.backend.core

import com.tenco.backend.domain.*
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

// ---- Aggregate DTOs ----
data class VendorDistributionDto(val vendorId: String, val vendorName: String, val quantity: Int, val duesPaise: Long)
data class SupplierDashboardDto(
    val stockOnHand: Int,
    val totalEarningsPaise: Long,
    val duesReceivablePaise: Long,
    val lossesPaise: Long,
    val vendorDistribution: List<VendorDistributionDto>,
)
data class VendorDashboardDto(
    val vendorId: String,
    val vendorName: String,
    val receivedQty: Int,
    val lastUnitPricePaise: Long,
    val pendingDuesPaise: Long,
)
data class PnlDto(val revenuePaise: Long, val purchaseCostPaise: Long, val complaintLossesPaise: Long) {
    val netProfitPaise: Long get() = revenuePaise - purchaseCostPaise - complaintLossesPaise
}

@Service
class CoreService(
    private val dealers: DealerRepository,
    private val purchases: PurchaseRepository,
    private val vendors: VendorRepository,
    private val prices: PriceRepository,
    private val deliveries: DeliveryRepository,
    private val complaints: ComplaintRepository,
    private val payments: PaymentRepository,
) {
    fun supplierDashboard(): SupplierDashboardDto {
        val allDeliveries = deliveries.findAll()
        val allPayments = payments.findAll()
        val allComplaints = complaints.findAll()
        val stock = purchases.findAll().sumOf { it.quantity } - allDeliveries.sumOf { it.quantity }
        val earnings = allPayments.filter { it.status == "COMPLETED" }.sumOf { it.amountPaise }
        val losses = allComplaints.filter { it.status == "RESOLVED" }.sumOf { it.adjustmentPaise }
        val distribution = vendors.findAll().map { v ->
            val delivered = allDeliveries.filter { it.vendorId == v.id }
            val value = delivered.sumOf { it.quantity * it.unitPricePaise }
            val paid = allPayments.filter { it.vendorId == v.id && it.status == "COMPLETED" }.sumOf { it.amountPaise }
            val adjust = allComplaints.filter { it.vendorId == v.id && it.status == "RESOLVED" }.sumOf { it.adjustmentPaise }
            VendorDistributionDto(v.id, v.name, delivered.sumOf { it.quantity }, (value - adjust - paid).coerceAtLeast(0))
        }
        return SupplierDashboardDto(stock, earnings, distribution.sumOf { it.duesPaise }, losses, distribution)
    }

    fun vendorDashboard(vendorId: String): VendorDashboardDto {
        val v = vendors.findById(vendorId).orElse(null)
        val vDeliveries = deliveries.findByVendorId(vendorId)
        val value = vDeliveries.sumOf { it.quantity * it.unitPricePaise }
        val paid = payments.findByVendorId(vendorId).filter { it.status == "COMPLETED" }.sumOf { it.amountPaise }
        val adjust = complaints.findByVendorId(vendorId).filter { it.status == "RESOLVED" }.sumOf { it.adjustmentPaise }
        val lastPrice = prices.findByVendorIdOrderByEffectiveFromDesc(vendorId).firstOrNull()?.unitPricePaise
            ?: vDeliveries.maxByOrNull { it.createdAt }?.unitPricePaise ?: 0L
        return VendorDashboardDto(
            vendorId = vendorId,
            vendorName = v?.name ?: "",
            receivedQty = vDeliveries.sumOf { it.quantity },
            lastUnitPricePaise = lastPrice,
            pendingDuesPaise = (value - adjust - paid).coerceAtLeast(0),
        )
    }

    fun pnl(): PnlDto = PnlDto(
        revenuePaise = deliveries.findAll().sumOf { it.quantity * it.unitPricePaise },
        purchaseCostPaise = purchases.findAll().sumOf { it.quantity * it.unitCostPaise },
        complaintLossesPaise = complaints.findAll().filter { it.status == "RESOLVED" }.sumOf { it.adjustmentPaise },
    )
}

/** Seeds demo data on startup if the DB is empty (mirrors the mobile app seed). */
@Component
class SeedRunner(
    private val dealers: DealerRepository,
    private val purchases: PurchaseRepository,
    private val vendors: VendorRepository,
    private val prices: PriceRepository,
    private val deliveries: DeliveryRepository,
    private val payments: PaymentRepository,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        if (dealers.count() > 0) return
        val now = now(); val day = 24 * 60 * 60 * 1000L

        val pollachi = dealers.save(Dealer(name = "Pollachi Traders", location = "Pollachi"))
        val nellore = dealers.save(Dealer(name = "Nellore Coconuts", location = "Nellore"))
        val theni = dealers.save(Dealer(name = "Theni Farms", location = "Theni"))
        purchases.save(Purchase(dealerId = pollachi.id, quantity = 500, unitCostPaise = 2000, createdAt = now - 5 * day))
        purchases.save(Purchase(dealerId = nellore.id, quantity = 300, unitCostPaise = 1800, createdAt = now - 3 * day))
        purchases.save(Purchase(dealerId = theni.id, quantity = 200, unitCostPaise = 2200, createdAt = now - day))

        val ravi = vendors.save(Vendor(name = "Ravi Stall", phone = "+919876543210", upiVpa = "ravi@upi", languageTag = "ta"))
        val lakshmi = vendors.save(Vendor(name = "Lakshmi Cart", phone = "+919812345678", upiVpa = "lakshmi@upi", languageTag = "te"))
        val kumar = vendors.save(Vendor(name = "Kumar Shop", phone = "+919800011122", upiVpa = "kumar@upi", languageTag = "hi"))
        prices.save(Price(vendorId = ravi.id, unitPricePaise = 2800, effectiveFrom = now - 4 * day))
        prices.save(Price(vendorId = lakshmi.id, unitPricePaise = 3000, effectiveFrom = now - 4 * day))
        prices.save(Price(vendorId = kumar.id, unitPricePaise = 2900, effectiveFrom = now - 4 * day))

        deliveries.save(Delivery(vendorId = ravi.id, quantity = 50, unitPricePaise = 2800, status = "CONFIRMED", createdAt = now - 3 * day, confirmedAt = now - 3 * day))
        deliveries.save(Delivery(vendorId = ravi.id, quantity = 40, unitPricePaise = 2800, status = "DELIVERED", createdAt = now - day))
        deliveries.save(Delivery(vendorId = lakshmi.id, quantity = 60, unitPricePaise = 3000, status = "CONFIRMED", createdAt = now - 2 * day, confirmedAt = now - 2 * day))
        deliveries.save(Delivery(vendorId = kumar.id, quantity = 30, unitPricePaise = 2900, status = "DELIVERED", createdAt = now - 6 * 60 * 60 * 1000L))

        payments.save(Payment(vendorId = ravi.id, amountPaise = 100000, status = "COMPLETED", upiRef = "UPI123", note = "Partial", createdAt = now - 2 * day))
        payments.save(Payment(vendorId = lakshmi.id, amountPaise = 180000, status = "COMPLETED", upiRef = "UPI456", createdAt = now - day))
    }
}
