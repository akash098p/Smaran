package com.smaran.app.data.model

import java.time.LocalDateTime

/** Represents a meaningful event in a task's lifecycle. */
data class TaskHistory(
    val id: Long,
    val taskId: Long,
    val action: HistoryAction,
    val timestamp: LocalDateTime,
    val previousDateTime: LocalDateTime? = null,
    val newDateTime: LocalDateTime? = null
)

enum class HistoryAction {
    CREATED,
    REMINDER_TRIGGERED,
    SNOOZED,
    RESCHEDULED,
    COMPLETED,
    MISSED,
    CANCELLED
}
