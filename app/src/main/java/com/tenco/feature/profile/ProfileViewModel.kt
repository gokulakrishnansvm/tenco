package com.tenco.feature.profile

import androidx.lifecycle.ViewModel
import com.tenco.core.locale.AppLanguage
import com.tenco.core.prefs.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(prefs: AppPreferences) : ViewModel() {
    val name: String = prefs.userName?.ifBlank { "TENCO User" } ?: "TENCO User"
    val phone: String = prefs.userPhone ?: "-"
    val role: String = prefs.role ?: "-"
    val language: AppLanguage = prefs.language
}
