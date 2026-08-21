package com.smaran.app.settings

import android.content.Context

/** Persists reminder preferences without changing existing task scheduling behavior. */
class ReminderPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

    var alarmStyleEnabled: Boolean
        get() = prefs.getBoolean(KEY_ALARM_STYLE, false)
        set(value) = prefs.edit().putBoolean(KEY_ALARM_STYLE, value).apply()

    var customSoundUri: String
        get() = prefs.getString(KEY_CUSTOM_SOUND_URI, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_SOUND_URI, value).apply()

    var defaultSnoozeMinutes: Int
        get() = prefs.getInt(KEY_SNOOZE, 15)
        set(value) = prefs.edit().putInt(KEY_SNOOZE, value).apply()

    companion object {
        private const val PREFS_NAME = "smaran_reminder_preferences"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_ALARM_STYLE = "alarm_style_enabled"
        private const val KEY_CUSTOM_SOUND_URI = "custom_sound_uri"
        private const val KEY_SNOOZE = "default_snooze_minutes"
    }
}
