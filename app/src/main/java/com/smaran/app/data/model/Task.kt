package com.smaran.app.data.model

import java.time.LocalDate
import java.time.LocalTime

/** Task lifecycle states as documented in the README. */
enum class TaskStatus {
    DRAFT,
    SCHEDULED,
    REMINDER_TRIGGERED,
    SNOOZED,
    RESCHEDULED,
    COMPLETED,
    MISSED,
    CANCELLED
}

/** Recurrence patterns as documented in the README. */
enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    WEEKDAYS
}

/** Lightweight task model used by the current UI layer. */
data class Task(
    val id: Long,
    val title: String,
    val description: String = "",
    val date: LocalDate,
    val time: LocalTime,
    val category: String = "Personal",
    val priority: Priority = Priority.MEDIUM,
    val completed: Boolean = false,
    val recurring: Boolean = false,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val status: TaskStatus = TaskStatus.SCHEDULED
)

enum class Priority { LOW, MEDIUM, HIGH }