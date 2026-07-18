package com.tenco.domain

/** Aggregated data for the supplier dashboard. */
data class SupplierDashboard(
    val stockOnHand: Int = 0,
    val totalEarningsPaise: Long = 0,
    val duesReceivablePaise: Long = 0,
    val lossesPaise: Long = 0,
    val vendorDistribution: List<VendorDistribution> = emptyList(),
)

data class VendorDistribution(
    val vendorId: String,
    val vendorName: String,
    val quantity: Int,
    val duesPaise: Long,
)

/** Aggregated data for the vendor dashboard. */
data class VendorDashboard(
    val vendorId: String,
    val vendorName: String,
    val supplierVpa: String? = null,
    val receivedQty: Int = 0,
    val lastUnitPricePaise: Long = 0,
    val pendingDuesPaise: Long = 0,
)

/** Supplier profit & loss for a period. */
data class PnlReport(
    val revenuePaise: Long = 0,
    val purchaseCostPaise: Long = 0,
    val complaintLossesPaise: Long = 0,
) {
    val netProfitPaise: Long get() = revenuePaise - purchaseCostPaise - complaintLossesPaise
}
