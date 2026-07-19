package com.tenco.data.remote

import com.tenco.core.prefs.AppPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backend API client (Phase 3). Base URL uses the Android emulator loopback alias 10.0.2.2
 * which maps to the host machine's localhost where the backend runs on :8080.
 */
@Singleton
class TencoApi @Inject constructor(
    private val client: HttpClient,
    private val prefs: AppPreferences,
) {
    suspend fun requestOtp(phone: String): OtpRequestResponse =
        client.post("$BASE_URL/auth/otp/request") {
            contentType(ContentType.Application.Json)
            setBody(OtpRequestBody(phone))
        }.body()

    suspend fun verifyOtp(phone: String, code: String, name: String?, role: String?): AuthResponse =
        client.post("$BASE_URL/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody(OtpVerifyBody(phone, code, name, role))
        }.body()

    /** Updates the authenticated user's role and returns re-issued tokens. */
    suspend fun setRole(role: String): AuthResponse =
        client.post("$BASE_URL/auth/role") {
            header("Authorization", "Bearer ${prefs.accessToken.orEmpty()}")
            contentType(ContentType.Application.Json)
            setBody(RoleBody(role))
        }.body()

    /** Pulls all records changed after [since] (epoch millis). Requires a valid JWT. */
    suspend fun syncChanges(since: Long): RemoteSyncChanges =
        client.get("$BASE_URL/api/sync/changes") {
            header("Authorization", "Bearer ${prefs.accessToken.orEmpty()}")
            url { parameters.append("since", since.toString()) }
        }.body()

    /** Pushes locally-changed records (outbox) to the backend. Requires a valid JWT. */
    suspend fun pushChanges(changes: RemoteSyncChanges) {
        client.post("$BASE_URL/api/sync/push") {
            header("Authorization", "Bearer ${prefs.accessToken.orEmpty()}")
            contentType(ContentType.Application.Json)
            setBody(changes)
        }
    }

    /** Creates a backend payment intent (Razorpay order + UPI link). Requires a valid JWT. */
    suspend fun createPaymentIntent(vendorId: String, amountPaise: Long): IntentResponse =
        client.post("$BASE_URL/api/payments/intent") {
            header("Authorization", "Bearer ${prefs.accessToken.orEmpty()}")
            contentType(ContentType.Application.Json)
            setBody(IntentBody(vendorId, amountPaise))
        }.body()

    /** Registers this device's FCM token for push notifications. Requires a valid JWT. */
    suspend fun registerDevice(userId: String, fcmToken: String) {
        client.post("$BASE_URL/api/devices/register") {
            header("Authorization", "Bearer ${prefs.accessToken.orEmpty()}")
            contentType(ContentType.Application.Json)
            setBody(DeviceRegisterBody(userId, fcmToken))
        }
    }

    companion object {
        const val BASE_URL = "http://10.0.2.2:8080"
    }
}
