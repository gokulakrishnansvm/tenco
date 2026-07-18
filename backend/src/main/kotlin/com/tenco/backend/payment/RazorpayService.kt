package com.tenco.backend.payment

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class RazorpayService(
    @Value("\${tenco.razorpay.key-id}") private val keyId: String,
    @Value("\${tenco.razorpay.key-secret}") private val keySecret: String,
    @Value("\${tenco.razorpay.webhook-secret}") private val webhookSecret: String,
    private val mapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val http = HttpClient.newHttpClient()

    private val useRealApi: Boolean
        get() = !keyId.contains("dummy") && !keySecret.contains("dummy")

    /**
     * Creates a payment order. With real keys this calls the Razorpay Orders API
     * (POST https://api.razorpay.com/v1/orders, Basic auth). Without real keys (dev), it
     * returns a deterministic stub id so the flow is testable offline.
     */
    fun createOrder(amountPaise: Long, receipt: String): String {
        if (!useRealApi) return "order_" + receipt.take(8) + "_" + amountPaise
        return try {
            val body = mapper.writeValueAsString(
                mapOf("amount" to amountPaise, "currency" to "INR", "receipt" to receipt)
            )
            val auth = Base64.getEncoder().encodeToString("$keyId:$keySecret".toByteArray())
            val req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.razorpay.com/v1/orders"))
                .header("Authorization", "Basic $auth")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build()
            val resp = http.send(req, HttpResponse.BodyHandlers.ofString())
            if (resp.statusCode() in 200..299) {
                mapper.readTree(resp.body()).path("id").asText()
            } else {
                log.warn("Razorpay order create failed: {} {}", resp.statusCode(), resp.body())
                "order_fallback_$receipt"
            }
        } catch (e: Exception) {
            log.warn("Razorpay order create error: {}", e.message)
            "order_fallback_$receipt"
        }
    }

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
