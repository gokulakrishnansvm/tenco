package com.tenco.backend.payment

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class RazorpayServiceTest {

    private val secret = "test-webhook-secret"
    private val service = RazorpayService(
        keyId = "rzp_test_dummy",
        keySecret = "dummy_secret",
        webhookSecret = secret,
        mapper = ObjectMapper(),
    )

    private fun sign(payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(payload.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `valid signature is accepted`() {
        val payload = """{"event":"payment.captured","orderId":"order_1"}"""
        assertTrue(service.isValidSignature(payload, sign(payload)))
    }

    @Test
    fun `tampered payload is rejected`() {
        val payload = """{"event":"payment.captured","orderId":"order_1"}"""
        val sig = sign(payload)
        assertFalse(service.isValidSignature(payload + "x", sig))
    }

    @Test
    fun `missing signature is rejected`() {
        assertFalse(service.isValidSignature("{}", null))
    }

    @Test
    fun `stub order id is returned for dummy keys`() {
        val order = service.createOrder(87000, "receipt123")
        assertTrue(order.startsWith("order_"))
    }
}
