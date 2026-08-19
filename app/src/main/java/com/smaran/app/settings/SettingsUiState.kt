package com.smaran.app.settings

/** Snapshot used by the settings UI without changing existing reminder behavior. */
data class SettingsUiState(
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val defaultSnoozeMinutes: Int,
    val darkMode: Boolean,
    val dynamicColor: Boolean
)
