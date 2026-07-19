package com.tenco.navigation

import androidx.lifecycle.ViewModel
import com.tenco.core.prefs.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Exposes the persisted UI theme mode (system/light/dark) and lets any screen change it. */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val prefs: AppPreferences,
) : ViewModel() {
    val themeMode: StateFlow<String> = prefs.themeModeFlow
    fun setThemeMode(mode: String) { prefs.themeMode = mode }
}
