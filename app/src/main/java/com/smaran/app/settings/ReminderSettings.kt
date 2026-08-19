package com.smaran.app.settings

import android.content.Context
import com.smaran.app.reminder.AlarmPermissionHelper

/** Read-only presentation model for reminder settings. */
data class ReminderSettings(
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val defaultSnoozeMinutes: Int,
    val exactAlarmAllowed: Boolean
)

fun Context.readReminderSettings(): ReminderSettings {
    val preferences = ReminderPreferences(this)
    return ReminderSettings(
        soundEnabled = preferences.soundEnabled,
        vibrationEnabled = preferences.vibrationEnabled,
        defaultSnoozeMinutes = preferences.defaultSnoozeMinutes,
        exactAlarmAllowed = AlarmPermissionHelper.canScheduleExactAlarms(this)
    )
}
