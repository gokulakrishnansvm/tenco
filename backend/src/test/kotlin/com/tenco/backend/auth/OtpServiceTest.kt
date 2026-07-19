package com.tenco.backend.auth

import com.tenco.backend.sms.SmsService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException

class OtpServiceTest {

    private fun newService() = OtpService(
        devMode = true,
        sms = SmsService("none", "91", "", "TENCOO", "", "", "", ""),
        store = InMemoryOtpStore(),
    )

    @Test
    fun `request returns code in dev mode and verify succeeds`() {
        val svc = newService()
        val code = svc.request("9876543210")
        assertNotNull(code)
        assertTrue(svc.verify("9876543210", code!!))
    }

    @Test
    fun `wrong code fails`() {
        val svc = newService()
        svc.request("9876543210")
        assertFalse(svc.verify("9876543210", "000000"))
    }

    @Test
    fun `code is single use`() {
        val svc = newService()
        val code = svc.request("9876543210")!!
        assertTrue(svc.verify("9876543210", code))
        assertFalse(svc.verify("9876543210", code))
    }

    @Test
    fun `request is rate limited after 5 per hour`() {
        val svc = newService()
        repeat(5) { svc.request("9990001111") }
        assertThrows(ResponseStatusException::class.java) { svc.request("9990001111") }
    }
}
