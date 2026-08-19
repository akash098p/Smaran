package com.smaran.app.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smaran.app.data.local.TaskStore
import com.smaran.app.reminder.scheduler.ReminderScheduler
import java.time.LocalDateTime

/** Restores future task alarms after Android finishes booting. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED &&
            intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val pendingResult = goAsync()
        Thread {
            try {
                val now = LocalDateTime.now()
                val scheduler = ReminderScheduler(context.applicationContext)
                TaskStore(context.applicationContext).getTasks()
                    .asSequence()
                    .filter { !it.completed }
                    .map { task -> task to task.date.atTime(task.time) }
                    .filter { (_, dateTime) -> dateTime.isAfter(now) }
                    .forEach { (task, dateTime) ->
                        scheduler.schedule(task.id, task.title, dateTime)
                    }
            } finally {
                pendingResult.finish()
            }
        }.start()
    }
}
