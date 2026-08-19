package com.smaran.app

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.smaran.app.data.local.TaskStore
import com.smaran.app.data.model.Priority
import com.smaran.app.data.model.Task
import com.smaran.app.reminder.scheduler.ReminderScheduler
import com.smaran.app.settings.Settings as FunctionalSettings
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val Purple = Color(0xFF5B2BD9)
private val Navy = Color(0xFF151733)
private val Background = Color(0xFFF9F8FD)
private val Muted = Color(0xFF747384)
private val Green = Color(0xFF27A96B)
private val Orange = Color(0xFFFF9D25)
private val Red = Color(0xFFF04C5C)

enum class SmaranTab(val title: String) { HOME("Home"), CALENDAR("Calendar"), TASKS("All Tasks"), STATS("Statistics"), SETTINGS("Settings") }

class SmaranActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 41)
        setContent { SmaranApp(applicationContext) }
    }
}

@Composable
private fun SmaranApp(context: Context) {
    val prefs = remember { context.getSharedPreferences("smaran_app", Context.MODE_PRIVATE) }
    var onboarding by rememberSaveable { mutableStateOf(!prefs.getBoolean("onboarding_done", false)) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    val store = remember { TaskStore(context) }
    val scheduler = remember { ReminderScheduler(context) }
    val tasks = remember(refresh) { store.getTasks() }

    if (onboarding) {
        Onboarding { prefs.edit().putBoolean("onboarding_done", true).apply(); onboarding = false }
        return
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                SmaranTab.entries.forEachIndexed { index, item ->
                    NavigationBarItem(index == tab, { tab = index }, icon = { Icon(tabIcon(item), item.title) }, label = { Text(item.title, fontSize = 9.sp) })
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (SmaranTab.entries[tab]) {
                SmaranTab.HOME -> Home(tasks, { showAdd = true }) { complete(it, store, scheduler) { refresh++ } }
                SmaranTab.CALENDAR -> Calendar(tasks) { showAdd = true }
                SmaranTab.TASKS -> Tasks(tasks, { showAdd = true }) { complete(it, store, scheduler) { refresh++ } }
                SmaranTab.STATS -> Statistics(tasks)
                SmaranTab.SETTINGS -> FunctionalSettings(context)
            }
        }
    }

    if (showAdd) AddTask({ showAdd = false }) { title, description, date, time, category, priority ->
        val id = System.currentTimeMillis()
        store.add(Task(id, title, description, date, time, category, priority))
        scheduler.schedule(id, title, date.atTime(time))
        refresh++
        showAdd = false
    }
}

private fun complete(task: Task, store: TaskStore, scheduler: ReminderScheduler, refresh: () -> Unit) { store.update(task.copy(completed = true)); scheduler.cancel(task.id); refresh() }

@Composable private fun tabIcon(tab: SmaranTab) = when (tab) {
    SmaranTab.HOME -> Icons.Default.Home
    SmaranTab.CALENDAR -> Icons.Default.CalendarMonth
    SmaranTab.TASKS -> Icons.Default.CheckCircle
    SmaranTab.STATS -> Icons.Default.PieChart
    SmaranTab.SETTINGS -> Icons.Default.Settings
}

@Composable private fun Home(tasks: List<Task>, add: () -> Unit, complete: (Task) -> Unit) {
    val today = LocalDate.now(); val todayTasks = tasks.filter { it.date == today }.sortedBy { it.time }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(greeting(), color = Muted); Text("Akash", fontSize = 27.sp, fontWeight = FontWeight.Bold) }; Avatar() } }
        item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Purple), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("The secret of getting ahead is getting started.", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(7.dp)); Text("— Mark Twain", color = Color.White.copy(.75f), fontSize = 12.sp) } } }
        item { Row(verticalAlignment = Alignment.CenterVertically) { Text("Today · ${today.format(DateTimeFormatter.ofPattern("d MMMM"))}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); TextButton(onClick = add) { Text("+ Add", color = Purple) } } }
        if (todayTasks.isEmpty()) item { Empty("No tasks today", "Create your first reminder.", add) }
        items(todayTasks) { TaskCard(it, if (!it.completed) complete else null) }
        item { AddButton(add) }
    }
}

@Composable private fun Calendar(tasks: List<Task>, add: () -> Unit) {
    val today = LocalDate.now(); var selected by remember { mutableStateOf(today) }; val first = today.withDayOfMonth(1).dayOfWeek.value
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Calendar", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        item { Month(first, today.lengthOfMonth(), selected.dayOfMonth) { selected = today.withDayOfMonth(it) } }
        item { Text(selected.format(DateTimeFormatter.ofPattern("d MMMM yyyy")), fontWeight = FontWeight.Bold) }
        val selectedTasks = tasks.filter { it.date == selected }.sortedBy { it.time }
        if (selectedTasks.isEmpty()) item { Empty("Nothing scheduled", "This day is clear.", add) }
        items(selectedTasks) { TaskCard(it, null) }
        item { Button(onClick = add, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Add Task") } }
    }
}

@Composable private fun Month(first: Int, count: Int, selected: Int, select: (Int) -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth()) { listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, color = Muted, fontSize = 10.sp) } }
            Spacer(Modifier.height(8.dp)); val cells = (0 until first - 1) + (1..count).toList()
            cells.chunked(7).forEach { week -> Row(Modifier.fillMaxWidth()) { repeat(7) { i -> val day = week.getOrNull(i) ?: 0; Box(Modifier.weight(1f).padding(vertical = 4.dp), Alignment.Center) { if(day > 0) Box(Modifier.size(34.dp).clip(CircleShape).background(if(day == selected) Purple else Color.Transparent).clickable { select(day) }, Alignment.Center) { Text(day.toString(), color = if(day == selected) Color.White else Color(0xFF171728), fontSize = 12.sp) } } } } }
        }
    }
}

@Composable private fun Tasks(tasks: List<Task>, add: () -> Unit, complete: (Task) -> Unit) {
    var filter by remember { mutableStateOf("All") }; var search by remember { mutableStateOf("") }
    val visible = tasks.filter { it.title.contains(search, true) && when(filter) { "Today" -> it.date == LocalDate.now(); "Upcoming" -> it.date >= LocalDate.now() && !it.completed; "Completed" -> it.completed; else -> true } }.sortedBy { it.date.atTime(it.time) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("All Tasks", fontSize = 24.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(10.dp)); OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search tasks...") }, leadingIcon = { Icon(Icons.Default.Search, null) }) }
        item { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("All","Today","Upcoming","Completed").forEach { value -> item { Chip(value, value == filter) { filter = value } } } } }
        if(visible.isEmpty()) item { Empty("No matching tasks", "Create a new reminder to get started.", add) }
        items(visible) { TaskCard(it, if(!it.completed) complete else null) }
        item { AddButton(add) }
    }
}

@Composable private fun Statistics(tasks: List<Task>) {
    val total = tasks.size; val done = tasks.count { it.completed }; val pending = total - done; val rate = if(total == 0) 0 else done * 100 / total; val streak = streak(tasks)
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Statistics", fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("Your productivity overview", color = Muted, fontSize = 12.sp) }
        item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Navy), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Tasks Completed", color = Color.White.copy(.7f)); Text(done.toString(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold); Text("Completion rate $rate%", color = Color.White.copy(.75f)) } } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Stat("Total", total.toString(), Modifier.weight(1f)); Stat("Completed", done.toString(), Modifier.weight(1f)); Stat("Pending", pending.toString(), Modifier.weight(1f)) } }
        item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Current Streak 🔥", fontWeight = FontWeight.Bold); Text("$streak days", color = Purple, fontSize = 29.sp, fontWeight = FontWeight.Bold); Text("Consecutive days with completed tasks.", color = Muted, fontSize = 12.sp) } } }
        item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Task History", fontWeight = FontWeight.Bold); tasks.sortedByDescending { it.date.atTime(it.time) }.take(10).forEach { Text("${it.date} · ${it.title} · ${if(it.completed) "Done" else "Pending"}", color = if(it.completed) Green else Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 9.dp)) } } } }
    }
}

@Composable private fun Stat(label: String, value: String, modifier: Modifier) { Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(14.dp)) { Text(label, color = Muted, fontSize = 11.sp); Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold) } } }

/* Legacy Settings composable retained intentionally for compatibility; SmaranApp now uses the functional settings module. */
@Composable private fun Settings() { LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { Text("Settings", fontSize = 27.sp, fontWeight = FontWeight.Bold) }; item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Avatar(); Spacer(Modifier.width(14.dp)); Column { Text("Akash", fontWeight = FontWeight.Bold); Text("Local Smaran profile", color = Muted, fontSize = 12.sp) } } } }; item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Column { listOf("General","Reminders","Appearance","Backup & Restore","Data & Storage","Notifications","Privacy","About Smaran").forEach { label -> Row(Modifier.fillMaxWidth().clickable { }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Settings, null, tint = Muted); Spacer(Modifier.width(14.dp)); Text(label, Modifier.weight(1f)); Text("›", color = Muted, fontSize = 22.sp) } } } } } } }

@Composable private fun Avatar() { Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFEDE5FF)), Alignment.Center) { Text("A", color = Purple, fontWeight = FontWeight.Bold, fontSize = 20.sp) } }
@Composable private fun TaskCard(task: Task, complete: ((Task) -> Unit)?) { val color = when (task.category) { "Work" -> Red; "Study" -> Purple; "Health" -> Green; else -> Orange }; Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.width(3.dp).height(45.dp).background(color)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(task.title, fontWeight = FontWeight.SemiBold, color = if(task.completed) Muted else Color(0xFF171728)); Text("${task.date} · ${task.time.format(DateTimeFormatter.ofPattern("hh:mm a"))}", color = Muted, fontSize = 11.sp); Spacer(Modifier.height(4.dp)); Chip(text = task.category, selected = false, onClick = {}) }; if(complete != null) Icon(Icons.Default.CheckCircle, "Complete", tint = Purple, modifier = Modifier.size(25.dp).clickable { complete(task) }) else if(task.completed) Icon(Icons.Default.Check, "Completed", tint = Green) } } }
@Composable private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) { Box(Modifier.clip(RoundedCornerShape(20.dp)).background(if(selected) Purple else Color(0xFFF1EFF7)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 7.dp)) { Text(text, color = if(selected) Color.White else Color(0xFF171728), fontSize = 10.sp, fontWeight = FontWeight.SemiBold) } }
@Composable private fun AddButton(add: () -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { Button(onClick = add, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Purple), contentPadding = PaddingValues(0.dp), modifier = Modifier.size(54.dp)) { Icon(Icons.Default.Add, "Add task") } } }
@Composable private fun Empty(title: String, body: String, add: () -> Unit) { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.AccessTime, null, tint = Purple); Spacer(Modifier.height(8.dp)); Text(title, fontWeight = FontWeight.Bold); Text(body, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center); Spacer(Modifier.height(10.dp)); OutlinedButton(onClick = add) { Text("Create task") } } } }

@Composable private fun AddTask(onDismiss: () -> Unit, create: (String, String, LocalDate, LocalTime, String, Priority) -> Unit) { val context = LocalContext.current; var title by remember { mutableStateOf("") }; var description by remember { mutableStateOf("") }; var date by remember { mutableStateOf(LocalDate.now()) }; var time by remember { mutableStateOf(LocalTime.now().plusMinutes(5).withSecond(0).withNano(0)) }; var category by remember { mutableStateOf("Personal") }; var priority by remember { mutableStateOf(Priority.MEDIUM) }; AlertDialog(onDismissRequest = onDismiss, title = { Text("New Task") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Task title") }); OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Description") }); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(modifier = Modifier.weight(1f), onClick = { DatePickerDialog(context, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) }, date.year, date.monthValue - 1, date.dayOfMonth).show() }) { Text(date.format(DateTimeFormatter.ofPattern("dd MMM"))) }; OutlinedButton(modifier = Modifier.weight(1f), onClick = { TimePickerDialog(context, { _, h, m -> time = LocalTime.of(h, m) }, time.hour, time.minute, false).show() }) { Text(time.format(DateTimeFormatter.ofPattern("hh:mm a"))) } }; LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Personal","Work","Study","Health").forEach { value -> item { Chip(text = value, selected = value == category, onClick = { category = value }) } } }; LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Priority.entries.forEach { value -> item { Chip(text = value.name.lowercase().replaceFirstChar { it.uppercase() }, selected = value == priority, onClick = { priority = value }) } } } } }, confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = { create(title.trim(), description.trim(), date, time, category, priority) }) { Text("Create", color = Purple) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }) }

@Composable private fun Onboarding(done: () -> Unit) { var page by rememberSaveable { mutableIntStateOf(0) }; val titles = listOf("Welcome to Smaran","Plan Your Tasks","Smart Reminders","Track & Improve","Let's Achieve More"); val bodies = listOf("Your intelligent reminder companion.","Create tasks with date & time and never miss what matters.","Get reminders with 15 min, 30 min and 1 hour snooze actions.","View history, statistics and build better habits.","Stay focused, stay productive and achieve your goals."); Column(Modifier.fillMaxSize().background(Navy).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = done) { Text("Skip", color = Color.White) } }; Spacer(Modifier.height(60.dp)); Text(listOf("⌛","✓","🔔","▣","✓")[page], fontSize = 64.sp); Spacer(Modifier.height(30.dp)); Text(titles[page], color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center); Spacer(Modifier.height(12.dp)); Text(bodies[page], color = Color.White.copy(.8f), textAlign = TextAlign.Center); Spacer(Modifier.weight(1f)); Button(onClick = { if(page == 4) done() else page++ }, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9BFFF))) { Text(if(page == 4) "Get Started →" else "Next", color = Navy, fontWeight = FontWeight.Bold) } } }
private fun greeting() = when(LocalTime.now().hour) { in 0..11 -> "Good Morning, 👋"; in 12..16 -> "Good Afternoon, 👋"; else -> "Good Evening, 👋" }
private fun streak(tasks: List<Task>): Int { var date = LocalDate.now(); val done = tasks.filter { it.completed }.map { it.date }.toSet(); var count = 0; while(done.contains(date)) { count++; date = date.minusDays(1) }; return count }
