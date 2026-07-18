package com.tenco.core.prefs

import android.content.Context
import com.tenco.core.locale.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight synchronous app settings (backed by SharedPreferences).
 * Synchronous access is required for locale bootstrap in Activity.attachBaseContext.
 */
@Singleton
class AppPreferences @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("tenco_prefs", Context.MODE_PRIVATE)

    var language: AppLanguage
        get() = AppLanguage.fromTag(prefs.getString(KEY_LANG, null))
        set(value) = prefs.edit().putString(KEY_LANG, value.tag).apply()

    /** Whether the user has completed first-run language selection. */
    var languageChosen: Boolean
        get() = prefs.getBoolean(KEY_LANG_CHOSEN, false)
        set(value) = prefs.edit().putBoolean(KEY_LANG_CHOSEN, value).apply()

    /** "SUPPLIER" or "VENDOR"; null if not chosen this session. */
    var role: String?
        get() = prefs.getString(KEY_ROLE, null)
        set(value) = prefs.edit().putString(KEY_ROLE, value).apply()

    /** The vendor identity when signed in as a vendor (Phase 1 single-device). */
    var selectedVendorId: String?
        get() = prefs.getString(KEY_VENDOR_ID, null)
        set(value) = prefs.edit().putString(KEY_VENDOR_ID, value).apply()

    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userPhone: String?
        get() = prefs.getString(KEY_USER_PHONE, null)
        set(value) = prefs.edit().putString(KEY_USER_PHONE, value).apply()

    val isLoggedIn: Boolean get() = !userPhone.isNullOrBlank()

    /** Clears the session (login + role + vendor) but keeps the chosen language. */
    fun clearSession() {
        prefs.edit()
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_PHONE)
            .remove(KEY_ROLE)
            .remove(KEY_VENDOR_ID)
            .apply()
    }

    companion object {
        private const val KEY_LANG = "language_tag"
        private const val KEY_LANG_CHOSEN = "language_chosen"
        private const val KEY_ROLE = "role"
        private const val KEY_VENDOR_ID = "selected_vendor_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
    }
}
