package com.smaran.app.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val REMINDERS = "smaran_reminders"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                REMINDERS,
                "Task reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Smaran task reminder notifications"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }
}
