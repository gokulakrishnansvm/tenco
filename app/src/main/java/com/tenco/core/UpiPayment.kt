package com.tenco.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Builds and launches a UPI payment deep link (`upi://pay?...`).
 *
 * NOTE (Phase 1): Android/UPI apps do not reliably return a success/failure result to the
 * caller, so the caller must ask the user to confirm the outcome after returning. Automatic
 * confirmation requires a payment gateway with server-side webhooks (Phase 3).
 */
object UpiPayment {

    fun buildUri(payeeVpa: String, payeeName: String, amountRupees: Double, note: String): Uri =
        Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", payeeVpa)
            .appendQueryParameter("pn", payeeName)
            .appendQueryParameter("am", String.format("%.2f", amountRupees))
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", note)
            .build()

    /** Returns true if a UPI app was launched, false if none is available. */
    fun launch(context: Context, payeeVpa: String, payeeName: String, amountRupees: Double, note: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, buildUri(payeeVpa, payeeName, amountRupees, note))
        // createChooser never throws ActivityNotFoundException even when no app can handle the
        // intent, so explicitly check for a handler first.
        if (intent.resolveActivity(context.packageManager) == null) return false
        return launchChooser(context, intent)
    }

    /** Launches an explicit UPI deep link string (e.g. one returned by the backend intent). */
    fun launchLink(context: Context, upiLink: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiLink))
        if (intent.resolveActivity(context.packageManager) == null) return false
        return launchChooser(context, intent)
    }

    private fun launchChooser(context: Context, intent: Intent): Boolean {
        val chooser = Intent.createChooser(intent, "Pay with")
        return try {
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
