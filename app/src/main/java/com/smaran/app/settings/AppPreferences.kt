package com.smaran.app.settings

import android.content.Context

/** Central access point for non-task application preferences. */
class AppPreferences(context: Context) {
    private val reminder = ReminderPreferences(context)
    private val appearance = AppearancePreferences(context)

    var soundEnabled: Boolean
        get() = reminder.soundEnabled
        set(value) { reminder.soundEnabled = value }

    var vibrationEnabled: Boolean
        get() = reminder.vibrationEnabled
        set(value) { reminder.vibrationEnabled = value }

    var alarmStyleEnabled: Boolean
        get() = reminder.alarmStyleEnabled
        set(value) { reminder.alarmStyleEnabled = value }

    var customSoundUri: String
        get() = reminder.customSoundUri
        set(value) { reminder.customSoundUri = value }

    var defaultSnoozeMinutes: Int
        get() = reminder.defaultSnoozeMinutes
        set(value) { reminder.defaultSnoozeMinutes = value }

    var darkMode: Boolean
        get() = appearance.darkMode
        set(value) { appearance.darkMode = value }

    var dynamicColor: Boolean
        get() = appearance.dynamicColor
        set(value) { appearance.dynamicColor = value }
}
