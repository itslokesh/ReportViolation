package com.example.reportviolation.domain.service

import android.content.Context
import android.content.SharedPreferences
import com.example.reportviolation.data.model.User
import com.example.reportviolation.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRegistrationService(
    private val context: Context,
    private val userRepository: UserRepository,
    private val languageManager: LanguageManager
) {
    
    companion object {
        private const val PREFS_NAME = "UserPrefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    suspend fun registerUser(name: String, email: String, phoneNumber: String, countryCode: String = "91"): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // Check if user already exists
                val existingUser = userRepository.getUserByPhone(phoneNumber)
                if (existingUser != null) {
                    return@withContext Result.failure(Exception("User with this phone number already exists"))
                }
                
                // Create new user with full phone number including country code
                val fullPhoneNumber = "+$countryCode$phoneNumber"
                val user = User(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    email = email,
                    phoneNumber = fullPhoneNumber,
                    registeredCity = "",
                    registeredPincode = "",
                    registeredDistrict = "",
                    registeredState = "",
                    isActive = true
                )
                
                userRepository.insertUser(user)
                
                // Set default language to English
                languageManager.setLanguage(LanguageManager.LANGUAGE_ENGLISH)
                
                // Mark user as logged in
                setUserLoggedIn(user.id)
                
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun verifyOtpAndCompleteRegistration(otp: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // For demo purposes, accept any 6-digit OTP
                // In real app, this would verify against the actual OTP
                if (otp.length == 6 && otp.matches(Regex("^[0-9]{6}$"))) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Invalid OTP"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private fun setUserLoggedIn(userId: String) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, userId)
            .apply()
    }
    
    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun getCurrentUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }
    
    fun logout() {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putString(KEY_USER_ID, null)
            .apply()
    }
}
