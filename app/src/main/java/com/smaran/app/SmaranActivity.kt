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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.smaran.app.data.local.TaskStore
import com.smaran.app.data.model.Priority
import com.smaran.app.data.model.Task
import com.smaran.app.reminder.scheduler.ReminderScheduler
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val SPrimary = Color(0xFF5B2BD9)
private val SDeep = Color(0xFF151733)
private val SBackground = Color(0xFFF9F8FD)
private val SText = Color(0xFF171728)
private val SMuted = Color(0xFF747384)
private val SGreen = Color(0xFF27A96B)
private val SOrange = Color(0xFFFF9D25)
private val SRed = Color(0xFFF04C5C)

enum class SmaranTab(val title: String) { HOME("Home"), CALENDAR("Calendar"), TASKS("All Tasks"), STATS("Statistics"), SETTINGS("Settings") }

class SmaranActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 41)
        setContent { SmaranRoot(applicationContext) }
    }
}

@Composable
private fun SmaranRoot(context: Context) {
    val prefs = remember { context.getSharedPreferences("smaran_app", Context.MODE_PRIVATE) }
    var onboarding by rememberSaveable { mutableStateOf(!prefs.getBoolean("onboarding_done", false)) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var addTask by remember { mutableStateOf(false) }
    var version by remember { mutableIntStateOf(0) }
    val store = remember { TaskStore(context) }
    val scheduler = remember { ReminderScheduler(context) }
    val tasks = remember(version) { store.getTasks() }

    if (onboarding) {
        SmaranOnboarding(onFinish = { prefs.edit().putBoolean("onboarding_done", true).apply(); onboarding = false })
        return
    }

    Scaffold(
        containerColor = SBackground,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                SmaranTab.entries.forEachIndexed { i, item ->
                    NavigationBarItem(i == tab, { tab = i }, icon = { Icon(smaranIcon(item), item.title) }, label = { Text(item.title, fontSize = 9.sp) })
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (SmaranTab.entries[tab]) {
                SmaranTab.HOME -> SmaranHome(tasks, { addTask = true }) { complete(it, store, scheduler) { version++ } }
                SmaranTab.CALENDAR -> SmaranCalendar(tasks) { addTask = true }
                SmaranTab.TASKS -> SmaranTasks(tasks, { addTask = true }) { complete(it, store, scheduler) { version++ } }
                SmaranTab.STATS -> SmaranStats(tasks)
                SmaranTab.SETTINGS -> SmaranSettings()
            }
        }
    }

    if (addTask) SmaranAddTask(onDismiss = { addTask = false }) { title, description, date, time, category, priority ->
        val id = System.currentTimeMillis()
        store.add(Task(id, title, description, date, time, category, priority))
        scheduler.schedule(id, title, date.atTime(time))
        version++
        addTask = false
    }
}

private fun complete(task: Task, store: TaskStore, scheduler: ReminderScheduler, refresh: () -> Unit) {
    store.update(task.copy(completed = true)); scheduler.cancel(task.id); refresh()
}

@Composable private fun smaranIcon(tab: SmaranTab) = when (tab) {
    SmaranTab.HOME -> Icons.Default.Home
    SmaranTab.CALENDAR -> Icons.Default.CalendarMonth
    SmaranTab.TASKS -> Icons.Default.CheckCircle
    SmaranTab.STATS -> Icons.Default.PieChart
    SmaranTab.SETTINGS -> Icons.Default.Settings
}

@Composable private fun SmaranHome(tasks: List<Task>, onAdd: () -> Unit, onComplete: (Task) -> Unit) {
    val today = LocalDate.now(); val todayTasks = tasks.filter { it.date == today }.sortedBy { it.time }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { Column(Modifier.weight(1f)) { Text(smaranGreeting(), color = SMuted); Text("Akash", fontSize = 27.sp, fontWeight = FontWeight.Bold) }; Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFEDE5FF)), Alignment.Center) { Text("A", color = SPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp) } } }
        item { Card(RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SPrimary), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("The secret of getting ahead is getting started.", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(8.dp)); Text("— Mark Twain", color = Color.White.copy(.75f), fontSize = 12.sp) } } }
        item { Row(verticalAlignment = Alignment.CenterVertically) { Text("Today · ${today.format(DateTimeFormatter.ofPattern("d MMMM"))}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); TextButton(onClick = onAdd) { Text("+ Add", color = SPrimary) } } }
        if (todayTasks.isEmpty()) item { SmaranEmpty("No tasks today", "Create your first reminder.", onAdd) }
        items(todayTasks) { SmaranTaskCard(it, if (!it.completed) onComplete else null) }
        item { SmaranFab(onAdd) }
    }
}

@Composable private fun SmaranCalendar(tasks: List<Task>, onAdd: () -> Unit) {
    val today = LocalDate.now(); var selected by remember { mutableStateOf(today) }; val first = today.withDayOfMonth(1).dayOfWeek.value
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Calendar", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        item { SmaranMonth(first, today.lengthOfMonth(), selected.dayOfMonth) { selected = today.withDayOfMonth(it) } }
        item { Text(selected.format(DateTimeFormatter.ofPattern("d MMMM yyyy")), fontWeight = FontWeight.Bold) }
        val dayTasks = tasks.filter { it.date == selected }.sortedBy { it.time }
        if (dayTasks.isEmpty()) item { SmaranEmpty("Nothing scheduled", "This day is clear.", onAdd) }
        items(dayTasks) { SmaranTaskCard(it, null) }
        item { Button(onClick = onAdd, Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = SPrimary)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Add Task") } }
    }
}

@Composable private fun SmaranMonth(first: Int, count: Int, selected: Int, onSelect: (Int) -> Unit) {
    Card(RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(14.dp)) {
        Row(Modifier.fillMaxWidth()) { listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, color = SMuted, fontSize = 10.sp) } }
        Spacer(Modifier.height(8.dp)); val cells = (0 until first - 1) + (1..count).toList()
        cells.chunked(7).forEach { week -> Row(Modifier.fillMaxWidth()) { repeat(7) { i -> val day = week.getOrNull(i) ?: 0; Box(Modifier.weight(1f).padding(vertical = 4.dp), Alignment.Center) { if (day > 0) Box(Modifier.size(34.dp).clip(CircleShape).background(if (day == selected) SPrimary else Color.Transparent).clickable { onSelect(day) }, Alignment.Center) { Text(day.toString(), color = if (day == selected) Color.White else SText, fontSize = 12.sp) } } } } }
    } }
}

@Composable private fun SmaranTasks(tasks: List<Task>, onAdd: () -> Unit, onComplete: (Task) -> Unit) {
    var filter by remember { mutableStateOf("All") }; var search by remember { mutableStateOf("") }
    val visible = tasks.filter { it.title.contains(search, true) && when(filter) { "Today" -> it.date == LocalDate.now(); "Upcoming" -> it.date >= LocalDate.now() && !it.completed; "Completed" -> it.completed; else -> true } }.sortedBy { it.date.atTime(it.time) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("All Tasks", fontSize = 24.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search tasks...") }, leadingIcon = { Icon(Icons.Default.Search, null) }) }
        item { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("All","Today","Upcoming","Completed").forEach { f -> item { SmaranChip(f, f == filter) { filter = f } } } } }
        if (visible.isEmpty()) item { SmaranEmpty("No matching tasks", "Create a new reminder to get started.", onAdd) }
        items(visible) { SmaranTaskCard(it, if (!it.completed) onComplete else null) }
        item { SmaranFab(onAdd) }
    }
}

@Composable private fun SmaranStats(tasks: List<Task>) {
    val total = tasks.size; val completed = tasks.count { it.completed }; val pending = total - completed; val rate = if(total == 0) 0 else completed * 100 / total; val streak = smaranStreak(tasks)
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Statistics", fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("Your productivity overview", color = SMuted, fontSize = 12.sp) }
        item { Card(RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = SDeep), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Tasks Completed", color = Color.White.copy(.7f)); Text(completed.toString(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold); Text("Completion rate $rate%", color = Color.White.copy(.75f)) } } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { SmaranStatBox("Total", total.toString(), Modifier.weight(1f)); SmaranStatBox("Completed", completed.toString(), Modifier.weight(1f)); SmaranStatBox("Pending", pending.toString(), Modifier.weight(1f)) } }
        item { Card(RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Current Streak 🔥", fontWeight = FontWeight.Bold); Text("$streak days", color = SPrimary, fontSize = 29.sp, fontWeight = FontWeight.Bold); Text("Complete tasks on consecutive days to build your streak.", color = SMuted, fontSize = 12.sp) } } }
        item { Card(RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("History", fontWeight = FontWeight.Bold); tasks.sortedByDescending { it.date.atTime(it.time) }.take(8).forEach { Text("${it.date}  ·  ${it.title}  ·  ${if(it.completed) "Done" else "Pending"}", color = if(it.completed) SGreen else SMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 9.dp)) } } } }
    }
}

@Composable private fun SmaranStatBox(label: String, value: String, modifier: Modifier) { Card(modifier, RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(14.dp)) { Text(label, color = SMuted, fontSize = 11.sp); Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold) } } }
@Composable private fun SmaranSettings() { LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { Text("Settings", fontSize = 27.sp, fontWeight = FontWeight.Bold) }; item { Card(RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFEDE5FF)), Alignment.Center) { Text("A", color = SPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp) }; Spacer(Modifier.width(14.dp)); Column { Text("Akash", fontWeight = FontWeight.Bold); Text("Local Smaran profile", color = SMuted, fontSize = 12.sp) } } } }; item { Card(RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column { listOf("General","Reminders","Appearance","Backup & Restore","Data & Storage","Notifications","Privacy","About Smaran").forEach { label -> Row(Modifier.fillMaxWidth().clickable { }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Settings, null, tint = SMuted); Spacer(Modifier.width(14.dp)); Text(label, Modifier.weight(1f), fontWeight = FontWeight.Medium); Text("›", color = SMuted, fontSize = 22.sp) } } } } } } }

@Composable private fun SmaranTaskCard(task: Task, onComplete: ((Task) -> Unit)?) { val color = when(task.category) { "Work" -> SRed; "Study" -> SPrimary; "Health" -> SGreen; else -> SOrange }; Card(RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.width(3.dp).height(45.dp).clip(RoundedCornerShape(4.dp)).background(color)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(task.title, fontWeight = FontWeight.SemiBold, color = if(task.completed) SMuted else SText, fontSize = 14.sp); Text("${task.date} · ${task.time.format(DateTimeFormatter.ofPattern("hh:mm a"))}", color = SMuted, fontSize = 11.sp); Spacer(Modifier.height(4.dp)); SmaranChip(task.category, false) { } }; if(onComplete != null) Icon(Icons.Default.CheckCircle, "Complete", tint = SPrimary, modifier = Modifier.size(25.dp).clickable { onComplete(task) }) else if(task.completed) Icon(Icons.Default.Check, "Completed", tint = SGreen) } } }
@Composable private fun SmaranChip(text: String, selected: Boolean, onClick: () -> Unit) { Box(Modifier.clip(RoundedCornerShape(20.dp)).background(if(selected) SPrimary else Color(0xFFF1EFF7)).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 7.dp)) { Text(text, color = if(selected) Color.White else SText, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) } }
@Composable private fun SmaranFab(onAdd: () -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { Button(onClick = onAdd, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = SPrimary), contentPadding = PaddingValues(0.dp), modifier = Modifier.size(54.dp)) { Icon(Icons.Default.Add, "Add task") } } }
@Composable private fun SmaranEmpty(title: String, body: String, onAdd: () -> Unit) { Card(RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.AccessTime, null, tint = SPrimary); Spacer(Modifier.height(8.dp)); Text(title, fontWeight = FontWeight.Bold); Text(body, color = SMuted, fontSize = 12.sp, textAlign = TextAlign.Center); Spacer(Modifier.height(10.dp)); OutlinedButton(onClick = onAdd) { Text("Create task") } } } }

@Composable private fun SmaranAddTask(onDismiss: () -> Unit, onCreate: (String, String, LocalDate, LocalTime, String, Priority) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }; var description by remember { mutableStateOf("") }; var category by remember { mutableStateOf("Personal") }; var priority by remember { mutableStateOf(Priority.MEDIUM) }; var date by remember { mutableStateOf(LocalDate.now()) }; var time by remember { mutableStateOf(LocalTime.now().plusMinutes(5).withSecond(0).withNano(0)) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("New Task") }, text = { Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Task title") })
        OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Description") })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(Modifier.weight(1f), onClick = { DatePickerDialog(context, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) }, date.year, date.monthValue - 1, date.dayOfMonth).show() }) { Text(date.format(DateTimeFormatter.ofPattern("dd MMM"))) }
            OutlinedButton(Modifier.weight(1f), onClick = { TimePickerDialog(context, { _, h, m -> time = LocalTime.of(h, m) }, time.hour, time.minute, false).show() }) { Text(time.format(DateTimeFormatter.ofPattern("hh:mm a"))) }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Personal","Work","Study","Health").forEach { c -> item { SmaranChip(c, c == category) { category = c } } } }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { Priority.entries.forEach { p -> item { SmaranChip(p.name.lowercase().replaceFirstChar { it.uppercase() }, p == priority) { priority = p } } } }
        Text("Reminder: ${date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))} at ${time.format(DateTimeFormatter.ofPattern("hh:mm a"))}", color = SMuted, fontSize = 11.sp)
    } }, confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = { onCreate(title.trim(), description.trim(), date, time, category, priority) }) { Text("Create", color = SPrimary) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable private fun SmaranOnboarding(onFinish: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }; val titles = listOf("Welcome to Smaran","Plan Your Tasks","Smart Reminders","Track & Improve","Let's Achieve More"); val bodies = listOf("Your intelligent reminder companion.","Create tasks with date & time and never miss what matters.","Get reminders on time with 15 min, 30 min and 1 hour snooze actions.","View history, statistics and build better habits every day.","Stay focused, stay productive and achieve your goals."); val icons = listOf("⌛","✓","🔔","▣","✓")
    Column(Modifier.fillMaxSize().background(SDeep).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onFinish) { Text("Skip", color = Color.White) } }; Spacer(Modifier.height(60.dp)); Text(icons[page], fontSize = 64.sp); Spacer(Modifier.height(30.dp)); Text(titles[page], color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center); Spacer(Modifier.height(12.dp)); Text(bodies[page], color = Color.White.copy(.8f), textAlign = TextAlign.Center, lineHeight = 22.sp); Spacer(Modifier.weight(1f)); Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { repeat(5) { Box(Modifier.size(if(it == page) 9.dp else 7.dp).clip(CircleShape).background(if(it == page) Color.White else Color.White.copy(.25f))) } }; Spacer(Modifier.height(22.dp)); Button(onClick = { if(page == 4) onFinish() else page++ }, Modifier.fillMaxWidth().height(54.dp), RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9BFFF))) { Text(if(page == 4) "Get Started →" else "Next", color = SDeep, fontWeight = FontWeight.Bold) } }
}

private fun smaranGreeting(): String { val h = LocalTime.now().hour; return when { h < 12 -> "Good Morning, 👋"; h < 17 -> "Good Afternoon, 👋"; else -> "Good Evening, 👋" } }
private fun smaranStreak(tasks: List<Task>): Int { var day = LocalDate.now(); val done = tasks.filter { it.completed }.map { it.date }.toSet(); var count = 0; while(done.contains(day)) { count++; day = day.minusDays(1) }; return count }
