package com.smaran.app.settings

import android.content.Context

/** Safe facade over the existing local preference stores. */
class SettingsRepository(context: Context) {
    private val manager = SettingsManager(context)

    fun get(): SettingsUiState = manager.read()

    fun updateSound(enabled: Boolean): SettingsUiState {
        manager.setSoundEnabled(enabled)
        return manager.read()
    }

    fun updateVibration(enabled: Boolean): SettingsUiState {
        manager.setVibrationEnabled(enabled)
        return manager.read()
    }

    fun updateSnooze(minutes: Int): SettingsUiState {
        require(minutes > 0) { "Snooze duration must be positive" }
        manager.setDefaultSnoozeMinutes(minutes)
        return manager.read()
    }

    fun updateDarkMode(enabled: Boolean): SettingsUiState {
        manager.setDarkMode(enabled)
        return manager.read()
    }

    fun updateDynamicColor(enabled: Boolean): SettingsUiState {
        manager.setDynamicColor(enabled)
        return manager.read()
    }
}
