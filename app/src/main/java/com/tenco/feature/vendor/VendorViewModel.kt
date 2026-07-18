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
) : ViewModel() {

    private val vendorId = MutableStateFlow<String?>(null)

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

    fun confirmLatestDelivery() = viewModelScope.launch {
        deliveries.value.firstOrNull { it.confirmedAt == null }?.let {
            repository.confirmDelivery(it.id)
        }
    }

    fun recordPayment(amountPaise: Long, success: Boolean) = viewModelScope.launch {
        val id = vendorId.value ?: return@launch
        val status = if (success) PaymentStatus.COMPLETED else PaymentStatus.FAILED
        repository.recordPayment(id, amountPaise, PaymentMethod.UPI, status, note = null)
    }

    fun raiseComplaint(reason: String, photoUri: String?) = viewModelScope.launch {
        val id = vendorId.value ?: return@launch
        val deliveryId = deliveries.value.firstOrNull()?.id ?: ""
        repository.raiseComplaint(id, deliveryId, reason, photoUri)
    }
}
