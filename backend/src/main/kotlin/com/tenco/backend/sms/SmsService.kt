package com.tenco.backend.sms

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Sends SMS via a configurable provider. Selected by `tenco.sms.provider`:
 *   - none   : logs the message only (dev default; no external calls)
 *   - msg91  : MSG91 Flow API (India)
 *   - twilio : Twilio Messages API
 *
 * Phone numbers are passed as bare 10-digit local numbers; providers get the country code added.
 */
@Service
class SmsService(
    @Value("\${tenco.sms.provider:none}") private val provider: String,
    @Value("\${tenco.sms.country-code:91}") private val countryCode: String,
    // MSG91
    @Value("\${tenco.sms.msg91.authkey:}") private val msg91Authkey: String,
    @Value("\${tenco.sms.msg91.sender:TENCOO}") private val msg91Sender: String,
    @Value("\${tenco.sms.msg91.template-id:}") private val msg91TemplateId: String,
    // Twilio
    @Value("\${tenco.sms.twilio.account-sid:}") private val twilioSid: String,
    @Value("\${tenco.sms.twilio.auth-token:}") private val twilioToken: String,
    @Value("\${tenco.sms.twilio.from:}") private val twilioFrom: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val http = HttpClient.newHttpClient()

    /** Returns true if the message was accepted for delivery. */
    fun send(phone10: String, message: String, otp: String): Boolean = when (provider.lowercase()) {
        "msg91" -> sendMsg91(phone10, otp, message)
        "twilio" -> sendTwilio(phone10, message)
        else -> {
            log.info("[SMS stub] to +{}{}: {}", countryCode, phone10, message)
            true
        }
    }

    private fun sendMsg91(phone10: String, otp: String, message: String): Boolean = try {
        // MSG91 Flow API: delivers a templated message; the OTP is passed as a template variable.
        val body = """{"template_id":"$msg91TemplateId","sender":"$msg91Sender","recipients":[{"mobiles":"$countryCode$phone10","otp":"$otp"}]}"""
        val req = HttpRequest.newBuilder()
            .uri(URI.create("https://control.msg91.com/api/v5/flow/"))
            .header("authkey", msg91Authkey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val resp = http.send(req, HttpResponse.BodyHandlers.ofString())
        (resp.statusCode() in 200..299).also { ok ->
            if (!ok) log.warn("MSG91 send failed: {} {}", resp.statusCode(), resp.body())
        }
    } catch (e: Exception) {
        log.warn("MSG91 error: {}", e.message); false
    }

    private fun sendTwilio(phone10: String, message: String): Boolean = try {
        val form = "To=" + enc("+$countryCode$phone10") + "&From=" + enc(twilioFrom) + "&Body=" + enc(message)
        val auth = Base64.getEncoder().encodeToString("$twilioSid:$twilioToken".toByteArray())
        val req = HttpRequest.newBuilder()
            .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/$twilioSid/Messages.json"))
            .header("Authorization", "Basic $auth")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build()
        val resp = http.send(req, HttpResponse.BodyHandlers.ofString())
        (resp.statusCode() in 200..299).also { ok ->
            if (!ok) log.warn("Twilio send failed: {} {}", resp.statusCode(), resp.body())
        }
    } catch (e: Exception) {
        log.warn("Twilio error: {}", e.message); false
    }

    private fun enc(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8)
}
