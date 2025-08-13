package com.example.reportviolation

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.reportviolation.domain.service.LanguageManager
import com.example.reportviolation.ui.navigation.AppNavigation
import com.example.reportviolation.ui.theme.ReportViolationTheme
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize language manager and set saved language
        val languageManager = LanguageManager(this)
        val savedLanguage = languageManager.getCurrentLanguage()
        setLocale(savedLanguage)
        
        setContent {
            ReportViolationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
    
    private fun setLocale(language: String) {
        val locale = when (language) {
            LanguageManager.LANGUAGE_TAMIL -> Locale("ta")
            else -> Locale("en")
        }
        
        Locale.setDefault(locale)
        
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    
    override fun attachBaseContext(newBase: Context) {
        val languageManager = LanguageManager(newBase)
        val savedLanguage = languageManager.getCurrentLanguage()
        val locale = when (savedLanguage) {
            LanguageManager.LANGUAGE_TAMIL -> Locale("ta")
            else -> Locale("en")
        }
        
        Locale.setDefault(locale)
        
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }
}