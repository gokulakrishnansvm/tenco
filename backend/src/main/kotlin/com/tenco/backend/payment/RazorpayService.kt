package com.tenco.backend.payment

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class RazorpayService(
    @Value("\${tenco.razorpay.key-id}") private val keyId: String,
    @Value("\${tenco.razorpay.webhook-secret}") private val webhookSecret: String,
) {
    /**
     * Creates a payment order. In production this calls the Razorpay Orders API
     * (POST https://api.razorpay.com/v1/orders) with Basic auth (keyId:keySecret).
     * Here it returns a deterministic stub id so the flow is testable without live keys.
     */
    fun createOrder(amountPaise: Long, receipt: String): String {
        // TODO(Phase 3): replace with real Razorpay Orders API call.
        return "order_" + receipt.take(8) + "_" + amountPaise
    }

    /** Builds a UPI deep link for the intent (fallback / display alongside the gateway order). */
    fun upiDeepLink(payeeVpa: String, payeeName: String, amountPaise: Long, note: String): String {
        val amt = String.format("%.2f", amountPaise / 100.0)
        return "upi://pay?pa=$payeeVpa&pn=$payeeName&am=$amt&cu=INR&tn=$note"
    }

    /** Verifies the Razorpay webhook signature: HMAC-SHA256(payload, webhookSecret) == signature. */
    fun isValidSignature(payload: String, signature: String?): Boolean {
        if (signature.isNullOrBlank()) return false
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(webhookSecret.toByteArray(), "HmacSHA256"))
        val hex = mac.doFinal(payload.toByteArray()).joinToString("") { "%02x".format(it) }
        return constantTimeEquals(hex, signature)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) result = result or (a[i].code xor b[i].code)
        return result == 0
    }
}
