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

    /** Pulls all records changed after [since] (epoch millis). Requires a valid JWT. */
    suspend fun syncChanges(since: Long): RemoteSyncChanges =
        client.get("$BASE_URL/api/sync/changes") {
            header("Authorization", "Bearer ${prefs.accessToken.orEmpty()}")
            url { parameters.append("since", since.toString()) }
        }.body()

    companion object {
        const val BASE_URL = "http://10.0.2.2:8080"
    }
}
