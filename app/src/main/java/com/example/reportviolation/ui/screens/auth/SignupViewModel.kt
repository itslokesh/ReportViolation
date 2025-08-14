package com.example.reportviolation.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Patterns
import com.example.reportviolation.di.AppModule
import com.example.reportviolation.data.model.Country
import com.example.reportviolation.domain.service.CountryService

data class SignupUiState(
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val selectedCountry: Country = CountryService.getCountryByCode("IN") ?: Country("India", "IN", "91"),
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val isLoading: Boolean = false,
    val shouldNavigateToOtp: Boolean = false
) {
    val isFormValid: Boolean
        get() = name.isNotBlank() && 
                email.isNotBlank() && 
                phoneNumber.isNotBlank() &&
                nameError == null &&
                emailError == null &&
                phoneError == null
}

class SignupViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()
    
    // TODO: Inject this service properly using dependency injection
    // For now, we'll simulate the registration process
    
    fun updateName(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                name = name,
                nameError = validateName(name)
            )
        }
    }
    
    fun updateEmail(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = email,
                emailError = validateEmail(email)
            )
        }
    }
    
    fun updatePhoneNumber(phoneNumber: String) {
        // Only allow digits and limit to the selected country's phone number length
        val filteredPhone = phoneNumber.filter { it.isDigit() }.take(_uiState.value.selectedCountry.phoneNumberLength)
        
        _uiState.update { currentState ->
            currentState.copy(
                phoneNumber = filteredPhone,
                phoneError = validatePhoneNumber(filteredPhone, currentState.selectedCountry)
            )
        }
    }
    
    fun updateSelectedCountry(country: Country) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCountry = country,
                phoneError = validatePhoneNumber(currentState.phoneNumber, country)
            )
        }
    }
    
    fun sendOtp() {
        if (!_uiState.value.isFormValid) return
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                // In a real app, this would send OTP to the phone number
                // For now, we'll simulate the process
                kotlinx.coroutines.delay(1500) // Simulate network delay
                
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        shouldNavigateToOtp = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false
                    )
                }
            }
        }
    }
    
    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name is required"
            name.length < 2 -> "Name must be at least 2 characters"
            name.length > 50 -> "Name must be less than 50 characters"
            !name.matches(Regex("^[a-zA-Z\\s]+$")) -> "Name can only contain letters and spaces"
            else -> null
        }
    }
    
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Please enter a valid email address"
            else -> null
        }
    }
    
    private fun validatePhoneNumber(phoneNumber: String, country: Country): String? {
        return when {
            phoneNumber.isBlank() -> "Phone number is required"
            !phoneNumber.matches(Regex("^[0-9]+$")) -> "Please enter a valid phone number"
            phoneNumber.length != country.phoneNumberLength -> "Phone number must be ${country.phoneNumberLength} digits for ${country.name}"
            else -> null
        }
    }
    
    fun resetNavigation() {
        _uiState.update { it.copy(shouldNavigateToOtp = false) }
    }
}
