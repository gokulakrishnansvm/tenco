package com.tenco.backend.payment

import com.fasterxml.jackson.databind.ObjectMapper
import com.tenco.backend.domain.Payment
import com.tenco.backend.domain.PaymentRepository
import com.tenco.backend.domain.now
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class IntentBody(val vendorId: String, val amountPaise: Long)
data class IntentResponse(
    val intentId: String,
    val orderId: String,
    val amountPaise: Long,
    val upiDeepLink: String,
)

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val payments: PaymentRepository,
    private val razorpay: RazorpayService,
) {
    /** Creates a payment intent: persists a PENDING payment + gateway order, returns pay info. */
    @PostMapping("/intent")
    fun createIntent(@RequestBody body: IntentBody): IntentResponse {
        val payment = payments.save(
            Payment(vendorId = body.vendorId, amountPaise = body.amountPaise, status = "PENDING")
        )
        val orderId = razorpay.createOrder(body.amountPaise, payment.id)
        payment.gatewayOrderId = orderId
        payment.status = "PENDING_VERIFICATION"
        payment.updatedAt = now()
        payments.save(payment)
        val link = razorpay.upiDeepLink("tenco.supplier@upi", "TENCO Supplier", body.amountPaise, "TENCO dues")
        return IntentResponse(payment.id, orderId, body.amountPaise, link)
    }
}

/**
 * Server-to-server webhook from the payment gateway. The signature is verified with the
 * shared webhook secret; the client is never trusted for payment success.
 *
 * Expected (simplified) JSON body:
 *   { "event": "payment.captured" | "payment.failed", "orderId": "...", "upiRef": "..." }
 */
@RestController
@RequestMapping("/webhooks")
class WebhookController(
    private val payments: PaymentRepository,
    private val razorpay: RazorpayService,
    private val mapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/pg")
    fun handle(
        @RequestBody rawBody: String,
        @RequestHeader("X-Razorpay-Signature", required = false) signature: String?,
    ): ResponseEntity<String> {
        if (!razorpay.isValidSignature(rawBody, signature)) {
            log.warn("Rejected webhook with invalid signature")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid signature")
        }
        val node = mapper.readTree(rawBody)
        val event = node.path("event").asText("")
        val orderId = node.path("orderId").asText("")
        val payment = payments.findByGatewayOrderId(orderId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("unknown order")

        payment.status = when (event) {
            "payment.captured" -> "COMPLETED"
            "payment.failed" -> "FAILED"
            else -> payment.status
        }
        payment.upiRef = node.path("upiRef").asText(null)
        payment.updatedAt = now()
        payments.save(payment)
        log.debug("Webhook {} applied to order {} -> {}", event, orderId, payment.status)
        return ResponseEntity.ok("ok")
    }
}
