package com.tenco.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.tenco.core.prefs.AppPreferences
import com.tenco.data.remote.TencoApi
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Fetches the current FCM token and registers it with the backend. Guarded: if Firebase is not
 * configured (no google-services.json), token retrieval fails and we log instead of crashing.
 */
@Singleton
class PushRegistrar @Inject constructor(
    private val api: TencoApi,
    private val prefs: AppPreferences,
) {
    suspend fun registerCurrentToken() {
        val userId = prefs.userId ?: return
        try {
            val token = fetchToken()
            api.registerDevice(userId, token)
            Log.d(TAG, "registered FCM token with backend")
        } catch (e: Exception) {
            Log.w(TAG, "FCM token registration skipped: ${e.message}")
        }
    }

    private suspend fun fetchToken(): String = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    companion object {
        private const val TAG = "TencoPush"
    }
}
