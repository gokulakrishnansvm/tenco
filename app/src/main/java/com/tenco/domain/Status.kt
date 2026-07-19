package com.tenco.domain

/** Status string constants (stored in Room as text). */
object DeliveryStatus {
    const val DELIVERED = "DELIVERED"
    const val CONFIRMED = "CONFIRMED"
}

object PaymentStatus {
    const val PENDING = "PENDING"
    const val PENDING_VERIFICATION = "PENDING_VERIFICATION"
    const val COMPLETED = "COMPLETED"
    const val FAILED = "FAILED"
}

object PaymentMethod {
    const val UPI = "UPI"
    const val CASH = "CASH"
    const val ORDER = "ORDER"
}

/** Vendor order fulfilment lifecycle (in progression order). */
object OrderStatus {
    const val PLACED = "PLACED"          // vendor placed, awaiting supplier
    const val CONFIRMED = "CONFIRMED"    // supplier set price / accepted
    const val IN_PROGRESS = "IN_PROGRESS" // being prepared
    const val IN_TRANSIT = "IN_TRANSIT"  // out for delivery
    const val DELIVERED = "DELIVERED"
    const val CANCELLED = "CANCELLED"

    /** Ordered pipeline used for the timeline + advancing. */
    val PIPELINE = listOf(PLACED, CONFIRMED, IN_PROGRESS, IN_TRANSIT, DELIVERED)

    fun next(status: String): String? {
        val i = PIPELINE.indexOf(status)
        return if (i in 0 until PIPELINE.lastIndex) PIPELINE[i + 1] else null
    }
}

object ComplaintStatus {
    const val OPEN = "OPEN"
    const val UNDER_REVIEW = "UNDER_REVIEW"
    const val RESOLVED = "RESOLVED"
    const val REJECTED = "REJECTED"
}
