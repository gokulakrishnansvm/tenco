package com.tenco.backend.auth

import com.tenco.backend.sms.SmsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

@Service
class OtpService(
    @Value("\${tenco.otp.dev-mode}") private val devMode: Boolean,
    private val sms: SmsService,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private data class Challenge(val code: String, val expiresAt: Long)
    private val challenges = ConcurrentHashMap<String, Challenge>()

    /**
     * Generates an OTP, delivers it via the configured SMS provider, and (in dev-mode only)
     * returns the code to the caller for convenience.
     */
    fun request(phone: String): String? {
        val code = Random.nextInt(100000, 999999).toString()
        challenges[phone] = Challenge(code, System.currentTimeMillis() + 5 * 60_000)
        val message = "Your TENCO verification code is $code. Valid for 5 minutes."
        sms.send(phone10 = phone, message = message, otp = code)
        log.debug("OTP for {} generated (dev-mode={})", phone, devMode)
        return if (devMode) code else null
    }

    fun verify(phone: String, code: String): Boolean {
        val challenge = challenges[phone] ?: return false
        if (System.currentTimeMillis() > challenge.expiresAt) {
            challenges.remove(phone)
            return false
        }
        val ok = challenge.code == code
        if (ok) challenges.remove(phone)
        return ok
    }
}
