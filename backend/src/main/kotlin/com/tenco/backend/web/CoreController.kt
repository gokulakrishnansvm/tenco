package com.tenco.backend.web

import com.tenco.backend.core.CoreService
import com.tenco.backend.domain.*
import org.springframework.web.bind.annotation.*

// ---- Request bodies ----
data class DealerBody(val name: String, val location: String)
data class PurchaseBody(val dealerId: String, val quantity: Int, val unitCostPaise: Long)
data class VendorBody(val name: String, val phone: String, val upiVpa: String?, val languageTag: String = "en")
data class PriceBody(val vendorId: String, val unitPricePaise: Long)
data class DeliveryBody(val vendorId: String, val quantity: Int, val unitPricePaise: Long)
data class ComplaintBody(val vendorId: String, val deliveryId: String, val reason: String, val photoUrl: String?)
data class ResolveBody(val adjustmentPaise: Long)

@RestController
@RequestMapping("/api")
class CoreController(
    private val dealers: DealerRepository,
    private val purchases: PurchaseRepository,
    private val vendors: VendorRepository,
    private val prices: PriceRepository,
    private val deliveries: DeliveryRepository,
    private val complaints: ComplaintRepository,
    private val payments: PaymentRepository,
    private val core: CoreService,
) {
    // Dealers & purchases
    @GetMapping("/dealers") fun dealers() = dealers.findAll()
    @PostMapping("/dealers") fun addDealer(@RequestBody b: DealerBody) =
        dealers.save(Dealer(name = b.name, location = b.location))
    @GetMapping("/purchases") fun purchases() = purchases.findAll()
    @PostMapping("/purchases") fun addPurchase(@RequestBody b: PurchaseBody) =
        purchases.save(Purchase(dealerId = b.dealerId, quantity = b.quantity, unitCostPaise = b.unitCostPaise))

    // Vendors & prices
    @GetMapping("/vendors") fun vendors() = vendors.findAll()
    @PostMapping("/vendors") fun addVendor(@RequestBody b: VendorBody) =
        vendors.save(Vendor(name = b.name, phone = b.phone, upiVpa = b.upiVpa, languageTag = b.languageTag))
    @GetMapping("/prices") fun prices(@RequestParam(required = false) vendorId: String?) =
        if (vendorId != null) prices.findByVendorIdOrderByEffectiveFromDesc(vendorId) else prices.findAll()
    @PutMapping("/prices") fun setPrice(@RequestBody b: PriceBody) =
        prices.save(Price(vendorId = b.vendorId, unitPricePaise = b.unitPricePaise))

    // Deliveries
    @GetMapping("/deliveries") fun deliveries(@RequestParam(required = false) vendorId: String?) =
        if (vendorId != null) deliveries.findByVendorId(vendorId) else deliveries.findAll()
    @PostMapping("/deliveries") fun addDelivery(@RequestBody b: DeliveryBody) =
        deliveries.save(Delivery(vendorId = b.vendorId, quantity = b.quantity, unitPricePaise = b.unitPricePaise))
    @PostMapping("/deliveries/{id}/confirm") fun confirm(@PathVariable id: String): Delivery {
        val d = deliveries.findById(id).orElseThrow()
        d.status = "CONFIRMED"; d.confirmedAt = now(); d.updatedAt = now()
        return deliveries.save(d)
    }

    // Complaints
    @GetMapping("/complaints") fun complaints(@RequestParam(required = false) vendorId: String?) =
        if (vendorId != null) complaints.findByVendorId(vendorId) else complaints.findAll()
    @PostMapping("/complaints") fun addComplaint(@RequestBody b: ComplaintBody) =
        complaints.save(Complaint(vendorId = b.vendorId, deliveryId = b.deliveryId, reason = b.reason, photoUrl = b.photoUrl))
    @PutMapping("/complaints/{id}/resolve") fun resolve(@PathVariable id: String, @RequestBody b: ResolveBody): Complaint {
        val c = complaints.findById(id).orElseThrow()
        c.status = "RESOLVED"; c.adjustmentPaise = b.adjustmentPaise; c.updatedAt = now()
        return complaints.save(c)
    }

    // Payments (read)
    @GetMapping("/payments") fun payments(@RequestParam(required = false) vendorId: String?) =
        if (vendorId != null) payments.findByVendorId(vendorId) else payments.findAll()

    // Dashboards & reports
    @GetMapping("/suppliers/me/dashboard") fun supplierDashboard() = core.supplierDashboard()
    @GetMapping("/vendors/{id}/dashboard") fun vendorDashboard(@PathVariable id: String) = core.vendorDashboard(id)
    @GetMapping("/reports/pnl") fun pnl() = core.pnl()
}

// ---- Delta sync ----
data class SyncChanges(
    val cursor: Long,
    val dealers: List<Dealer>,
    val purchases: List<Purchase>,
    val vendors: List<Vendor>,
    val prices: List<Price>,
    val deliveries: List<Delivery>,
    val complaints: List<Complaint>,
    val payments: List<Payment>,
)

@RestController
@RequestMapping("/api/sync")
class SyncController(
    private val dealers: DealerRepository,
    private val purchases: PurchaseRepository,
    private val vendors: VendorRepository,
    private val prices: PriceRepository,
    private val deliveries: DeliveryRepository,
    private val complaints: ComplaintRepository,
    private val payments: PaymentRepository,
) {
    /** Returns all records changed after [since] (epoch millis). Clients pass 0 for a full pull. */
    @GetMapping("/changes")
    fun changes(@RequestParam(defaultValue = "0") since: Long): SyncChanges = SyncChanges(
        cursor = System.currentTimeMillis(),
        dealers = dealers.findByUpdatedAtGreaterThan(since),
        purchases = purchases.findByUpdatedAtGreaterThan(since),
        vendors = vendors.findByUpdatedAtGreaterThan(since),
        prices = prices.findByUpdatedAtGreaterThan(since),
        deliveries = deliveries.findByUpdatedAtGreaterThan(since),
        complaints = complaints.findByUpdatedAtGreaterThan(since),
        payments = payments.findByUpdatedAtGreaterThan(since),
    )
}
