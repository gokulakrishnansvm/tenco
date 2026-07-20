package com.tenco.feature.vendor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenco.data.local.ComplaintEntity
import com.tenco.data.local.DeliveryEntity
import com.tenco.data.local.PaymentEntity
import com.tenco.data.repository.TencoRepository
import com.tenco.domain.PaymentMethod
import com.tenco.domain.PaymentStatus
import com.tenco.domain.VendorDashboard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VendorViewModel @Inject constructor(
    private val repository: TencoRepository,
    private val api: com.tenco.data.remote.TencoApi,
    private val syncManager: com.tenco.data.sync.SyncManager,
    private val tts: com.tenco.core.tts.TtsManager,
) : ViewModel() {

    private val vendorId = MutableStateFlow<String?>(null)

    fun speak(languageTag: String, text: String) {
        tts.setLanguage(languageTag)
        tts.speak(text)
    }

    /** Set when a backend payment intent was created, so the manual confirmation reconciles
     *  against the same payment id the backend/webhook will update. */
    private var lastIntentId: String? = null

    fun setVendor(id: String) {
        if (id.isNotBlank()) vendorId.value = id
    }

    val dashboard: StateFlow<VendorDashboard?> =
        vendorId.filterNotNull()
            .flatMapLatest { repository.observeVendorDashboard(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val deliveries: StateFlow<List<DeliveryEntity>> =
        vendorId.filterNotNull()
            .flatMapLatest { repository.observeDeliveriesForVendor(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> =
        vendorId.filterNotNull()
            .flatMapLatest { repository.observePaymentsForVendor(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val complaints: StateFlow<List<ComplaintEntity>> =
        vendorId.filterNotNull()
            .flatMapLatest { repository.observeComplaintsForVendor(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val orders: StateFlow<List<com.tenco.data.local.OrderEntity>> =
        vendorId.filterNotNull()
            .flatMapLatest { repository.observeOrdersForVendor(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun placeOrder(quantity: Int) = viewModelScope.launch {
        val id = vendorId.value ?: return@launch
        if (quantity > 0) repository.placeOrder(id, quantity)
    }

    fun payOrder(orderId: String) = viewModelScope.launch { repository.markOrderPaid(orderId) }
    fun cancelOrder(orderId: String) = viewModelScope.launch { repository.requestOrderCancel(orderId) }

    /** Records a cash payment that stays in review until the supplier approves it. */
    fun payCash(amountPaise: Long) = viewModelScope.launch {
        val id = vendorId.value ?: return@launch
        if (amountPaise > 0) {
            repository.recordPayment(id, amountPaise, PaymentMethod.CASH, PaymentStatus.PENDING_VERIFICATION, note = "Cash")
        }
    }

    fun confirmLatestDelivery() = viewModelScope.launch {
        deliveries.value.firstOrNull { it.confirmedAt == null }?.let {
            repository.confirmDelivery(it.id)
        }
    }

    fun recordPayment(amountPaise: Long, success: Boolean) = viewModelScope.launch {
        val id = vendorId.value ?: return@launch
        val status = if (success) PaymentStatus.COMPLETED else PaymentStatus.FAILED
        // Reuse the backend intent id when present so a later webhook-driven sync reconciles
        // the same record instead of creating a duplicate.
        repository.recordPayment(id, amountPaise, PaymentMethod.UPI, status, note = null, id = lastIntentId)
        lastIntentId = null
        syncManager.sync() // push the payment + pull authoritative status if backend reachable
    }

    /**
     * Creates a backend payment intent (Razorpay order + UPI link). Invokes [onResult] with the
     * backend-provided UPI deep link, or null to fall back to a locally-built UPI link (offline).
     */
    fun createBackendIntent(amountPaise: Long, onResult: (String?) -> Unit) {
        val id = vendorId.value ?: return onResult(null)
        viewModelScope.launch {
            val link = try {
                val intent = api.createPaymentIntent(id, amountPaise)
                lastIntentId = intent.intentId
                intent.upiDeepLink
            } catch (e: Exception) {
                lastIntentId = null
                null
            }
            onResult(link)
        }
    }

    fun raiseComplaint(reason: String, photoUri: String?, shortQuantity: Int = 0) = viewModelScope.launch {
        val id = vendorId.value ?: return@launch
        val deliveryId = deliveries.value.firstOrNull()?.id ?: ""
        repository.raiseComplaint(id, deliveryId, reason, photoUri, shortQuantity)
    }
}
