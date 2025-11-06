package com.example.sismologx.util

import android.content.Context

class NotifyState(context: Context) {
    private val prefs = context.getSharedPreferences("notify_state", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_EPOCH = "last_notified_epoch" // último sismo notificado
    }

    fun getLastEpoch(): Long = prefs.getLong(KEY_LAST_EPOCH, 0L)

    fun setLastEpoch(value: Long) {
        prefs.edit().putLong(KEY_LAST_EPOCH, value).apply()
    }
}
