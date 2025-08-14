package com.example.reportviolation.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.reportviolation.di.AppModule

data class OtpVerificationUiState(
    val otp: String = "",
    val phoneNumber: String = "",
    val name: String = "",
    val email: String = "",
    val countryCode: String = "91",
    val otpError: String? = null,
    val isLoading: Boolean = false,
    val shouldNavigateToDashboard: Boolean = false,
    val canResendOtp: Boolean = true,
    val resendTimer: Int = 0
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
                // In a real app, this would verify the OTP with the server
                // For now, we'll simulate the verification process
                kotlinx.coroutines.delay(2000) // Simulate network delay
                
                if (_uiState.value.otp.length == 6) {
                    try {
                        // Register the user
                        val userRegistrationService = AppModule.getUserRegistrationService()
                        val result = userRegistrationService.registerUser(
                            _uiState.value.name,
                            _uiState.value.email,
                            _uiState.value.phoneNumber,
                            _uiState.value.countryCode
                        )
                        
                        if (result.isSuccess) {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    shouldNavigateToDashboard = true
                                )
                            }
                        } else {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    otpError = result.exceptionOrNull()?.message ?: "Registration failed"
                                )
                            }
                        }
                    } catch (e: Exception) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                otpError = "Registration failed: ${e.message}"
                            )
                        }
                    }
                } else {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            otpError = "Invalid OTP. Please try again."
                        )
                    }
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
                resendTimer = 30 // 30 seconds cooldown
            )
        }
        
        viewModelScope.launch {
            try {
                // Simulate sending OTP
                kotlinx.coroutines.delay(1000) // Simulate network delay
                // In real app, this would trigger OTP sending
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun decrementResendTimer() {
        _uiState.update { currentState ->
            if (currentState.resendTimer > 0) {
                currentState.copy(
                    resendTimer = currentState.resendTimer - 1
                )
            } else {
                currentState.copy(
                    canResendOtp = true,
                    resendTimer = 0
                )
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
