package com.example.reportviolation.di

import android.content.Context
import com.example.reportviolation.data.local.AppDatabase
import com.example.reportviolation.data.repository.UserRepository
import com.example.reportviolation.domain.service.LanguageManager
import com.example.reportviolation.domain.service.UserRegistrationService

object AppModule {
    
    private var database: AppDatabase? = null
    private var userRepository: UserRepository? = null
    private var languageManager: LanguageManager? = null
    private var userRegistrationService: UserRegistrationService? = null
    
    fun initialize(context: Context) {
        if (database == null) {
            database = AppDatabase.getDatabase(context)
            userRepository = UserRepository(database!!.userDao())
            languageManager = LanguageManager(context)
            userRegistrationService = UserRegistrationService(
                context,
                userRepository!!,
                languageManager!!
            )
        }
    }
    
    fun getUserRepository(): UserRepository {
        return userRepository ?: throw IllegalStateException("AppModule not initialized")
    }
    
    fun getLanguageManager(): LanguageManager {
        return languageManager ?: throw IllegalStateException("AppModule not initialized")
    }
    
    fun getUserRegistrationService(): UserRegistrationService {
        return userRegistrationService ?: throw IllegalStateException("AppModule not initialized")
    }
}
