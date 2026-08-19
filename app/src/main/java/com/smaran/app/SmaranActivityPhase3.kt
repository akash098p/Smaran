package com.smaran.app

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smaran.app.data.local.HistoryStore
import com.smaran.app.data.local.TaskStore
import com.smaran.app.data.model.HistoryAction
import com.smaran.app.data.model.Priority
import com.smaran.app.data.model.Task
import com.smaran.app.reminder.scheduler.ReminderScheduler
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val Purple = Color(0xFF5B2BD9)
private val Background = Color(0xFFF9F8FD)
private val Muted = Color(0xFF747384)

class SmaranActivityPhase3 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 41)
        setContent { SmaranPhase3(this) }
    }
}

@Composable
private fun SmaranPhase3(context: Context) {
    val store = remember { TaskStore(context) }
    val history = remember { HistoryStore(context) }
    val scheduler = remember { ReminderScheduler(context) }
    var refresh by remember { mutableIntStateOf(0) }
    var editor by remember { mutableStateOf<Task?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tasks = remember(refresh) { store.getTasks() }

    Scaffold(
        containerColor = Background,
        floatingActionButton = { FloatingActionButton(onClick = { editor = null; showEditor = true }, containerColor = Purple) { Icon(Icons.Default.Add, "Add task") } },
        bottomBar = {
            NavigationBar {
                listOf("Home", "Calendar", "All Tasks", "Statistics", "Settings").forEachIndexed { i, label ->
                    NavigationBarItem(tab == i, { tab = i }, icon = { Icon(listOf(Icons.Default.Home, Icons.Default.CalendarMonth, Icons.Default.CheckCircle, Icons.Default.BarChart, Icons.Default.Settings)[i], null) }, label = { Text(label, fontSize = 8.sp) })
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> HomePhase3(tasks, onComplete = { complete(it, store, history, scheduler) { refresh++ } })
                1 -> CalendarPhase3(tasks)
                2 -> TasksPhase3(tasks, onEdit = { editor = it; showEditor = true }, onComplete = { complete(it, store, history, scheduler) { refresh++ } }, onDelete = { delete(it, store, scheduler, history) { refresh++ } })
                3 -> StatisticsPhase3(tasks, history)
                else -> SettingsPhase3()
            }
        }
    }

    if (showEditor) TaskEditorPhase3(editor, onDismiss = { showEditor = false }) { task ->
        if (editor == null) history.record(task.id, HistoryAction.CREATED, next = task.date.atTime(task.time))
        else history.record(task.id, HistoryAction.RESCHEDULED, editor!!.date.atTime(editor!!.time), task.date.atTime(task.time))
        scheduler.cancel(task.id)
        store.add(task)
        scheduler.schedule(task.id, task.title, task.date.atTime(task.time))
        showEditor = false
        refresh++
    }
}

private fun complete(task: Task, store: TaskStore, history: HistoryStore, scheduler: ReminderScheduler, refresh: () -> Unit) {
    store.update(task.copy(completed = true)); scheduler.cancel(task.id); history.record(task.id, HistoryAction.COMPLETED)
    if (task.recurring) {
        val next = task.copy(id = System.currentTimeMillis(), date = task.date.plusDays(1), completed = false)
        store.add(next); scheduler.schedule(next.id, next.title, next.date.atTime(next.time)); history.record(next.id, HistoryAction.CREATED, next = next.date.atTime(next.time))
    }
    refresh()
}

private fun delete(task: Task, store: TaskStore, scheduler: ReminderScheduler, history: HistoryStore, refresh: () -> Unit) {
    scheduler.cancel(task.id); store.delete(task.id); history.record(task.id, HistoryAction.CANCELLED); refresh()
}

@Composable private fun HomePhase3(tasks: List<Task>, onComplete: (Task) -> Unit) {
    val today = LocalDate.now(); val list = tasks.filter { it.date == today }.sortedBy { it.time }
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
        item { Text("Good morning, 👋", color = Muted); Text("Akash", fontSize = 28.sp, fontWeight = FontWeight.Bold) }
        item { Card(colors = CardDefaults.cardColors(containerColor = Purple), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(18.dp)) { Text("The secret of getting ahead is getting started.", color = Color.White, fontWeight = FontWeight.SemiBold); Text("— Mark Twain", color = Color.White.copy(.7f), fontSize = 12.sp) } } }
        item { Text("Today · ${today.format(DateTimeFormatter.ofPattern("d MMMM"))}", fontWeight = FontWeight.Bold) }
        if (list.isEmpty()) item { Text("No tasks today. Tap + to create a reminder.", color = Muted) }
        items(list) { TaskRowPhase3(it, onComplete, null, null) }
    }
}

@Composable private fun CalendarPhase3(tasks: List<Task>) {
    var date by remember { mutableStateOf(LocalDate.now()) }
    val list = tasks.filter { it.date == date }.sortedBy { it.time }
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
        item { Text("Calendar", fontSize = 26.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); OutlinedButton(onClick = { date = date.minusDays(1) }) { Text("‹ Previous day") }; OutlinedButton(onClick = { date = date.plusDays(1) }) { Text("Next day ›") }; Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")), fontWeight = FontWeight.Bold) }
        if (list.isEmpty()) item { Text("Nothing scheduled.", color = Muted) }
        items(list) { TaskRowPhase3(it, null, null, null) }
    }
}

@Composable private fun TasksPhase3(tasks: List<Task>, onEdit: (Task) -> Unit, onComplete: (Task) -> Unit, onDelete: (Task) -> Unit) {
    var filter by remember { mutableStateOf("All") }
    val list = tasks.filter { when(filter) { "Today" -> it.date == LocalDate.now(); "Pending" -> !it.completed; "Completed" -> it.completed; else -> true } }.sortedBy { it.date.atTime(it.time) }
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
        item { Text("All Tasks", fontSize = 26.sp, fontWeight = FontWeight.Bold); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("All","Today","Pending","Completed").forEach { TextButton(onClick = { filter = it }) { Text(it, color = if(filter == it) Purple else Muted) } } } }
        items(list) { TaskRowPhase3(it, onComplete, onEdit, onDelete) }
    }
}

@Composable private fun StatisticsPhase3(tasks: List<Task>, history: HistoryStore) {
    val total = tasks.size; val done = tasks.count { it.completed }; val pending = total - done
    val doneDays = tasks.filter { it.completed }.map { it.date }.toSet(); var streak = 0; var d = LocalDate.now(); while(doneDays.contains(d)) { streak++; d = d.minusDays(1) }
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
        item { Text("Statistics", fontSize = 26.sp, fontWeight = FontWeight.Bold) }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Metric("Total", total); Metric("Done", done); Metric("Pending", pending) } }
        item { Card(shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(18.dp)) { Text("Current Streak 🔥", fontWeight = FontWeight.Bold); Text("$streak days", color = Purple, fontSize = 30.sp, fontWeight = FontWeight.Bold) } } }
        item { Card(shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(18.dp)) { Text("Recent History", fontWeight = FontWeight.Bold); history.getAll().take(15).forEach { Text("${it.timestamp.format(DateTimeFormatter.ofPattern("dd MMM HH:mm"))} · ${it.action.name}", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 7.dp)) } } } }
    }
}

@Composable private fun SettingsPhase3() { LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(0.dp), contentPadding = PaddingValues(bottom = 80.dp)) { item { Text("Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(18.dp)); Card(shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(18.dp)) { Text("Akash", fontWeight = FontWeight.Bold); Text("Local Smaran profile", color = Muted, fontSize = 12.sp) } }; Spacer(Modifier.height(16.dp)) }; items(listOf("General","Reminders","Appearance","Backup & Restore","Data & Storage","Notifications","Privacy","About Smaran")) { label -> ListItem(headlineContent = { Text(label) }, leadingContent = { Icon(Icons.Default.Settings, null) }, trailingContent = { Text("›", fontSize = 22.sp, color = Muted) }) } } }

@Composable private fun TaskRowPhase3(task: Task, onComplete: ((Task) -> Unit)?, onEdit: ((Task) -> Unit)?, onDelete: ((Task) -> Unit)?) {
    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable(enabled = onEdit != null) { onEdit?.invoke(task) }) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(task.title, fontWeight = FontWeight.SemiBold); Text("${task.date} · ${task.time.format(DateTimeFormatter.ofPattern("hh:mm a"))}", color = Muted, fontSize = 11.sp); Text("${task.category} · ${task.priority.name.lowercase()}", color = Purple, fontSize = 10.sp) }; if(onComplete != null) IconButton({ onComplete(task) }) { Icon(Icons.Default.CheckCircle, "Complete", tint = Purple) }; if(onDelete != null) IconButton({ onDelete(task) }) { Icon(Icons.Default.DeleteOutline, "Delete", tint = Color.Red) } else if(task.completed) Icon(Icons.Default.Check, "Done", tint = Color(0xFF27A96B)) } }
}

@Composable private fun Metric(label: String, value: Int) { Card(Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Column(Modifier.padding(13.dp)) { Text(label, color = Muted, fontSize = 10.sp); Text(value.toString(), fontSize = 21.sp, fontWeight = FontWeight.Bold) } } }

@Composable private fun TaskEditorPhase3(existing: Task?, onDismiss: () -> Unit, onSave: (Task) -> Unit) {
    val context = LocalContext.current; var title by remember(existing) { mutableStateOf(existing?.title ?: "") }; var description by remember(existing) { mutableStateOf(existing?.description ?: "") }; var date by remember(existing) { mutableStateOf(existing?.date ?: LocalDate.now()) }; var time by remember(existing) { mutableStateOf(existing?.time ?: LocalTime.now().plusHours(1).withMinute(0)) }; var category by remember(existing) { mutableStateOf(existing?.category ?: "Personal") }; var priority by remember(existing) { mutableStateOf(existing?.priority ?: Priority.MEDIUM) }; var recurring by remember(existing) { mutableStateOf(existing?.recurring ?: false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if(existing == null) "Create Task" else "Edit / Reschedule") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Task title") }); OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Description") })
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { OutlinedButton({ DatePickerDialog(context, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) }, date.year, date.monthValue - 1, date.dayOfMonth).show() }, Modifier.weight(1f)) { Text(date.toString()) }; OutlinedButton({ TimePickerDialog(context, { _, h, m -> time = LocalTime.of(h, m) }, time.hour, time.minute, true).show() }, Modifier.weight(1f)) { Text(time.format(DateTimeFormatter.ofPattern("hh:mm a"))) } }
        Text("Category", color = Muted, fontSize = 11.sp); Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf("Personal","Work","Study","Health").forEach { FilterChip(selected = category == it, onClick = { category = it }, label = { Text(it) }) } }
        Text("Priority", color = Muted, fontSize = 11.sp); Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { Priority.entries.forEach { FilterChip(selected = priority == it, onClick = { priority = it }, label = { Text(it.name.lowercase()) }) } }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(recurring, { recurring = it }); Text("Repeat daily") }
    } }, confirmButton = { Button(enabled = title.isNotBlank(), onClick = { onSave(Task(existing?.id ?: System.currentTimeMillis(), title.trim(), description.trim(), date, time, category, priority, existing?.completed ?: false, recurring)) }, colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
