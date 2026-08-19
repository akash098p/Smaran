package com.smaran.app.data.model

import java.time.LocalDate
import java.time.LocalTime

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
    val recurring: Boolean = false
)

enum class Priority { LOW, MEDIUM, HIGH }
