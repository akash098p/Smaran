package com.smaran.app.settings

/** Presentation helpers for the existing Settings screen. */
object SettingsScreenModel {
    fun snoozeOptions(): List<Int> = listOf(15, 30, 60)

    fun snoozeLabel(minutes: Int): String = when (minutes) {
        60 -> "1 hour"
        else -> "$minutes minutes"
    }
}
