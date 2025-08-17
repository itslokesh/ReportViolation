package com.example.reportviolation.data.remote.auth

import android.content.Context

object SessionPrefs {
    private const val PREF_NAME = "session_prefs"
    private const val KEY_LOGIN_AT = "login_at_epoch_ms"
    private const val KEY_LAST_PHONE = "last_phone"
    private const val KEY_LAST_COUNTRY = "last_country"
    private const val KEY_PROFILE_COMPLETE = "profile_complete"

    fun setLoginAt(context: Context, epochMs: Long) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LOGIN_AT, epochMs).apply()
    }

    fun getLoginAt(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LOGIN_AT, 0L)
    }

    fun setLastPhone(context: Context, phone: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_PHONE, phone).apply()
    }

    fun getLastPhone(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_PHONE, null)
    }

    fun setLastCountry(context: Context, countryCode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_COUNTRY, countryCode).apply()
    }

    fun getLastCountry(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_COUNTRY, null)
    }

    fun setProfileComplete(context: Context, complete: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PROFILE_COMPLETE, complete).apply()
    }

    fun isProfileComplete(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PROFILE_COMPLETE, false)
    }
}

