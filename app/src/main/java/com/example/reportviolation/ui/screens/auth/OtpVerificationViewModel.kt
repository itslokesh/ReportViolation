package com.example.reportviolation.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.reportviolation.di.AppModule
import com.example.reportviolation.data.remote.auth.TokenPrefs
import com.example.reportviolation.data.remote.auth.TokenStore
import com.example.reportviolation.data.remote.auth.SessionPrefs


data class OtpVerificationUiState(
    val otp: String = "",
    val phoneNumber: String = "",
    val name: String = "",
    val email: String = "",
    val countryCode: String = "91",
    val otpError: String? = null,
    val isLoading: Boolean = false,
    val shouldNavigateToDashboard: Boolean = false,
    val canResendOtp: Boolean = false,
    val resendTimer: Int = 45
)

class OtpVerificationViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(OtpVerificationUiState())
    val uiState: StateFlow<OtpVerificationUiState> = _uiState.asStateFlow()
    
    fun initializeSignupData(name: String, email: String, phoneNumber: String, countryCode: String = "91") {
        _uiState.update { 
            it.copy(
                name = name,
                email = email,
                phoneNumber = phoneNumber,
                countryCode = countryCode
            )
        }
    }
    
    fun updateOtp(otp: String) {
        // Only allow digits and limit to 6 characters
        val filteredOtp = otp.filter { it.isDigit() }.take(6)
        
        _uiState.update { currentState ->
            currentState.copy(
                otp = filteredOtp,
                otpError = validateOtp(filteredOtp)
            )
        }
    }
    
    fun verifyOtp() {
        if (_uiState.value.otp.length != 6) return
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                val fullPhone = "+${_uiState.value.countryCode}-${_uiState.value.phoneNumber}"
                val ok = OtpNetworkBridge.verifyOtp(fullPhone, _uiState.value.otp)
                if (ok && _uiState.value.name.isNotBlank() && _uiState.value.email.isNotBlank()) {
                    // Update citizen profile with name/email using the new token
                    runCatching { OtpNetworkBridge.registerCitizenProfile(fullPhone, _uiState.value.name, _uiState.value.email) }
                }
                if (ok) {
                    // Record session start (persisted by Splash check)
                    // We cannot access context here; Splash will only read the timestamp from SharedPreferences.
                    // Use a side channel via a helper that needs context when available; for now, set epoch in a global store via a callback is out-of-scope.
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        shouldNavigateToDashboard = ok,
                        otpError = if (ok) null else "Invalid OTP. Please try again."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        otpError = "Verification failed. Please try again."
                    )
                }
            }
        }
    }
    
    fun resendOtp() {
        if (!_uiState.value.canResendOtp) return
        
        _uiState.update { 
            it.copy(
                canResendOtp = false,
                resendTimer = 45 // 45 seconds cooldown
            )
        }
        
        viewModelScope.launch {
            try {
                val fullPhone = "+${_uiState.value.countryCode}-${_uiState.value.phoneNumber}"
                com.example.reportviolation.ui.screens.auth.OtpNetworkBridge.sendOtp(fullPhone)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun decrementResendTimer() {
        _uiState.update { currentState ->
            val next = currentState.resendTimer - 1
            if (next > 0) {
                currentState.copy(resendTimer = next)
            } else {
                currentState.copy(canResendOtp = true, resendTimer = 0)
            }
        }
    }
    
    private fun validateOtp(otp: String): String? {
        return when {
            otp.isBlank() -> "OTP is required"
            otp.length != 6 -> "OTP must be 6 digits"
            !otp.matches(Regex("^[0-9]{6}$")) -> "Please enter a valid 6-digit OTP"
            else -> null
        }
    }
    
    fun resetNavigation() {
        _uiState.update { it.copy(shouldNavigateToDashboard = false) }
    }
}
