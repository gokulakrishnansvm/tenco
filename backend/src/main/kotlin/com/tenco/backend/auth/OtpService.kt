package com.tenco.backend.auth

import com.tenco.backend.sms.SmsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import kotlin.random.Random

@Service
class OtpService(
    @Value("\${tenco.otp.dev-mode}") private val devMode: Boolean,
    private val sms: SmsService,
    private val store: OtpStore,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Generates an OTP, delivers it via SMS, and (dev-mode only) returns the code.
     * Rate-limited to [MAX_REQUESTS_PER_HOUR] requests per phone per hour.
     */
    fun request(phone: String): String? {
        if (store.incrRequests(phone, HOUR_MS) > MAX_REQUESTS_PER_HOUR) {
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many OTP requests; try later")
        }
        val code = Random.nextInt(100000, 999999).toString()
        store.saveCode(phone, code, CODE_TTL_MS)
        sms.send(phone10 = phone, message = "Your TENCO verification code is $code. Valid for 5 minutes.", otp = code)
        log.debug("OTP for {} generated (dev-mode={})", phone, devMode)
        return if (devMode) code else null
    }

    /** Verifies the OTP; limited to [MAX_ATTEMPTS] attempts per phone before lockout. */
    fun verify(phone: String, code: String): Boolean {
        if (store.incrAttempts(phone) > MAX_ATTEMPTS) {
            store.clear(phone)
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many attempts; request a new OTP")
        }
        val saved = store.getCode(phone) ?: return false
        val ok = saved == code
        if (ok) store.clear(phone)
        return ok
    }

    companion object {
        private const val HOUR_MS = 60 * 60 * 1000L
        private const val CODE_TTL_MS = 5 * 60 * 1000L
        private const val MAX_REQUESTS_PER_HOUR = 5
        private const val MAX_ATTEMPTS = 5
    }
}
