package com.tenco.backend.auth

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

@Service
class OtpService(
    @Value("\${tenco.otp.dev-mode}") private val devMode: Boolean,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private data class Challenge(val code: String, val expiresAt: Long)
    private val challenges = ConcurrentHashMap<String, Challenge>()

    /** Generates and "sends" an OTP. In dev-mode the code is returned to the caller. */
    fun request(phone: String): String? {
        val code = Random.nextInt(100000, 999999).toString()
        challenges[phone] = Challenge(code, System.currentTimeMillis() + 5 * 60_000)
        log.debug("OTP for {} = {} (dev-mode={})", phone, code, devMode)
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
