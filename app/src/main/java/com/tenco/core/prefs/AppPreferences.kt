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

    /** The supplier's own UPI VPA that vendors pay into. */
    var supplierVpa: String
        get() = prefs.getString(KEY_SUPPLIER_VPA, "tenco.supplier@upi") ?: "tenco.supplier@upi"
        set(value) = prefs.edit().putString(KEY_SUPPLIER_VPA, value).apply()

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    /** Delta-sync cursor (epoch millis) of the last successful pull. */
    var lastSyncCursor: Long
        get() = prefs.getLong(KEY_SYNC_CURSOR, 0L)
        set(value) = prefs.edit().putLong(KEY_SYNC_CURSOR, value).apply()

    private val _themeMode = kotlinx.coroutines.flow.MutableStateFlow(
        prefs.getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM,
    )

    /** Observable UI theme mode: "system" | "light" | "dark". */
    val themeModeFlow: kotlinx.coroutines.flow.StateFlow<String> = _themeMode

    var themeMode: String
        get() = _themeMode.value
        set(value) {
            prefs.edit().putString(KEY_THEME, value).apply()
            _themeMode.value = value
        }

    private val _usage = kotlinx.coroutines.flow.MutableStateFlow(loadUsage())

    /** Observable per-action usage counts, used to order quick actions by most-used. */
    val actionUsageFlow: kotlinx.coroutines.flow.StateFlow<Map<String, Int>> = _usage

    fun incrementActionUsage(key: String) {
        val m = _usage.value.toMutableMap()
        m[key] = (m[key] ?: 0) + 1
        prefs.edit().putString(KEY_USAGE, m.entries.joinToString(",") { "${it.key}:${it.value}" }).apply()
        _usage.value = m
    }

    private fun loadUsage(): Map<String, Int> =
        (prefs.getString(KEY_USAGE, "") ?: "").split(",").mapNotNull {
            val parts = it.split(":"); if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: 0) else null
        }.toMap()

    val isLoggedIn: Boolean get() = !userPhone.isNullOrBlank()

    /** Clears the session (login + tokens + role + vendor) but keeps the chosen language. */
    fun clearSession() {
        prefs.edit()
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_PHONE)
            .remove(KEY_ROLE)
            .remove(KEY_VENDOR_ID)
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .apply()
    }

    companion object {
        private const val KEY_LANG = "language_tag"
        private const val KEY_LANG_CHOSEN = "language_chosen"
        private const val KEY_ROLE = "role"
        private const val KEY_VENDOR_ID = "selected_vendor_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_SUPPLIER_VPA = "supplier_vpa"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_SYNC_CURSOR = "last_sync_cursor"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_USAGE = "action_usage"
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }
}
