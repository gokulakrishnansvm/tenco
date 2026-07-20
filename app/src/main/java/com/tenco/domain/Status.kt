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
    const val REJECTED = "REJECTED"
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
    const val CANCEL_REQUESTED = "CANCEL_REQUESTED" // vendor asked to cancel, awaiting supplier
    const val CANCELLED = "CANCELLED"

    /** Ordered pipeline used for the timeline + advancing. */
    val PIPELINE = listOf(PLACED, CONFIRMED, IN_PROGRESS, IN_TRANSIT, DELIVERED)

    fun next(status: String): String? {
        val i = PIPELINE.indexOf(status)
        return if (i in 0 until PIPELINE.lastIndex) PIPELINE[i + 1] else null
    }

    /** A vendor may request cancellation only before the order is dispatched. */
    fun cancellable(status: String): Boolean = status in listOf(PLACED, CONFIRMED, IN_PROGRESS)
}

object ComplaintStatus {
    const val OPEN = "OPEN"
    const val UNDER_REVIEW = "UNDER_REVIEW"
    const val RESOLVED = "RESOLVED"
    const val REJECTED = "REJECTED"
}


/** Tender-coconut variety colour. */
object CoconutColor {
    const val GREEN = "GREEN"
    const val RED = "RED"
    val ALL = listOf(GREEN, RED)
}

/** Coconut size grade — Tamil: perusu (big), podi (medium), sillu (small). */
object CoconutGrade {
    const val BIG = "BIG"
    const val MEDIUM = "MEDIUM"
    const val SMALL = "SMALL"
    val ALL = listOf(BIG, MEDIUM, SMALL)
}

/** Advance-payment ledger entry direction (supplier-only tracking). */
object AdvanceType {
    const val RECEIVED = "RECEIVED"   // advance taken from the vendor
    const val RETURNED = "RETURNED"   // advance given back to the vendor
}
