package com.example.reportviolation.ui.screens.settings

import android.content.Context
import com.example.reportviolation.data.remote.ApiClient
import com.example.reportviolation.data.remote.AuthApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reportviolation.domain.service.LanguageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userName: String = "Rajesh Kumar",
    val selectedLanguage: String = "English",
    val showLanguageDropdown: Boolean = false,
    val governmentNotifications: Boolean = true,
    val serviceUpdates: Boolean = true,
    val offers: Boolean = true,
    val rewardPoints: String = "1,250",
    val showFeedback: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showLanguageConfirmationDialog: Boolean = false,
    val pendingLanguageChange: String? = null
)

class SettingsViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    private var languageManager: LanguageManager? = null
    private var appContext: Context? = null
    
               fun initialize(context: Context) {
               val applicationContext = context.applicationContext
               appContext = applicationContext
               languageManager = LanguageManager(applicationContext)
               val currentLanguage = languageManager?.getCurrentLanguage() ?: LanguageManager.LANGUAGE_ENGLISH
               _uiState.update { it.copy(selectedLanguage = currentLanguage) }
           }
           
           fun getLanguageDisplayName(language: String): String {
               return languageManager?.getLanguageDisplayName(language) ?: language
           }
    
    fun updateLanguage(language: String) {
        val currentLanguage = _uiState.value.selectedLanguage
        if (language != currentLanguage) {
            _uiState.update { 
                it.copy(
                    showLanguageConfirmationDialog = true,
                    pendingLanguageChange = language
                )
            }
        }
    }
    
    fun toggleLanguageDropdown() {
        _uiState.update { it.copy(showLanguageDropdown = !it.showLanguageDropdown) }
    }
    
    fun updateGovernmentNotifications(enabled: Boolean) {
        _uiState.update { it.copy(governmentNotifications = enabled) }
        // TODO: Save notification preference to SharedPreferences or database
    }
    
    fun updateServiceUpdates(enabled: Boolean) {
        _uiState.update { it.copy(serviceUpdates = enabled) }
        // TODO: Save notification preference to SharedPreferences or database
    }
    
    fun updateOffers(enabled: Boolean) {
        _uiState.update { it.copy(offers = enabled) }
        // TODO: Save notification preference to SharedPreferences or database
    }
    
    fun confirmLanguageChange() {
        val pendingLanguage = _uiState.value.pendingLanguageChange
        if (pendingLanguage != null) {
            viewModelScope.launch {
                languageManager?.setLanguage(pendingLanguage)
                _uiState.update { 
                    it.copy(
                        selectedLanguage = pendingLanguage,
                        showLanguageConfirmationDialog = false,
                        pendingLanguageChange = null
                    )
                }
            }
        }
    }
    
    fun dismissLanguageConfirmationDialog() {
        _uiState.update { 
            it.copy(
                showLanguageConfirmationDialog = false,
                pendingLanguageChange = null
            )
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            // Clear in-memory tokens
            com.example.reportviolation.data.remote.auth.TokenStore.update(null, null)
            // Clear persisted tokens and session flags
            appContext?.let { ctx ->
                runCatching { com.example.reportviolation.data.remote.auth.TokenPrefs.persist(ctx) }
                runCatching { com.example.reportviolation.data.remote.auth.SessionPrefs.setLoginAt(ctx, 0L) }
                runCatching { com.example.reportviolation.data.remote.auth.SessionPrefs.setProfileComplete(ctx, false) }
                runCatching { com.example.reportviolation.data.remote.auth.SessionPrefs.setLastPhone(ctx, "") }
                runCatching { com.example.reportviolation.data.remote.auth.SessionPrefs.setLastCountry(ctx, "") }
            }
        }
    }
    
    // Simple in-memory memoization to avoid repeated profile fetches when toggling tabs
    private var lastProfileFetchMs: Long = 0L
    private var cachedUserName: String? = null
    private val profileStaleAfterMs: Long = 5 * 60 * 1000 // 5 minutes

    fun loadUserSettings(force: Boolean = false) {
        val now = System.currentTimeMillis()
        val cached = cachedUserName
        val freshEnough = (now - lastProfileFetchMs) < profileStaleAfterMs
        if (!force && cached != null && freshEnough) {
            _uiState.update { it.copy(userName = cached) }
            return
        }
        viewModelScope.launch {
            runCatching {
                val base = ApiClient.retrofit(okhttp3.OkHttpClient.Builder().build())
                val client = ApiClient.buildClientWithAuthenticator(base.create(AuthApi::class.java))
                val api = ApiClient.retrofit(client).create(AuthApi::class.java)
                val res = api.getCitizenProfile()
                val profile = res.data
                val name = profile?.name?.takeIf { !it.isNullOrBlank() } ?: "Citizen"
                cachedUserName = name
                lastProfileFetchMs = System.currentTimeMillis()
                _uiState.update { it.copy(userName = name) }
            }.onFailure {
                // On failure, if we have cached name, keep using it
                cachedUserName?.let { name -> _uiState.update { it.copy(userName = name) } }
            }
        }
    }
}
