package com.smaran.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smaran.app.data.local.TaskStore
import com.smaran.app.reminder.scheduler.ReminderScheduler
import java.time.LocalDateTime

/** Restores future one-shot reminders after device/package restart events. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED -> restoreFutureReminders(context)
        }
    }

    private fun restoreFutureReminders(context: Context) {
        val now = LocalDateTime.now()
        val store = TaskStore(context)
        val scheduler = ReminderScheduler(context)
        store.getTasks()
            .filter { !it.completed && it.date.atTime(it.time).isAfter(now) }
            .forEach { task ->
                scheduler.schedule(task.id, task.title, task.date.atTime(task.time))
            }
    }
}
