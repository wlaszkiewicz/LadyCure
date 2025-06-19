package com.example.ladycure.utility

import android.content.Context
import androidx.core.content.edit

object SharedPreferencesHelper {
    private const val PREFS_NAME = "AppPreferences"
    private const val KEY_CITY = "selected_city"
    private const val KEY_REMEMBER_CHOICE = "remember_city_choice"
    private const val KEY_LOCATION_PERMISSION_DENIED = "location_permission_denied"

    fun saveCity(context: Context, city: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_CITY, city) }
    }

    fun getCity(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CITY, null)
    }

    fun saveRememberChoice(context: Context, remember: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_REMEMBER_CHOICE, remember) }
    }

    fun shouldRememberChoice(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_REMEMBER_CHOICE, false)
    }

    fun clearPreferences(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { clear() }
    }

}