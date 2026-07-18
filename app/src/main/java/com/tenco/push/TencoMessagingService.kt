package com.tenco.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives FCM messages and new-token callbacks. Active only when Firebase is configured
 * (google-services.json present). Notification display can be added here as needed.
 */
class TencoMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // A fresh token; the app re-registers it on next login/start via PushRegistrar.
        Log.d(TAG, "onNewToken")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.data["title"] ?: message.notification?.title
        val body = message.data["body"] ?: message.notification?.body
        Log.d(TAG, "push received: $title / $body")
        // TODO: surface as a system notification / in-app inbox.
    }

    companion object {
        private const val TAG = "TencoPush"
    }
}
