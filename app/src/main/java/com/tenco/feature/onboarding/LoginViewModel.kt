package com.tenco.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenco.core.prefs.AppPreferences
import com.tenco.data.remote.TencoApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoginPhase { PHONE, OTP }

data class LoginUiState(
    val phase: LoginPhase = LoginPhase.PHONE,
    val loading: Boolean = false,
    val error: String? = null,
    val devOtp: String? = null,   // dev convenience: prefill the code returned by the backend
    val offline: Boolean = false, // backend unreachable — logged in locally
    val done: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: TencoApi,
    private val prefs: AppPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private var pendingName: String = ""
    private var pendingPhone: String = ""

    /** Requests an OTP from the backend. Falls back to local (offline) login if unreachable. */
    fun sendOtp(name: String, phone: String) {
        pendingName = name
        pendingPhone = phone
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val resp = api.requestOtp(phone)
                _state.update { it.copy(loading = false, phase = LoginPhase.OTP, devOtp = resp.devOtp) }
            } catch (e: Exception) {
                // Backend not reachable → keep the local-first experience working.
                loginLocally(name, phone)
                _state.update { it.copy(loading = false, offline = true, done = true) }
            }
        }
    }

    /** Verifies the OTP with the backend and stores the JWT session. */
    fun verify(code: String) {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val auth = api.verifyOtp(pendingPhone, code, pendingName, role = null)
                prefs.userName = pendingName
                prefs.userPhone = pendingPhone
                prefs.userId = auth.userId
                prefs.accessToken = auth.accessToken
                prefs.refreshToken = auth.refreshToken
                _state.update { it.copy(loading = false, done = true) }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = "Invalid or expired OTP") }
            }
        }
    }

    private fun loginLocally(name: String, phone: String) {
        prefs.userName = name
        prefs.userPhone = phone
    }
}
