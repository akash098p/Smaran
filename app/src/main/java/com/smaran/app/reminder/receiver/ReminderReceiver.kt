package com.smaran.app.reminder.receiver

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.smaran.app.R
import com.smaran.app.data.local.HistoryStore
import com.smaran.app.data.local.TaskStore
import com.smaran.app.data.model.HistoryAction
import com.smaran.app.data.model.RecurrenceType
import com.smaran.app.reminder.RescheduleActivity
import com.smaran.app.reminder.scheduler.ReminderScheduler
import com.smaran.app.settings.ReminderPreferences
import java.time.LocalDateTime

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return
        val store = TaskStore(context)
        val history = HistoryStore(context)
        val task = store.getTasks().firstOrNull { it.id == taskId } ?: return
        val scheduler = ReminderScheduler(context)
        when (intent.action) {
            ACTION_COMPLETE -> {
                stopAlarmSound()
                store.update(task.copy(completed = true))
                scheduler.cancel(taskId)
                history.record(taskId, HistoryAction.COMPLETED)
                if (task.recurring) {
                    val nextDate = when (task.recurrenceType) {
                        RecurrenceType.DAILY -> task.date.plusDays(1)
                        RecurrenceType.WEEKLY -> task.date.plusWeeks(1)
                        RecurrenceType.MONTHLY -> task.date.plusMonths(1)
                        RecurrenceType.WEEKDAYS -> {
                            var d = task.date.plusDays(1)
                            while (d.dayOfWeek.value > 5) d = d.plusDays(1)
                            d
                        }
                        RecurrenceType.NONE -> task.date.plusDays(1)
                    }
                    val next = task.copy(id = System.currentTimeMillis(), date = nextDate, completed = false)
                    store.add(next)
                    scheduler.schedule(next.id, next.title, next.date.atTime(next.time))
                    history.record(next.id, HistoryAction.CREATED, next = next.date.atTime(next.time))
                }
                notifyManager(context).cancel(taskId.hashCode())
            }
            ACTION_SNOOZE -> {
                stopAlarmSound()
                val minutes = intent.getIntExtra(EXTRA_MINUTES, 15)
                val next = LocalDateTime.now().plusMinutes(minutes.toLong())
                scheduler.schedule(taskId, task.title, next)
                history.record(taskId, HistoryAction.SNOOZED, task.date.atTime(task.time), next)
                showNotification(context, taskId, task.title, minutes)
            }
            ACTION_FIRE -> {
                history.record(taskId, HistoryAction.REMINDER_TRIGGERED)
                showNotification(context, taskId, task.title, null)
            }
        }
    }

    private fun showNotification(context: Context, taskId: Long, title: String, snoozedFor: Int?) {
        val manager = notifyManager(context)
        val prefs = ReminderPreferences(context)
        val alarmMode = prefs.alarmStyleEnabled
        val customSoundUri = prefs.customSoundUri
        val soundUri = if (customSoundUri.isBlank()) {
            Uri.parse("android.resource://${context.packageName}/${R.raw.notification_sound}")
        } else {
            Uri.parse(customSoundUri)
        }
        val soundKey = Integer.toHexString(soundUri.toString().hashCode())
        val channelId = when {
            alarmMode && prefs.vibrationEnabled -> "${ALARM_VIBRATION_CHANNEL_ID}_$soundKey"
            alarmMode -> "${ALARM_CHANNEL_ID}_$soundKey"
            prefs.soundEnabled && prefs.vibrationEnabled -> "${SOUND_VIBRATION_CHANNEL_ID}_$soundKey"
            prefs.soundEnabled -> "${SOUND_CHANNEL_ID}_$soundKey"
            prefs.vibrationEnabled -> "${SILENT_VIBRATION_CHANNEL_ID}_$soundKey"
            else -> "${SILENT_CHANNEL_ID}_$soundKey"
        }
        val channel = NotificationChannel(channelId, "Smaran Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Task reminder alerts"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(prefs.vibrationEnabled)
            if (!prefs.soundEnabled || alarmMode) {
                setSound(null, null)
            } else {
                setSound(soundUri, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
            }
        }
        manager.createNotificationChannel(channel)
        if (alarmMode && prefs.soundEnabled && snoozedFor == null) startAlarmSound(context, soundUri)
        val openIntent = PendingIntent.getActivity(context, taskId.hashCode(), Intent(context, com.smaran.app.SmaranActivityPhase3::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val completeIntent = actionIntent(context, ACTION_COMPLETE, taskId, 0)
        val snooze15 = actionIntent(context, ACTION_SNOOZE, taskId, 15)
        val snooze30 = actionIntent(context, ACTION_SNOOZE, taskId, 30)
        val snooze60 = actionIntent(context, ACTION_SNOOZE, taskId, 60)
        val rescheduleIntent = PendingIntent.getActivity(context, taskId.hashCode() + 9000, Intent(context, RescheduleActivity::class.java).apply { putExtra(RescheduleActivity.EXTRA_TASK_ID, taskId) }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Reminder: $title")
            .setContentText(if (snoozedFor == null) "It's time for your task." else "Reminded again in $snoozedFor minutes.")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .setVibrate(if (prefs.vibrationEnabled) longArrayOf(0L, 450L, 250L, 450L) else longArrayOf(0L))
            .addAction(0, "Done", completeIntent).addAction(0, "15 min", snooze15).addAction(0, "30 min", snooze30).addAction(0, "1 hour", snooze60).addAction(0, "Reschedule", rescheduleIntent).build()
        manager.notify(taskId.hashCode(), notification)
    }

    private fun actionIntent(context: Context, action: String, taskId: Long, minutes: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply { this.action = action; putExtra(EXTRA_TASK_ID, taskId); putExtra(EXTRA_MINUTES, minutes) }
        return PendingIntent.getBroadcast(context, taskId.hashCode() + action.hashCode() + minutes, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun notifyManager(context: Context) = context.getSystemService(NotificationManager::class.java)

    private fun startAlarmSound(context: Context, soundUri: Uri) {
        synchronized(soundLock) {
            alarmPlayer?.release()
            alarmPlayer = runCatching {
                MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context.applicationContext, soundUri)
                    isLooping = true
                    setOnErrorListener { player, _, _ -> player.release(); alarmPlayer = null; true }
                    prepare()
                    start()
                }
            }.getOrElse {
                alarmPlayer = null
                null
            }
            soundHandler.removeCallbacksAndMessages(null)
            soundHandler.postDelayed({ stopAlarmSound() }, 60_000L)
        }
    }

    private fun stopAlarmSound() {
        synchronized(soundLock) {
            alarmPlayer?.stop()
            alarmPlayer?.release()
            alarmPlayer = null
            soundHandler.removeCallbacksAndMessages(null)
        }
    }

    companion object {
        const val ACTION_FIRE = "com.smaran.app.action.FIRE_REMINDER"
        const val ACTION_COMPLETE = "com.smaran.app.action.COMPLETE_REMINDER"
        const val ACTION_SNOOZE = "com.smaran.app.action.SNOOZE_REMINDER"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_MINUTES = "minutes"
        private const val SOUND_CHANNEL_ID = "smaran_reminders_sound"
        private const val SOUND_VIBRATION_CHANNEL_ID = "smaran_reminders_sound_vibration"
        private const val SILENT_CHANNEL_ID = "smaran_reminders_silent"
        private const val SILENT_VIBRATION_CHANNEL_ID = "smaran_reminders_silent_vibration"
        private const val ALARM_CHANNEL_ID = "smaran_reminders_alarm"
        private const val ALARM_VIBRATION_CHANNEL_ID = "smaran_reminders_alarm_vibration"
        private val soundLock = Any()
        private var alarmPlayer: MediaPlayer? = null
        private val soundHandler = Handler(Looper.getMainLooper())
    }
}
