package com.smaran.app.settings

import android.content.Context

/** Small state manager for settings screens. Existing task/reminder stores are untouched. */
class SettingsManager(context: Context) {
    private val preferences = AppPreferences(context)

    fun read(): SettingsUiState = SettingsUiState(
        soundEnabled = preferences.soundEnabled,
        vibrationEnabled = preferences.vibrationEnabled,
        alarmStyleEnabled = preferences.alarmStyleEnabled,
        customSoundUri = preferences.customSoundUri,
        defaultSnoozeMinutes = preferences.defaultSnoozeMinutes,
        darkMode = preferences.darkMode,
        dynamicColor = preferences.dynamicColor
    )

    fun setSoundEnabled(enabled: Boolean) { preferences.soundEnabled = enabled }
    fun setVibrationEnabled(enabled: Boolean) { preferences.vibrationEnabled = enabled }
    fun setAlarmStyleEnabled(enabled: Boolean) { preferences.alarmStyleEnabled = enabled }
    fun setCustomSoundUri(uri: String) { preferences.customSoundUri = uri }
    fun setDefaultSnoozeMinutes(minutes: Int) { preferences.defaultSnoozeMinutes = minutes }
    fun setDarkMode(enabled: Boolean) { preferences.darkMode = enabled }
    fun setDynamicColor(enabled: Boolean) { preferences.dynamicColor = enabled }
}
