package com.tenco.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenco.core.locale.AppLanguage
import com.tenco.core.prefs.AppPreferences
import com.tenco.data.repository.TencoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val prefs: AppPreferences,
    private val repository: TencoRepository,
    private val syncManager: com.tenco.data.sync.SyncManager,
    private val pushRegistrar: com.tenco.push.PushRegistrar,
    private val api: com.tenco.data.remote.TencoApi,
) : ViewModel() {

    init {
        // Ensure demo data exists as soon as the app starts (both roles rely on it).
        viewModelScope.launch { repository.ensureSeeded() }
        // Push queued local changes + pull backend data (no-op if offline / not logged in).
        viewModelScope.launch { syncManager.sync() }
    }

    val startRoute: String
        get() = when {
            !prefs.languageChosen -> Routes.LANGUAGE
            !prefs.isLoggedIn -> Routes.LOGIN
            prefs.role == ROLE_SUPPLIER -> Routes.SUPPLIER_HOME
            prefs.role == ROLE_VENDOR -> Routes.VENDOR_HOME
            else -> Routes.ROLE
        }

    val currentLanguage: AppLanguage get() = prefs.language
    val currentVendorId: String? get() = prefs.selectedVendorId
    val isLanguageChosen: Boolean get() = prefs.languageChosen

    fun login(name: String, phone: String) {
        prefs.userName = name
        prefs.userPhone = phone
    }

    fun logout() = prefs.clearSession()

    fun setLanguage(language: AppLanguage) {
        prefs.language = language
        prefs.languageChosen = true
    }

    fun chooseSupplier() {
        prefs.role = ROLE_SUPPLIER
        viewModelScope.launch { applyRoleRemote(ROLE_SUPPLIER); syncManager.sync() }
        viewModelScope.launch { pushRegistrar.registerCurrentToken() }
    }

    /**
     * Selects vendor role and binds the session to the vendor matching the logged-in phone
     * number. Falls back to the first seeded vendor for the demo when there is no match.
     */
    fun chooseVendor(onReady: (String?) -> Unit) {
        prefs.role = ROLE_VENDOR
        viewModelScope.launch { pushRegistrar.registerCurrentToken() }
        viewModelScope.launch {
            repository.ensureSeeded() // guarantee seed data before the one-shot reads
            applyRoleRemote(ROLE_VENDOR) // align backend JWT role for RBAC (best-effort)
            syncManager.sync()           // push local changes + pull backend data (best-effort)
            val matched = repository.findVendorByPhone(prefs.userPhone)?.id
            val vendorId = matched ?: prefs.selectedVendorId ?: repository.firstVendor()?.id
            prefs.selectedVendorId = vendorId
            onReady(vendorId)
        }
    }

    /** Re-issues the JWT with the chosen role so RBAC-protected endpoints accept it. */
    private suspend fun applyRoleRemote(role: String) {
        if (prefs.accessToken.isNullOrBlank()) return
        try {
            val auth = api.setRole(role)
            prefs.accessToken = auth.accessToken
            prefs.refreshToken = auth.refreshToken
            prefs.userId = auth.userId
        } catch (e: Exception) {
            // Offline / backend unavailable — local role still drives the UI.
        }
    }

    companion object {
        const val ROLE_SUPPLIER = "SUPPLIER"
        const val ROLE_VENDOR = "VENDOR"
    }
}
