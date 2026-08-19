package com.smaran.app.reminder

import java.time.LocalDateTime

data class RescheduleOption(
    val dateTime: LocalDateTime,
    val label: String
)
