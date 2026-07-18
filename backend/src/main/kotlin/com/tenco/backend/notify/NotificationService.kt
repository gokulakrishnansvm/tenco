package com.tenco.backend.notify

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.tenco.backend.domain.DeviceTokenRepository
import com.tenco.backend.domain.Payment
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.FileInputStream

/**
 * Sends FCM push notifications. Credential-guarded: if no Firebase service-account credentials
 * are configured (TENCO_FCM_CREDENTIALS or GOOGLE_APPLICATION_CREDENTIALS), it logs instead of
 * sending, so the service runs with no Firebase project in dev.
 */
@Service
class NotificationService(
    @Value("\${tenco.fcm.credentials-path:}") private val credentialsPath: String,
    private val devices: DeviceTokenRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val enabled: Boolean by lazy { tryInit() }

    private fun tryInit(): Boolean = try {
        val credentials = when {
            credentialsPath.isNotBlank() -> GoogleCredentials.fromStream(FileInputStream(credentialsPath))
            else -> GoogleCredentials.getApplicationDefault()
        }
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(FirebaseOptions.builder().setCredentials(credentials).build())
        }
        log.info("FCM initialized")
        true
    } catch (e: Exception) {
        log.warn("FCM not configured ({}); notifications will be logged only", e.message)
        false
    }

    fun notifyPaymentCaptured(payment: Payment) {
        val tokens = devices.findAll().map { it.token }.distinct()
        val title = "Payment received"
        val body = "₹%.2f received (order %s)".format(payment.amountPaise / 100.0, payment.gatewayOrderId ?: "-")
        if (!enabled || tokens.isEmpty()) {
            log.debug("[FCM stub] {} / {} -> {} device(s)", title, body, tokens.size)
            return
        }
        tokens.forEach { token ->
            try {
                val msg = Message.builder()
                    .setToken(token)
                    .putData("title", title)
                    .putData("body", body)
                    .putData("type", "PAYMENT_CAPTURED")
                    .putData("paymentId", payment.id)
                    .build()
                FirebaseMessaging.getInstance().send(msg)
            } catch (e: Exception) {
                log.warn("FCM send failed for a token: {}", e.message)
            }
        }
    }
}
