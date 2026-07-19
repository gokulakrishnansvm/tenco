package com.tenco.data.sync

import android.util.Log
import com.tenco.core.prefs.AppPreferences
import com.tenco.data.local.ComplaintEntity
import com.tenco.data.local.DealerEntity
import com.tenco.data.local.DeliveryEntity
import com.tenco.data.local.PaymentEntity
import com.tenco.data.local.PriceEntity
import com.tenco.data.local.PurchaseEntity
import com.tenco.data.local.VendorEntity
import com.tenco.data.remote.RemoteSyncChanges
import com.tenco.data.remote.RemoteDealer
import com.tenco.data.remote.RemotePurchase
import com.tenco.data.remote.RemoteVendor
import com.tenco.data.remote.RemotePrice
import com.tenco.data.remote.RemoteDelivery
import com.tenco.data.remote.RemoteComplaint
import com.tenco.data.remote.RemotePayment
import com.tenco.data.remote.TencoApi
import com.tenco.data.repository.TencoRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pulls delta changes from the backend and applies them into Room (offline-first cache).
 * Best-effort: failures (offline / no token) are swallowed so the local experience continues.
 */
@Singleton
class SyncManager @Inject constructor(
    private val api: TencoApi,
    private val repository: TencoRepository,
    private val prefs: AppPreferences,
) {
    /** Returns the number of records applied, or null if the pull failed/skipped. */
    suspend fun pull(): Int? {
        if (prefs.accessToken.isNullOrBlank()) return null // not authenticated (offline login)
        return try {
            val changes = api.syncChanges(prefs.lastSyncCursor)
            apply(changes)
            prefs.lastSyncCursor = changes.cursor
            changes.total().also { Log.d(TAG, "sync applied $it records; cursor=${changes.cursor}") }
        } catch (e: Exception) {
            Log.w(TAG, "sync pull failed: ${e.message}")
            null
        }
    }

    /** Pushes queued local changes (outbox) to the backend, clearing them on success. */
    suspend fun push(): Int? {
        if (prefs.accessToken.isNullOrBlank()) return null
        return try {
            val entries = repository.outboxAll()
            if (entries.isEmpty()) return 0
            fun idsOf(type: String) = entries.filter { it.entityType == type }.map { it.entityId }.distinct()
            val changes = RemoteSyncChanges(
                cursor = 0,
                dealers = repository.dealersByIds(idsOf(TencoRepository.OUT_DEALER)).map { RemoteDealer(it.id, it.name, it.location) },
                purchases = repository.purchasesByIds(idsOf(TencoRepository.OUT_PURCHASE)).map { RemotePurchase(it.id, it.dealerId, it.quantity, it.unitCostPaise, it.createdAt) },
                vendors = repository.vendorsByIds(idsOf(TencoRepository.OUT_VENDOR)).map { RemoteVendor(it.id, it.name, it.phone, it.upiVpa, it.languageTag) },
                prices = repository.pricesByIds(idsOf(TencoRepository.OUT_PRICE)).map { RemotePrice(it.id, it.vendorId, it.unitPricePaise, it.effectiveFrom) },
                deliveries = repository.deliveriesByIds(idsOf(TencoRepository.OUT_DELIVERY)).map { RemoteDelivery(it.id, it.vendorId, it.quantity, it.unitPricePaise, it.status, it.createdAt, it.confirmedAt) },
                complaints = repository.complaintsByIds(idsOf(TencoRepository.OUT_COMPLAINT)).map { RemoteComplaint(it.id, it.deliveryId, it.vendorId, it.reason, it.photoUri, it.adjustmentPaise, it.status, it.createdAt) },
                payments = repository.paymentsByIds(idsOf(TencoRepository.OUT_PAYMENT)).map { RemotePayment(it.id, it.vendorId, it.amountPaise, it.method, it.status, it.upiRef, it.note, it.createdAt) },
            )
            api.pushChanges(changes)
            repository.clearOutbox(entries.map { it.seq.toString() })
            entries.size.also { Log.d(TAG, "outbox pushed $it entries") }
        } catch (e: Exception) {
            Log.w(TAG, "sync push failed: ${e.message}")
            null
        }
    }

    /** Full sync: push local changes first, then pull remote changes. */
    suspend fun sync() {
        push()
        pull()
    }

    private suspend fun apply(c: RemoteSyncChanges) {
        repository.upsertDealers(c.dealers.map { DealerEntity(it.id, it.name, it.location) })
        repository.upsertPurchases(c.purchases.map { PurchaseEntity(it.id, it.dealerId, it.quantity, it.unitCostPaise, it.createdAt) })
        repository.upsertVendors(c.vendors.map { VendorEntity(it.id, it.name, it.phone, it.upiVpa, it.languageTag) })
        repository.upsertPrices(c.prices.map { PriceEntity(it.id, it.vendorId, it.unitPricePaise, it.effectiveFrom) })
        repository.upsertDeliveries(c.deliveries.map { DeliveryEntity(it.id, it.vendorId, it.quantity, it.unitPricePaise, it.status, it.createdAt, it.confirmedAt) })
        repository.upsertComplaints(c.complaints.map { ComplaintEntity(it.id, it.deliveryId, it.vendorId, it.reason, it.photoUrl, it.adjustmentPaise, it.status, it.createdAt) })
        repository.upsertPayments(c.payments.map { PaymentEntity(it.id, it.vendorId, it.amountPaise, it.method, it.status, it.upiRef, it.note, it.createdAt) })
    }

    private fun RemoteSyncChanges.total() =
        dealers.size + purchases.size + vendors.size + prices.size + deliveries.size + complaints.size + payments.size

    companion object {
        private const val TAG = "TencoSync"
    }
}
