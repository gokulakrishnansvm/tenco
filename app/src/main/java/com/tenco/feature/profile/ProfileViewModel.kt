package com.tenco.feature.profile

import androidx.lifecycle.ViewModel
import com.tenco.core.locale.AppLanguage
import com.tenco.core.prefs.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val prefs: AppPreferences) : ViewModel() {
    val name: String = prefs.userName?.ifBlank { "TENCO User" } ?: "TENCO User"
    val role: String = prefs.role ?: "-"
    val language: AppLanguage = prefs.language
    val isSupplier: Boolean = prefs.role == "SUPPLIER"

    val phone: String get() = prefs.userPhone ?: ""
    val upi: String get() = prefs.supplierVpa

    fun saveContact(phone: String, upi: String) {
        if (phone.isNotBlank()) prefs.userPhone = phone
        if (upi.isNotBlank()) prefs.supplierVpa = upi
    }
}
