package com.example.reportviolation.domain.service

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*
import com.example.reportviolation.R

class LanguageManager(private val context: Context) {
    
    companion object {
        const val LANGUAGE_ENGLISH = "English"
        const val LANGUAGE_TAMIL = "Tamil"
        
        private const val PREFS_NAME = "LanguagePrefs"
        private const val KEY_LANGUAGE = "selected_language"
    }
    
    fun getLanguageDisplayName(language: String): String {
        return when (language) {
            LANGUAGE_TAMIL -> "தமிழ்"
            else -> "English"
        }
    }
    
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    fun getCurrentLanguage(): String {
        return sharedPreferences.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }
    
    fun setLanguage(language: String) {
        val locale = when (language) {
            LANGUAGE_TAMIL -> Locale("ta")
            else -> Locale("en")
        }
        
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        
        // Save to preferences
        sharedPreferences.edit().putString(KEY_LANGUAGE, language).apply()
    }
    
    fun getLocaleForLanguage(language: String): Locale {
        return when (language) {
            LANGUAGE_TAMIL -> Locale("ta")
            else -> Locale("en")
        }
    }
    
    fun getLanguageCode(language: String): String {
        return when (language) {
            LANGUAGE_TAMIL -> "ta"
            else -> "en"
        }
    }
    
    fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            "ta" -> LANGUAGE_TAMIL
            else -> LANGUAGE_ENGLISH
        }
    }
}
