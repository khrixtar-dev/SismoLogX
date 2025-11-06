package com.example.sismologx.util

import android.content.Context
import android.content.SharedPreferences
import kotlin.math.round

class SettingsPrefs (context: Context){
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_NOTIFICATION_THRESHOLD = "notification_threshold"
        const val DEFAULT_THRESHOLD = 4.0
    }

    fun isNotificationsEnabled(): Boolean =
        prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)

    fun setNotificationsEnabled(enabled: Boolean){
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun getThreshold(): Double =
        java.lang.Double.longBitsToDouble(
            prefs.getLong(KEY_NOTIFICATION_THRESHOLD, java.lang.Double.doubleToRawLongBits(DEFAULT_THRESHOLD))
        )

    fun setThreshold(value: Double){
        //opcional : redondear a 0.1
        val rounded = round(value * 10) / 10.0
        prefs.edit().putLong(KEY_NOTIFICATION_THRESHOLD, java.lang.Double.doubleToRawLongBits(rounded)).apply()
    }
}