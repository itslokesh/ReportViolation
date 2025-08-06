package com.example.reportviolation.data.repository

import com.example.reportviolation.data.local.dao.UserDao
import com.example.reportviolation.data.model.User
import com.example.reportviolation.data.model.UserSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class UserRepository(
    private val userDao: UserDao
) {
    
    // User CRUD operations
    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }
    
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
    
    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }
    
    suspend fun getUserByPhone(phoneNumber: String): User? {
        return userDao.getUserByPhone(phoneNumber)
    }
    
    suspend fun getActiveUserByPhone(phoneNumber: String): User? {
        return userDao.getActiveUserByPhone(phoneNumber)
    }
    
    // Authentication
    suspend fun verifyPhoneNumber(userId: String, verified: Boolean) {
        userDao.updatePhoneVerification(userId, verified)
    }
    
    suspend fun verifyIdentity(userId: String, verified: Boolean) {
        userDao.updateIdentityVerification(userId, verified)
    }
    
    suspend fun updateLastLogin(userId: String) {
        userDao.updateLastLogin(userId, LocalDateTime.now())
    }
    
    // Reward System
    suspend fun addPoints(userId: String, points: Int) {
        userDao.addPoints(userId, points)
    }
    
    suspend fun redeemPoints(userId: String, points: Int) {
        userDao.redeemPoints(userId, points)
    }
    
    suspend fun incrementReportCount(userId: String) {
        userDao.incrementReportCount(userId)
    }
    
    suspend fun incrementApprovedReportCount(userId: String) {
        userDao.incrementApprovedReportCount(userId)
    }
    
    // Jurisdiction Management
    suspend fun updateAuthorizedCities(userId: String, cities: List<String>) {
        userDao.updateAuthorizedCities(userId, cities)
    }
    
    suspend fun updateGuestStatus(userId: String, isGuest: Boolean, expiryDate: LocalDateTime?) {
        userDao.updateGuestStatus(userId, isGuest, expiryDate)
    }
    
    // Settings
    suspend fun updateAnonymousMode(userId: String, anonymous: Boolean) {
        userDao.updateAnonymousMode(userId, anonymous)
    }
    
    suspend fun updateNotificationSettings(userId: String, enabled: Boolean) {
        userDao.updateNotificationSettings(userId, enabled)
    }
    
    suspend fun updateLocationSharingSettings(userId: String, enabled: Boolean) {
        userDao.updateLocationSharingSettings(userId, enabled)
    }
    
    // Session Management
    suspend fun insertSession(session: UserSession) {
        userDao.insertSession(session)
    }
    
    suspend fun getCurrentSession(userId: String): UserSession? {
        return userDao.getCurrentSession(userId)
    }
    
    suspend fun updateSessionLocation(sessionId: String, city: String?, pincode: String?, lat: Double?, lng: Double?) {
        userDao.updateSessionLocation(sessionId, city, pincode, lat, lng)
    }
    
    // Statistics
    suspend fun getUserCountByCity(city: String): Int {
        return userDao.getUserCountByCity(city)
    }
    
    suspend fun getActiveUserCount(): Int {
        return userDao.getActiveUserCount()
    }
    
    // Cleanup
    suspend fun deleteOldSessions(cutoffDate: LocalDateTime) {
        userDao.deleteOldSessions(cutoffDate)
    }
} 