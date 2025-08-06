package com.example.reportviolation.data.local.dao

import androidx.room.*
import com.example.reportviolation.data.model.User
import com.example.reportviolation.data.model.UserSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?
    
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber")
    suspend fun getUserByPhone(phoneNumber: String): User?
    
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber AND isActive = 1")
    suspend fun getActiveUserByPhone(phoneNumber: String): User?
    
    @Query("SELECT * FROM users WHERE registeredCity = :city")
    fun getUsersByCity(city: String): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE registeredPincode = :pincode")
    fun getUsersByPincode(pincode: String): Flow<List<User>>
    
    // Authentication and Verification
    @Query("UPDATE users SET isPhoneVerified = :verified, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updatePhoneVerification(userId: String, verified: Boolean, timestamp: LocalDateTime = LocalDateTime.now())
    
    @Query("UPDATE users SET isIdentityVerified = :verified, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateIdentityVerification(userId: String, verified: Boolean, timestamp: LocalDateTime = LocalDateTime.now())
    
    @Query("UPDATE users SET lastLoginAt = :timestamp WHERE id = :userId")
    suspend fun updateLastLogin(userId: String, timestamp: LocalDateTime = LocalDateTime.now())
    
    // Reward System
    @Query("UPDATE users SET totalPoints = totalPoints + :points, pointsEarned = pointsEarned + :points, updatedAt = :timestamp WHERE id = :userId")
    suspend fun addPoints(userId: String, points: Int, timestamp: LocalDateTime = LocalDateTime.now())
    
    @Query("UPDATE users SET totalPoints = totalPoints - :points, pointsRedeemed = pointsRedeemed + :points, updatedAt = :timestamp WHERE id = :userId")
    suspend fun redeemPoints(userId: String, points: Int, timestamp: LocalDateTime = LocalDateTime.now())
    
    @Query("UPDATE users SET reportsSubmitted = reportsSubmitted + 1, updatedAt = :timestamp WHERE id = :userId")
    suspend fun incrementReportCount(userId: String, timestamp: LocalDateTime = LocalDateTime.now())
    
    @Query("UPDATE users SET reportsApproved = reportsApproved + 1, accuracyRate = (reportsApproved * 100.0 / reportsSubmitted), updatedAt = :timestamp WHERE id = :userId")
    suspend fun incrementApprovedReportCount(userId: String, timestamp: LocalDateTime = LocalDateTime.now())
    
    // Jurisdiction Management
    @Query("UPDATE users SET authorizedCities = :cities, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateAuthorizedCities(userId: String, cities: List<String>, timestamp: LocalDateTime = LocalDateTime.now())
    
    @Query("UPDATE users SET isGuestUser = :isGuest, guestExpiryDate = :expiryDate, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateGuestStatus(userId: String, isGuest: Boolean, expiryDate: LocalDateTime?, timestamp: LocalDateTime = LocalDateTime.now())
    
    // Settings
    @Query("UPDATE users SET isAnonymousMode = :anonymous, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateAnonymousMode(userId: String, anonymous: Boolean, timestamp: LocalDateTime = LocalDateTime.now())
    
    @Query("UPDATE users SET notificationEnabled = :enabled, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateNotificationSettings(userId: String, enabled: Boolean, timestamp: LocalDateTime = LocalDateTime.now())
    
    @Query("UPDATE users SET locationSharingEnabled = :enabled, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateLocationSharingSettings(userId: String, enabled: Boolean, timestamp: LocalDateTime = LocalDateTime.now())
    
    // Statistics
    @Query("SELECT COUNT(*) FROM users WHERE registeredCity = :city")
    suspend fun getUserCountByCity(city: String): Int
    
    @Query("SELECT COUNT(*) FROM users WHERE isActive = 1")
    suspend fun getActiveUserCount(): Int
    
    // Session Management
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UserSession)
    
    @Query("SELECT * FROM user_sessions WHERE userId = :userId ORDER BY sessionStartTime DESC LIMIT 1")
    suspend fun getCurrentSession(userId: String): UserSession?
    
    @Query("UPDATE user_sessions SET currentCity = :city, currentPincode = :pincode, latitude = :lat, longitude = :lng WHERE id = :sessionId")
    suspend fun updateSessionLocation(sessionId: String, city: String?, pincode: String?, lat: Double?, lng: Double?)
    
    @Query("DELETE FROM user_sessions WHERE sessionStartTime < :cutoffDate")
    suspend fun deleteOldSessions(cutoffDate: LocalDateTime)
} 