package com.example.reportviolation.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    
    // Basic Information
    val phoneNumber: String,
    val name: String?,
    val email: String?,
    
    // Location and Jurisdiction
    val registeredCity: String,
    val registeredPincode: String,
    val registeredDistrict: String,
    val registeredState: String,
    
    // Verification
    val isPhoneVerified: Boolean = false,
    val isIdentityVerified: Boolean = false,
    val aadhaarNumber: String? = null,
    val panNumber: String? = null,
    
    // Reward System
    val totalPoints: Int = 0,
    val pointsEarned: Int = 0,
    val pointsRedeemed: Int = 0,
    val reportsSubmitted: Int = 0,
    val reportsApproved: Int = 0,
    val accuracyRate: Double = 0.0,
    
    // Settings and Preferences
    val isAnonymousMode: Boolean = false,
    val notificationEnabled: Boolean = true,
    val locationSharingEnabled: Boolean = true,
    
    // Multi-city Access
    val authorizedCities: List<String> = emptyList(),
    val isGuestUser: Boolean = false,
    val guestExpiryDate: LocalDateTime? = null,
    
    // Metadata
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val lastLoginAt: LocalDateTime? = null,
    val isActive: Boolean = true
)

@Entity(tableName = "user_sessions")
data class UserSession(
    @PrimaryKey
    val id: String,
    val userId: String,
    val currentCity: String?,
    val currentPincode: String?,
    val latitude: Double?,
    val longitude: Double?,
    val sessionStartTime: LocalDateTime = LocalDateTime.now(),
    val isGuestMode: Boolean = false
) 