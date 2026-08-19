package com.smaran.app.reminder

/** Actions available from a reminder notification. */
enum class ReminderAction {
    COMPLETE,
    SNOOZE_15_MIN,
    SNOOZE_30_MIN,
    SNOOZE_1_HOUR,
    RESCHEDULE,
    DISMISS
}
