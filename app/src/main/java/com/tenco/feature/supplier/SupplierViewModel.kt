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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplierViewModel @Inject constructor(
    private val repository: TencoRepository,
) : ViewModel() {

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

    val pnl: StateFlow<PnlReport> =
        repository.observePnl().stateInVm(PnlReport())

    fun addPurchase(dealerId: String, quantity: Int, unitCostPaise: Long) = viewModelScope.launch {
        repository.addPurchase(dealerId, quantity, unitCostPaise)
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

    private fun <T> kotlinx.coroutines.flow.Flow<T>.stateInVm(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)
}
