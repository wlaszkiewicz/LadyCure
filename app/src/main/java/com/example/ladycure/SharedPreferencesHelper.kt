package com.example.ladycure

import android.content.Context

object SharedPreferencesHelper {
    private const val PREFS_NAME = "AppPreferences"
    private const val KEY_CITY = "selected_city"
    private const val KEY_REMEMBER_CHOICE = "remember_city_choice"

    fun saveCity(context: Context, city: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CITY, city).apply()
    }

    fun getCity(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CITY, null)
    }

    fun saveRememberChoice(context: Context, remember: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_REMEMBER_CHOICE, remember).apply()
    }

    fun shouldRememberChoice(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_REMEMBER_CHOICE, false)
    }
}