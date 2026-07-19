package com.tenco.feature.supplier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenco.data.local.ComplaintEntity
import com.tenco.data.local.DealerEntity
import com.tenco.data.local.DeliveryEntity
import com.tenco.data.local.PaymentEntity
import com.tenco.data.local.PriceEntity
import com.tenco.data.local.PurchaseEntity
import com.tenco.data.local.VendorEntity
import com.tenco.data.repository.TencoRepository
import com.tenco.domain.PnlReport
import com.tenco.domain.SupplierDashboard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/** Reporting period for the P&L report / export. */
enum class ReportPeriod {
    ALL, THIS_MONTH;

    fun from(): Long = when (this) {
        ALL -> 0L
        THIS_MONTH -> Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun to(): Long = Long.MAX_VALUE
}

@HiltViewModel
class SupplierViewModel @Inject constructor(
    private val repository: TencoRepository,
    private val syncManager: com.tenco.data.sync.SyncManager,
) : ViewModel() {

    private val _refreshing = kotlinx.coroutines.flow.MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing

    fun refresh() = viewModelScope.launch {
        _refreshing.value = true
        syncManager.sync()
        kotlinx.coroutines.delay(500)
        _refreshing.value = false
    }

    val dashboard: StateFlow<SupplierDashboard> =
        repository.observeSupplierDashboard().stateInVm(SupplierDashboard())

    val dealers: StateFlow<List<DealerEntity>> =
        repository.observeDealers().stateInVm(emptyList())

    val purchases: StateFlow<List<PurchaseEntity>> =
        repository.observePurchases().stateInVm(emptyList())

    val vendors: StateFlow<List<VendorEntity>> =
        repository.observeVendors().stateInVm(emptyList())

    val prices: StateFlow<List<PriceEntity>> =
        repository.observePrices().stateInVm(emptyList())

    val deliveries: StateFlow<List<DeliveryEntity>> =
        repository.observeDeliveries().stateInVm(emptyList())

    val payments: StateFlow<List<PaymentEntity>> =
        repository.observePayments().stateInVm(emptyList())

    val complaints: StateFlow<List<ComplaintEntity>> =
        repository.observeComplaints().stateInVm(emptyList())

    private val period = kotlinx.coroutines.flow.MutableStateFlow(ReportPeriod.ALL)
    val selectedPeriod: StateFlow<ReportPeriod> = period

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val pnl: StateFlow<PnlReport> =
        period.flatMapLatest { p -> repository.observePnl(p.from(), p.to()) }
            .stateInVm(PnlReport())

    fun setPeriod(p: ReportPeriod) { period.value = p }

    val insights: StateFlow<com.tenco.domain.SupplierInsights> =
        repository.observeInsights().stateInVm(com.tenco.domain.SupplierInsights())

    fun addPurchase(dealerId: String, quantity: Int, unitCostPaise: Long) = viewModelScope.launch {
        repository.addPurchase(dealerId, quantity, unitCostPaise)
    }

    fun addDealer(name: String, location: String) = viewModelScope.launch {
        repository.addDealer(name, location)
    }

    /** Records a sale to a vendor (creates a delivery), which raises the vendor's dues. */
    fun sellToVendor(vendorId: String, quantity: Int, unitPricePaise: Long) = viewModelScope.launch {
        repository.addDelivery(vendorId, quantity, unitPricePaise)
    }

    /** Records an in-person cash collection from a vendor (reduces their dues). */
    fun recordCashPayment(vendorId: String, amountPaise: Long) = viewModelScope.launch {
        repository.recordPayment(
            vendorId, amountPaise,
            method = com.tenco.domain.PaymentMethod.CASH,
            status = com.tenco.domain.PaymentStatus.COMPLETED,
            note = "Cash collection",
        )
    }

    fun addVendor(name: String, phone: String, upiVpa: String?) = viewModelScope.launch {
        repository.addVendor(name, phone, upiVpa, languageTag = "en")
    }

    fun setPrice(vendorId: String, unitPricePaise: Long) = viewModelScope.launch {
        repository.setPrice(vendorId, unitPricePaise)
    }

    fun resolveComplaint(complaintId: String, adjustmentPaise: Long) = viewModelScope.launch {
        repository.resolveComplaint(complaintId, adjustmentPaise)
    }

    fun setComplaintStatus(complaintId: String, status: String) = viewModelScope.launch {
        repository.setComplaintStatus(complaintId, status)
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T>.stateInVm(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)
}
