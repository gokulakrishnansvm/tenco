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
) : ViewModel() {

    init {
        // Ensure demo data exists as soon as the app starts (both roles rely on it).
        viewModelScope.launch { repository.ensureSeeded() }
        // Pull backend data for returning, authenticated users (no-op if offline / not logged in).
        viewModelScope.launch { syncManager.pull() }
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
        viewModelScope.launch { syncManager.pull() }
    }

    /**
     * Selects vendor role and binds the session to the vendor matching the logged-in phone
     * number. Falls back to the first seeded vendor for the demo when there is no match.
     */
    fun chooseVendor(onReady: (String?) -> Unit) {
        prefs.role = ROLE_VENDOR
        viewModelScope.launch {
            repository.ensureSeeded() // guarantee seed data before the one-shot reads
            syncManager.pull()        // pull backend data (best-effort) before resolving vendor
            val matched = repository.findVendorByPhone(prefs.userPhone)?.id
            val vendorId = matched ?: prefs.selectedVendorId ?: repository.firstVendor()?.id
            prefs.selectedVendorId = vendorId
            onReady(vendorId)
        }
    }

    companion object {
        const val ROLE_SUPPLIER = "SUPPLIER"
        const val ROLE_VENDOR = "VENDOR"
    }
}
