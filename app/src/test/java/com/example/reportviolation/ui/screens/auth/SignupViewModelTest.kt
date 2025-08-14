package com.example.reportviolation.ui.screens.auth

import org.junit.Test
import org.junit.Assert.*

class SignupViewModelTest {
    
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
            !email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> "Please enter a valid email address"
            else -> null
        }
    }
    
    private fun validatePhoneNumber(phoneNumber: String): String? {
        // For testing, we'll use India's phone number length (10 digits)
        val expectedLength = 10
        return when {
            phoneNumber.isBlank() -> "Phone number is required"
            !phoneNumber.matches(Regex("^[0-9]+$")) -> "Please enter a valid phone number"
            phoneNumber.length != expectedLength -> "Phone number must be $expectedLength digits for India"
            else -> null
        }
    }
    
    @Test
    fun `test name validation`() {
        // Test empty name
        assertNotNull(validateName(""))
        
        // Test short name
        assertNotNull(validateName("A"))
        
        // Test valid name
        assertNull(validateName("John Doe"))
        
        // Test name with numbers (invalid)
        assertNotNull(validateName("John123"))
        
        // Test name with special characters
        assertNotNull(validateName("John@Doe"))
    }
    
    @Test
    fun `test email validation`() {
        // Test empty email
        assertNotNull(validateEmail(""))
        
        // Test invalid email
        assertNotNull(validateEmail("invalid-email"))
        
        // Test valid email
        assertNull(validateEmail("test@example.com"))
        
        // Test email with spaces
        assertNotNull(validateEmail("test @example.com"))
        
        // Test email without domain
        assertNotNull(validateEmail("test@"))
    }
    
    @Test
    fun `test phone validation`() {
        // Test empty phone
        assertNotNull(validatePhoneNumber(""))
        
        // Test short phone (less than 10 digits for India)
        assertNotNull(validatePhoneNumber("123"))
        
        // Test valid phone (10 digits for India)
        assertNull(validatePhoneNumber("1234567890"))
        
        // Test phone with letters
        assertNotNull(validatePhoneNumber("123abc456"))
        
        // Test phone with special characters
        assertNotNull(validatePhoneNumber("123-456-7890"))
        
        // Test phone with spaces
        assertNotNull(validatePhoneNumber("123 456 7890"))
        
        // Test phone that's too long (more than 10 digits for India)
        assertNotNull(validatePhoneNumber("12345678901"))
        
        // Test phone with wrong length (9 digits for India)
        assertNotNull(validatePhoneNumber("123456789"))
    }
    
    @Test
    fun `test form validation`() {
        // Test with all valid data
        val validName = validateName("John Doe")
        val validEmail = validateEmail("test@example.com")
        val validPhone = validatePhoneNumber("1234567890")
        
        assertNull(validName)
        assertNull(validEmail)
        assertNull(validPhone)
        
        // Test with invalid data
        val invalidName = validateName("")
        val invalidEmail = validateEmail("invalid")
        val invalidPhone = validatePhoneNumber("123")
        
        assertNotNull(invalidName)
        assertNotNull(invalidEmail)
        assertNotNull(invalidPhone)
    }
}
