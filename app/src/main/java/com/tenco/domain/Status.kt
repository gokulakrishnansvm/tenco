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
}

object ComplaintStatus {
    const val OPEN = "OPEN"
    const val UNDER_REVIEW = "UNDER_REVIEW"
    const val RESOLVED = "RESOLVED"
    const val REJECTED = "REJECTED"
}
