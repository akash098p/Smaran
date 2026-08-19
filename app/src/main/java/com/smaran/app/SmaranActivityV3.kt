package com.smaran.app

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
private val Navy = Color(0xFF151733)
private val Background = Color(0xFFF9F8FD)
private val Muted = Color(0xFF747384)
private val Green = Color(0xFF27A96B)
private val Orange = Color(0xFFFF9D25)
private val Red = Color(0xFFF04C5C)

enum class Phase3Tab(val title: String) { HOME("Home"), CALENDAR("Calendar"), TASKS("All Tasks"), STATS("Statistics"), SETTINGS("Settings") }

class SmaranActivityV3 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 41)
        setContent { Phase3App(this) }
    }
}

@Composable
private fun Phase3App(activity: Context) {
    val prefs = remember { activity.getSharedPreferences("smaran_app", Context.MODE_PRIVATE) }
    var onboarding by rememberSaveable { mutableStateOf(!prefs.getBoolean("onboarding_done", false)) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var editor by remember { mutableStateOf<Task?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var rescheduleTask by remember { mutableStateOf<Task?>(null) }
    var refresh by remember { mutableIntStateOf(0) }
    val store = remember { TaskStore(activity) }
    val history = remember { HistoryStore(activity) }
    val scheduler = remember { ReminderScheduler(activity) }
    val tasks = remember(refresh) { store.getTasks() }

    LaunchedEffect(refresh) {
        tasks.filter { !it.completed && it.date.atTime(it.time).isBefore(LocalDateTime.now()) }
            .forEach { task ->
                if (!history.hasActionSince(task.id, HistoryAction.MISSED, task.date.atTime(it.time).minusHours(24))) {
                    history.record(task.id, HistoryAction.MISSED)
                }
            }
    }

    if (onboarding) {
        Phase3Onboarding {
            prefs.edit().putBoolean("onboarding_done", true).apply()
            onboarding = false
        }
        return
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                Phase3Tab.entries.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Icon(tabIcon(item), null) },
                        label = { Text(item.title, fontSize = 9.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (Phase3Tab.entries[tab]) {
                Phase3Tab.HOME -> Home3(tasks, { editor = null; showEditor = true }) { completeTask(it, store, history, scheduler) { refresh++ } }
                Phase3Tab.CALENDAR -> Calendar3(tasks, { editor = null; showEditor = true })
                Phase3Tab.TASKS -> Tasks3(tasks, { editor = null; showEditor = true }, { editor = it; showEditor = true }, { completeTask(it, store, history, scheduler) { refresh++ } }, { task -> deleteTask(task, store, scheduler, history) { refresh++ } })
                Phase3Tab.STATS -> Statistics3(tasks, history)
                Phase3Tab.SETTINGS -> Settings3()
            }
        }
    }

    if (showEditor) {
        TaskEditor(
            existing = editor,
            onDismiss = { showEditor = false },
            onSave = { task ->
                if (editor == null) {
                    store.add(task)
                    history.record(task.id, HistoryAction.CREATED, next = task.date.atTime(task.time))
                } else {
                    scheduler.cancel(task.id)
                    store.update(task)
                    history.record(task.id, HistoryAction.RESCHEDULED, editor!!.date.atTime(editor!!.time), task.date.atTime(task.time))
                }
                scheduler.schedule(task.id, task.title, task.date.atTime(task.time))
                showEditor = false
                refresh++
            }
        )
    }

    if (rescheduleTask != null) {
        RescheduleDialog(rescheduleTask!!, { rescheduleTask = null }) { dateTime ->
            val task = rescheduleTask!!
            scheduler.cancel(task.id)
            store.update(task.copy(date = dateTime.toLocalDate(), time = dateTime.toLocalTime(), completed = false))
            scheduler.schedule(task.id, task.title, dateTime)
            history.record(task.id, HistoryAction.RESCHEDULED, task.date.atTime(task.time), dateTime)
            rescheduleTask = null
            refresh++
        }
    }
}

private fun completeTask(task: Task, store: TaskStore, history: HistoryStore, scheduler: ReminderScheduler, refresh: () -> Unit) {
    store.update(task.copy(completed = true))
    scheduler.cancel(task.id)
    history.record(task.id, HistoryAction.COMPLETED)
    if (task.recurring) {
        val nextDate = task.date.plusDays(1)
        val next = task.copy(id = System.currentTimeMillis(), date = nextDate, completed = false)
        store.add(next)
        scheduler.schedule(next.id, next.title, next.date.atTime(next.time))
        history.record(next.id, HistoryAction.CREATED, next = next.date.atTime(next.time))
    }
    refresh()
}

private fun deleteTask(task: Task, store: TaskStore, scheduler: ReminderScheduler, history: HistoryStore, refresh: () -> Unit) {
    scheduler.cancel(task.id)
    store.delete(task.id)
    history.record(task.id, HistoryAction.CANCELLED)
    refresh()
}

@Composable private fun tabIcon(tab: Phase3Tab) = when (tab) {
    Phase3Tab.HOME -> Icons.Default.Home
    Phase3Tab.CALENDAR -> Icons.Default.CalendarMonth
    Phase3Tab.TASKS -> Icons.Default.CheckCircle
    Phase3Tab.STATS -> Icons.Default.BarChart
    Phase3Tab.SETTINGS -> Icons.Default.Settings
}

@Composable private fun Home3(tasks: List<Task>, add: () -> Unit, complete: (Task) -> Unit) {
    val today = LocalDate.now()
    val todayTasks = tasks.filter { it.date == today }.sortedBy { it.time }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(greeting3(), color = Muted); Text("Akash", fontSize = 27.sp, fontWeight = FontWeight.Bold) }; Avatar3() } }
        item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Purple), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("The secret of getting ahead is getting started.", color = Color.White, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(6.dp)); Text("— Mark Twain", color = Color.White.copy(.75f), fontSize = 12.sp) } } }
        item { Row(verticalAlignment = Alignment.CenterVertically) { Text("Today · ${today.format(DateTimeFormatter.ofPattern("d MMMM"))}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); TextButton(onClick = add) { Text("+ Add", color = Purple) } } }
        if (todayTasks.isEmpty()) item { Empty3("No tasks today", "Create your first reminder.", add) }
        items(todayTasks) { TaskCard3(it, if (!it.completed) complete else null, null, null) }
        item { AddButton3(add) }
    }
}

@Composable private fun Calendar3(tasks: List<Task>, add: () -> Unit) {
    val today = LocalDate.now(); var selected by remember { mutableStateOf(today) }
    val monthDays = today.lengthOfMonth(); val first = today.withDayOfMonth(1).dayOfWeek.value
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { Text("Calendar", fontSize = 25.sp, fontWeight = FontWeight.Bold) }
        item { Month3(first, monthDays, selected.dayOfMonth) { selected = today.withDayOfMonth(it) } }
        item { Text(selected.format(DateTimeFormatter.ofPattern("d MMMM yyyy")), fontWeight = FontWeight.Bold) }
        val dayTasks = tasks.filter { it.date == selected }.sortedBy { it.time }
        if (dayTasks.isEmpty()) item { Empty3("Nothing scheduled", "This day is clear.", add) }
        items(dayTasks) { TaskCard3(it, null, null, null) }
        item { Button(onClick = add, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(7.dp)); Text("Add Task") } }
    }
}

@Composable private fun Month3(first: Int, count: Int, selected: Int, select: (Int) -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth()) { listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, color = Muted, fontSize = 10.sp) } }
            Spacer(Modifier.height(8.dp))
            ((0 until first - 1).toList() + (1..count).toList()).chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(7) { i ->
                        val day = week.getOrNull(i) ?: 0
                        Box(Modifier.weight(1f).padding(vertical = 4.dp), Alignment.Center) {
                            if (day > 0) Box(Modifier.size(34.dp).clip(CircleShape).background(if (day == selected) Purple else Color.Transparent).clickable { select(day) }, Alignment.Center) { Text(day.toString(), color = if (day == selected) Color.White else Color(0xFF171728), fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun Tasks3(tasks: List<Task>, add: () -> Unit, edit: (Task) -> Unit, complete: (Task) -> Unit, delete: (Task) -> Unit) {
    var search by remember { mutableStateOf("") }; var filter by remember { mutableStateOf("All") }
    val visible = tasks.filter { it.title.contains(search, true) && when(filter) { "Today" -> it.date == LocalDate.now(); "Upcoming" -> it.date >= LocalDate.now() && !it.completed; "Completed" -> it.completed; else -> true } }.sortedBy { it.date.atTime(it.time) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item { Text("All Tasks", fontSize = 25.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(9.dp)); OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search tasks...") }, leadingIcon = { Icon(Icons.Default.Search, null) }) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { listOf("All","Today","Upcoming","Completed").forEach { Chip3(it, it == filter) { filter = it } } } }
        if (visible.isEmpty()) item { Empty3("No matching tasks", "Create a new reminder to get started.", add) }
        items(visible) { task -> TaskCard3(task, if (!task.completed) complete else null, edit, delete) }
        item { AddButton3(add) }
    }
}

@Composable private fun Statistics3(tasks: List<Task>, history: HistoryStore) {
    val total = tasks.size; val done = tasks.count { it.completed }; val pending = total - done; val rate = if (total == 0) 0 else done * 100 / total
    val completedDays = tasks.filter { it.completed }.map { it.date }.toSet(); var streak = 0; var cursor = LocalDate.now()
    while (completedDays.contains(cursor)) { streak++; cursor = cursor.minusDays(1) }
    val events = history.getAll().take(12)
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { Text("Statistics", fontSize = 25.sp, fontWeight = FontWeight.Bold); Text("Your productivity overview", color = Muted, fontSize = 12.sp) }
        item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Navy), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Tasks Completed", color = Color.White.copy(.7f)); Text(done.toString(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold); Text("$rate% completion rate", color = Color.White.copy(.75f)) } } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) { Stat3("Total", total.toString(), Modifier.weight(1f)); Stat3("Done", done.toString(), Modifier.weight(1f)); Stat3("Pending", pending.toString(), Modifier.weight(1f)) } }
        item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Current Streak 🔥", fontWeight = FontWeight.Bold); Text("$streak days", color = Purple, fontSize = 29.sp, fontWeight = FontWeight.Bold); Text("Complete at least one task every day to build your streak.", color = Muted, fontSize = 12.sp) } } }
        item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(17.dp)) { Text("Activity History", fontWeight = FontWeight.Bold); events.forEach { e -> Text("${e.timestamp.format(DateTimeFormatter.ofPattern("dd MMM · HH:mm"))}  ·  ${e.action.name.replace('_', ' ')}", color = if(e.action == HistoryAction.COMPLETED) Green else Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 9.dp)) } } } }
    }
}

@Composable private fun Settings3() { LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { Text("Settings", fontSize = 27.sp, fontWeight = FontWeight.Bold) }; item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Avatar3(); Spacer(Modifier.width(13.dp)); Column { Text("Akash", fontWeight = FontWeight.Bold); Text("Local Smaran profile", color = Muted, fontSize = 12.sp) } } } }; item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Column { listOf("General","Reminders","Appearance","Backup & Restore","Data & Storage","Notifications","Privacy","About Smaran").forEach { label -> Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Settings, null, tint = Muted); Spacer(Modifier.width(13.dp)); Text(label, Modifier.weight(1f)); Text("›", color = Muted, fontSize = 21.sp) } } } } } } }

@Composable private fun TaskCard3(task: Task, complete: ((Task) -> Unit)?, edit: ((Task) -> Unit)?, delete: ((Task) -> Unit)?) {
    val color = when(task.category) { "Work" -> Red; "Study" -> Purple; "Health" -> Green; else -> Orange }
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().clickable(enabled = edit != null) { edit?.invoke(task) }) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(3.dp).height(53.dp).background(color)); Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(task.title, fontWeight = FontWeight.SemiBold); Text("${task.date} · ${task.time.format(DateTimeFormatter.ofPattern("hh:mm a"))}", color = Muted, fontSize = 11.sp); Row(verticalAlignment = Alignment.CenterVertically) { Chip3(task.category, false) {}; if(task.recurring) { Spacer(Modifier.width(5.dp)); Text("↻", color = Purple, fontSize = 13.sp) } } }
            if (complete != null) Icon(Icons.Default.CheckCircle, "Complete", tint = Purple, modifier = Modifier.size(25.dp).clickable { complete(task) }) else if(task.completed) Icon(Icons.Default.Check, "Completed", tint = Green)
            if(edit != null && delete != null) { IconButton(onClick = { delete(task) }) { Icon(Icons.Default.DeleteOutline, "Delete", tint = Red) } }
        }
    }
}

@Composable private fun TaskEditor(existing: Task?, onDismiss: () -> Unit, onSave: (Task) -> Unit) {
    val context = LocalContext.current
    var title by remember(existing) { mutableStateOf(existing?.title ?: "") }; var description by remember(existing) { mutableStateOf(existing?.description ?: "") }
    var date by remember(existing) { mutableStateOf(existing?.date ?: LocalDate.now()) }; var time by remember(existing) { mutableStateOf(existing?.time ?: LocalTime.now().plusHours(1).withMinute(0)) }
    var category by remember(existing) { mutableStateOf(existing?.category ?: "Personal") }; var priority by remember(existing) { mutableStateOf(existing?.priority ?: Priority.MEDIUM) }; var recurring by remember(existing) { mutableStateOf(existing?.recurring ?: false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if(existing == null) "Create Task" else "Edit Task") }, text = { Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Task title") }); OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Description") })
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { OutlinedButton(onClick = { DatePickerDialog(context, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) }, date.year, date.monthValue - 1, date.dayOfMonth).show() }, modifier = Modifier.weight(1f)) { Text(date.toString()) }; OutlinedButton(onClick = { TimePickerDialog(context, { _, h, m -> time = LocalTime.of(h, m) }, time.hour, time.minute, true).show() }, modifier = Modifier.weight(1f)) { Text(time.format(DateTimeFormatter.ofPattern("hh:mm a"))) } }
        Text("Category", fontSize = 12.sp, color = Muted); Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { listOf("Personal","Work","Study","Health").forEach { Chip3(it, it == category) { category = it } } }
        Text("Priority", fontSize = 12.sp, color = Muted); Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { Priority.entries.forEach { Chip3(it.name.lowercase().replaceFirstChar(Char::uppercase), it == priority) { priority = it } } }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(recurring, { recurring = it }); Text("Repeat daily") }
    } }, confirmButton = { Button(enabled = title.isNotBlank(), onClick = { onSave(Task(existing?.id ?: System.currentTimeMillis(), title.trim(), description.trim(), date, time, category, priority, existing?.completed ?: false, recurring)) }, colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable private fun RescheduleDialog(task: Task, dismiss: () -> Unit, save: (LocalDateTime) -> Unit) {
    val context = LocalContext.current; var date by remember { mutableStateOf(task.date) }; var time by remember { mutableStateOf(task.time) }
    AlertDialog(onDismissRequest = dismiss, title = { Text("Reschedule reminder") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(task.title, fontWeight = FontWeight.Bold); Text("Choose a new date and time.", color = Muted, fontSize = 12.sp); OutlinedButton(onClick = { DatePickerDialog(context, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) }, date.year, date.monthValue - 1, date.dayOfMonth).show() }, Modifier.fillMaxWidth()) { Text(date.toString()) }; OutlinedButton(onClick = { TimePickerDialog(context, { _, h, m -> time = LocalTime.of(h, m) }, time.hour, time.minute, true).show() }, Modifier.fillMaxWidth()) { Text(time.format(DateTimeFormatter.ofPattern("hh:mm a"))) } } }, confirmButton = { Button(onClick = { save(date.atTime(time)) }, colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text("Reschedule") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}

@Composable private fun Phase3Onboarding(done: () -> Unit) { var page by rememberSaveable { mutableIntStateOf(0) }; val titles = listOf("Welcome to Smaran","Plan Your Tasks","Smart Reminders","Track & Improve","Let's Achieve More"); val bodies = listOf("Your intelligent reminder companion.","Create tasks with date & time and never miss what matters.","Snooze for 15 minutes, 30 minutes or 1 hour, then reschedule.","See completed, pending, missed and rescheduled activity.","Stay focused, stay productive and achieve your goals."); Surface(Modifier.fillMaxSize(), color = Navy) { Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) { Text("Skip", Modifier.fillMaxWidth().clickable { done() }, color = Color.White, textAlign = TextAlign.End); Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("⌛", fontSize = 72.sp); Text(titles[page], color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center); Spacer(Modifier.height(10.dp)); Text(bodies[page], color = Color.White.copy(.75f), textAlign = TextAlign.Center) }; Row { repeat(5) { Box(Modifier.padding(3.dp).size(if(it == page) 9.dp else 7.dp).clip(CircleShape).background(Color.White.copy(if(it == page) 1f else .35f))) } }; Button(onClick = { if(page == 4) done() else page++ }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD8C5FF)), shape = RoundedCornerShape(14.dp)) { Text(if(page == 4) "Get Started →" else "Next", color = Navy) } } } }

@Composable private fun Chip3(text: String, selected: Boolean, onClick: () -> Unit) { Box(Modifier.clip(RoundedCornerShape(20.dp)).background(if(selected) Purple else Color(0xFFF0EEF6)).clickable { onClick() }.padding(horizontal = 10.dp, vertical = 6.dp)) { Text(text, color = if(selected) Color.White else Color(0xFF171728), fontSize = 10.sp, fontWeight = FontWeight.SemiBold) } }
@Composable private fun Stat3(label: String, value: String, modifier: Modifier) { Card(modifier, RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(13.dp)) { Text(label, color = Muted, fontSize = 10.sp); Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold) } } }
@Composable private fun AddButton3(add: () -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { Button(onClick = add, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Purple), contentPadding = PaddingValues(0.dp), modifier = Modifier.size(54.dp)) { Icon(Icons.Default.Add, "Add task") } } }
@Composable private fun Empty3(title: String, body: String, add: () -> Unit) { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.AccessTime, null, tint = Purple); Spacer(Modifier.height(6.dp)); Text(title, fontWeight = FontWeight.Bold); Text(body, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center); Spacer(Modifier.height(9.dp)); TextButton(onClick = add) { Text("Create task", color = Purple) } } } }
@Composable private fun Avatar3() { Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFEDE5FF)), Alignment.Center) { Text("A", color = Purple, fontWeight = FontWeight.Bold, fontSize = 20.sp) } }
private fun greeting3(): String = when(java.time.LocalTime.now().hour) { in 5..11 -> "Good Morning, 👋"; in 12..16 -> "Good Afternoon, 👋"; else -> "Good Evening, 👋" }
