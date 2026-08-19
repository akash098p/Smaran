package com.smaran.app.reminder.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.smaran.app.MainActivity
import com.smaran.app.R
import com.smaran.app.data.local.TaskStore
import com.smaran.app.reminder.scheduler.ReminderScheduler
import java.time.LocalDateTime

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val store = TaskStore(context)
        val task = store.getTasks().firstOrNull { it.id == taskId } ?: return

        when (intent.action) {
            ACTION_COMPLETE -> {
                store.update(task.copy(completed = true))
                ReminderScheduler(context).cancel(taskId)
            }
            ACTION_SNOOZE -> {
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 15)
                val next = LocalDateTime.now().plusMinutes(minutes.toLong())
                ReminderScheduler(context).schedule(taskId, task.title, next)
                showNotification(context, taskId, task.title, minutes)
            }
            ACTION_FIRE -> showNotification(context, taskId, task.title, null)
        }
    }

    private fun showNotification(context: Context, taskId: Long, title: String, snoozedFor: Int?) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Smaran Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Task reminder alerts"
        })

        val openIntent = PendingIntent.getActivity(
            context, taskId.hashCode(), Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val completeIntent = actionIntent(context, ACTION_COMPLETE, taskId)
        val snooze15 = actionIntent(context, ACTION_SNOOZE, taskId).apply { }
        val snooze30 = actionIntent(context, ACTION_SNOOZE, taskId, 30)
        val snooze60 = actionIntent(context, ACTION_SNOOZE, taskId, 60)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(if (snoozedFor == null) "Reminder: $title" else "Snoozed: $title")
            .setContentText(if (snoozedFor == null) "It's time for your task." else "Reminded again in $snoozedFor minutes.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .addAction(0, "Done", completeIntent)
            .addAction(0, "15 min", snooze15)
            .addAction(0, "30 min", snooze30)
            .addAction(0, "1 hour", snooze60)
            .build()

        manager.notify(taskId.hashCode(), notification)
    }

    private fun actionIntent(context: Context, action: String, taskId: Long, minutes: Int = 15): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_MINUTES, minutes)
        }
        return PendingIntent.getBroadcast(
            context, (taskId.hashCode() + action.hashCode() + minutes), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_FIRE = "com.smaran.app.action.FIRE_REMINDER"
        const val ACTION_COMPLETE = "com.smaran.app.action.COMPLETE_REMINDER"
        const val ACTION_SNOOZE = "com.smaran.app.action.SNOOZE_REMINDER"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_MINUTES = "minutes"
        private const val CHANNEL_ID = "smaran_reminders"
    }
}
