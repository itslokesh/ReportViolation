package com.example.reportviolation.data.remote.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenPrefs {
    private const val PREF_NAME = "auth_tokens"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"

    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        // Load any existing tokens on app start
        TokenStore.update(
            prefs.getString(KEY_ACCESS, null),
            prefs.getString(KEY_REFRESH, null)
        )
    }

    fun persist(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        prefs.edit()
            .putString(KEY_ACCESS, TokenStore.accessToken)
            .putString(KEY_REFRESH, TokenStore.refreshToken)
            .apply()
    }
}


