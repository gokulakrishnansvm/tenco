package com.tenco.feature.loadman

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenco.data.local.OrderEntity
import com.tenco.data.local.VendorEntity
import com.tenco.data.repository.TencoRepository
import com.tenco.domain.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoadmanViewModel @Inject constructor(
    private val repository: TencoRepository,
) : ViewModel() {

    val orders: StateFlow<List<OrderEntity>> =
        repository.observeOrders().stateInVm(emptyList())

    /** All vendors (incl. archived) so delivered history still resolves names. */
    val vendors: StateFlow<List<VendorEntity>> =
        repository.observeAllVendors().stateInVm(emptyList())

    /** Marks a whole order group delivered (records a sale + depletes stock per line). */
    fun markGroupDelivered(orderIds: List<String>) = viewModelScope.launch {
        orderIds.forEach { repository.advanceOrderStatus(it, OrderStatus.DELIVERED) }
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T>.stateInVm(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)
}
