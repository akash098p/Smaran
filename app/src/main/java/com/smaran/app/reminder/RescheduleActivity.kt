package com.smaran.app.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smaran.app.data.local.HistoryStore
import com.smaran.app.data.local.TaskStore
import com.smaran.app.data.model.HistoryAction
import com.smaran.app.reminder.scheduler.ReminderScheduler
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class RescheduleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        setContent { RescheduleScreen(taskId) { finish() } }
    }

    companion object { const val EXTRA_TASK_ID = "task_id" }
}

@Composable
private fun RescheduleScreen(taskId: Long, close: () -> Unit) {
    val context = LocalContext.current
    val store = remember { TaskStore(context) }
    val history = remember { HistoryStore(context) }
    val scheduler = remember { ReminderScheduler(context) }
    val task = remember(taskId) { store.getTasks().firstOrNull { it.id == taskId } }
    if (task == null) { LaunchedEffect(Unit) { close() }; return }
    var date by remember { mutableStateOf(task.date) }
    var time by remember { mutableStateOf(task.time) }

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("Reschedule reminder", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(task.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(20.dp))
            OutlinedButton(onClick = { DatePickerDialog(context, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) }, date.year, date.monthValue - 1, date.dayOfMonth).show() }, Modifier.fillMaxWidth()) { Text(date.toString()) }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = { TimePickerDialog(context, { _, h, m -> time = LocalTime.of(h, m) }, time.hour, time.minute, true).show() }, Modifier.fillMaxWidth()) { Text(time.format(DateTimeFormatter.ofPattern("hh:mm a"))) }
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                val next = date.atTime(time)
                scheduler.cancel(task.id)
                store.update(task.copy(date = date, time = time, completed = false))
                scheduler.schedule(task.id, task.title, next)
                history.record(task.id, HistoryAction.RESCHEDULED, task.date.atTime(task.time), next)
                close()
            }, Modifier.fillMaxWidth()) { Text("Reschedule") }
            TextButton(onClick = close, Modifier.fillMaxWidth()) { Text("Cancel") }
        }
    }
}
