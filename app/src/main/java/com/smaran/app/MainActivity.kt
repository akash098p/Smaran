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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smaran.app.data.local.TaskStore
import com.smaran.app.data.model.Priority
import com.smaran.app.data.model.Task
import com.smaran.app.reminder.scheduler.ReminderScheduler
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val Purple = Color(0xFF5B2BD9)
private val PurpleDark = Color(0xFF151733)
private val PurpleSoft = Color(0xFFEDE5FF)
private val Background = Color(0xFFF9F8FD)
private val TextPrimary = Color(0xFF171728)
private val TextSecondary = Color(0xFF747384)
private val Green = Color(0xFF27A96B)
private val Orange = Color(0xFFFF9D25)
private val Red = Color(0xFFF04C5C)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        setContent { SmaranApp(applicationContext) }
    }
}

enum class MainPage(val label: String) { HOME("Home"), CALENDAR("Calendar"), TASKS("All Tasks"), STATS("Statistics"), SETTINGS("Settings") }

data class TaskUi(val task: Task, val color: Color)

@Composable
fun SmaranApp(context: Context) {
    val prefs = remember { context.getSharedPreferences("smaran_app", Context.MODE_PRIVATE) }
    var onboarding by rememberSaveable { mutableStateOf(!prefs.getBoolean("onboarding_done", false)) }
    var selectedPage by rememberSaveable { mutableIntStateOf(0) }
    var refresh by remember { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    val store = remember { TaskStore(context) }
    val scheduler = remember { ReminderScheduler(context) }
    val tasks = remember(refresh) { store.getTasks() }

    Surface(modifier = Modifier.fillMaxSize(), color = Background) {
        if (onboarding) {
            OnboardingScreen(onFinish = {
                prefs.edit().putBoolean("onboarding_done", true).apply()
                onboarding = false
            })
        } else {
            Scaffold(
                containerColor = Background,
                bottomBar = {
                    NavigationBar(containerColor = Color.White) {
                        MainPage.entries.forEachIndexed { index, page ->
                            NavigationBarItem(
                                selected = selectedPage == index,
                                onClick = { selectedPage = index },
                                icon = { Icon(pageIcon(page), page.label) },
                                label = { Text(page.label, fontSize = 9.sp) }
                            )
                        }
                    }
                }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (MainPage.entries[selectedPage]) {
                        MainPage.HOME -> HomeScreen(tasks, onAdd = { showAdd = true }, onComplete = { completeTask(it, store, scheduler) { refresh++ } })
                        MainPage.CALENDAR -> CalendarScreen(tasks, onAdd = { showAdd = true })
                        MainPage.TASKS -> TasksScreen(tasks, onComplete = { completeTask(it, store, scheduler) { refresh++ } }, onAdd = { showAdd = true })
                        MainPage.STATS -> StatisticsScreen(tasks)
                        MainPage.SETTINGS -> SettingsScreen()
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddTaskDialog(
            onDismiss = { showAdd = false },
            onCreate = { title, description, date, time, category, priority ->
                val id = System.currentTimeMillis()
                val task = Task(id, title, description, date, time, category, priority)
                store.add(task)
                scheduler.schedule(id, title, date.atTime(time))
                refresh++
                showAdd = false
            }
        )
    }
}

private fun completeTask(task: Task, store: TaskStore, scheduler: ReminderScheduler, refresh: () -> Unit) {
    store.update(task.copy(completed = true))
    scheduler.cancel(task.id)
    refresh()
}

@Composable
private fun pageIcon(page: MainPage) = when (page) {
    MainPage.HOME -> Icons.Default.Home
    MainPage.CALENDAR -> Icons.Default.CalendarMonth
    MainPage.TASKS -> Icons.Default.CheckCircle
    MainPage.STATS -> Icons.Default.PieChart
    MainPage.SETTINGS -> Icons.Default.Settings
}

@Composable
private fun HomeScreen(tasks: List<Task>, onAdd: () -> Unit, onComplete: (Task) -> Unit) {
    val today = LocalDate.now()
    val todayTasks = tasks.filter { it.date == today }.sortedBy { it.time }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(greeting(), color = TextSecondary, fontSize = 14.sp)
                    Text("Akash", color = TextPrimary, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                }
                Box(Modifier.size(48.dp).clip(CircleShape).background(PurpleSoft), contentAlignment = Alignment.Center) { Text("A", color = Purple, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Purple), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Text("The secret of getting ahead is getting started.", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp)); Text("— Mark Twain", color = Color.White.copy(.78f), fontSize = 12.sp)
                }
            }
        }
        item { Row(verticalAlignment = Alignment.CenterVertically) { Text("Today · ${today.format(DateTimeFormatter.ofPattern("d MMMM"))}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); TextButton(onClick = onAdd) { Text("+ Add", color = Purple) } } }
        if (todayTasks.isEmpty()) item { EmptyState("No tasks today", "Create a reminder and Smaran will alert you on time.", onAdd) }
        items(todayTasks) { TaskCard(it, onComplete) }
        item { FloatingAdd(onAdd) }
    }
}

@Composable
private fun CalendarScreen(tasks: List<Task>, onAdd: () -> Unit) {
    val today = LocalDate.now(); var selected by remember { mutableStateOf(today) }
    val first = today.withDayOfMonth(1).dayOfWeek.value; val count = today.lengthOfMonth()
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Calendar", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        item { CalendarCard(first, count, selected.dayOfMonth) { selected = today.withDayOfMonth(it) } }
        item { Row(verticalAlignment = Alignment.CenterVertically) { Text(selected.format(DateTimeFormatter.ofPattern("d MMMM yyyy")), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("${tasks.count { it.date == selected }} tasks", color = TextSecondary) } }
        val dayTasks = tasks.filter { it.date == selected }.sortedBy { it.time }
        if (dayTasks.isEmpty()) item { EmptyState("Nothing scheduled", "Your selected day is clear.", onAdd) }
        items(dayTasks) { TaskCard(it, null) }
        item { Button(onClick = onAdd, Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Add Task") } }
    }
}

@Composable
private fun CalendarCard(firstDay: Int, days: Int, selected: Int, onSelect: (Int) -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth()) { listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, color = TextSecondary, fontSize = 10.sp) } }
            Spacer(Modifier.height(8.dp)); val cells = (0 until firstDay - 1) + (1..days).toList()
            cells.chunked(7).forEach { week -> Row(Modifier.fillMaxWidth()) { repeat(7) { i -> val day = week.getOrNull(i) ?: 0; Box(Modifier.weight(1f).padding(vertical = 4.dp), contentAlignment = Alignment.Center) { if (day > 0) Box(Modifier.size(34.dp).clip(CircleShape).background(if (day == selected) Purple else Color.Transparent).clickable { onSelect(day) }, Alignment.Center) { Text(day.toString(), color = if (day == selected) Color.White else TextPrimary, fontSize = 12.sp) } } } } }
        }
    }
}

@Composable
private fun TasksScreen(tasks: List<Task>, onComplete: (Task) -> Unit, onAdd: () -> Unit) {
    var filter by remember { mutableStateOf("All") }; var search by remember { mutableStateOf("") }
    val visible = tasks.filter { task ->
        val matches = task.title.contains(search, true)
        matches && when (filter) { "Today" -> task.date == LocalDate.now(); "Upcoming" -> task.date >= LocalDate.now() && !task.completed; "Completed" -> task.completed; else -> true }
    }.sortedBy { it.date.atTime(it.time) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("All Tasks", fontSize = 24.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search tasks...") }, leadingIcon = { Icon(Icons.Default.Search, null) }) }
        item { LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("All","Today","Upcoming","Completed").forEach { f -> item { FilterChip(f, f == filter) { filter = f } } } } }
        if (visible.isEmpty()) item { EmptyState("No matching tasks", "Try another filter or create a new task.", onAdd) }
        items(visible) { TaskCard(it, if (!it.completed) onComplete else null) }
        item { FloatingAdd(onAdd) }
    }
}

@Composable
private fun StatisticsScreen(tasks: List<Task>) {
    val completed = tasks.count { it.completed }; val pending = tasks.count { !it.completed }; val total = tasks.size
    val rate = if (total == 0) 0 else completed * 100 / total
    val streak = calculateStreak(tasks)
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Statistics", fontSize = 24.sp, fontWeight = FontWeight.Bold); Text("This Week", color = TextSecondary, fontSize = 12.sp) }
        item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = PurpleDark), Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Tasks Completed", color = Color.White.copy(.7f)); Text(completed.toString(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold); Text("Keep building your consistency.", color = Color.White.copy(.75f), fontSize = 12.sp) } } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { StatBox("Total", total.toString(), Modifier.weight(1f)); StatBox("Completed", completed.toString(), Modifier.weight(1f)); StatBox("Pending", pending.toString(), Modifier.weight(1f)) } }
        item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Completion Rate", fontWeight = FontWeight.Bold); Text("$rate%", fontSize = 34.sp, color = Purple, fontWeight = FontWeight.Bold); Text("${completed} of ${total} tasks completed", color = TextSecondary, fontSize = 12.sp) } } }
        item { Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Current Streak 🔥", fontWeight = FontWeight.Bold); Text("$streak days", color = Purple, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("Complete tasks consistently to grow your streak.", color = TextSecondary, fontSize = 12.sp) } } }
    }
}

@Composable private fun StatBox(label: String, value: String, modifier: Modifier) { Card(modifier, RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(14.dp)) { Text(label, color = TextSecondary, fontSize = 11.sp); Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold) } } }

@Composable
private fun SettingsScreen() {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 18.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Settings", fontSize = 27.sp, fontWeight = FontWeight.Bold) }
        item { Card(RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(52.dp).clip(CircleShape).background(PurpleSoft), Alignment.Center) { Text("A", color = Purple, fontWeight = FontWeight.Bold, fontSize = 20.sp) }; Spacer(Modifier.width(14.dp)); Column { Text("Akash", fontWeight = FontWeight.Bold); Text("Local Smaran profile", color = TextSecondary, fontSize = 12.sp) } } } }
        item { SettingsGroup(listOf("General", "Reminders", "Appearance", "Backup & Restore", "Data & Storage", "Notifications", "Privacy", "About Smaran")) }
    }
}

@Composable private fun SettingsGroup(items: List<String>) { Card(RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column { items.forEachIndexed { i, label -> Row(Modifier.fillMaxWidth().clickable { }.padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (label == "Reminders" || label == "Notifications") Icons.Default.NotificationsNone else Icons.Default.Settings, null, tint = TextSecondary); Spacer(Modifier.width(14.dp)); Text(label, fontWeight = FontWeight.Medium, Modifier.weight(1f)); Text("›", color = TextSecondary, fontSize = 22.sp) }; if (i < items.lastIndex) Spacer(Modifier.height(1.dp)) } } } }

@Composable private fun TaskCard(task: Task, onComplete: ((Task) -> Unit)?) { val color = categoryColor(task.category); Card(RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), Modifier.fillMaxWidth()) { Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.width(3.dp).height(45.dp).clip(RoundedCornerShape(4.dp)).background(color)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(task.title, fontWeight = FontWeight.SemiBold, color = if (task.completed) TextSecondary else TextPrimary, fontSize = 14.sp); Text("${task.date} · ${task.time.format(DateTimeFormatter.ofPattern("hh:mm a"))}", color = TextSecondary, fontSize = 11.sp); Spacer(Modifier.height(4.dp)); Tag(task.category, color) }; if (onComplete != null) Icon(Icons.Default.CheckCircle, "Complete", tint = Purple, Modifier.size(25.dp).clickable { onComplete(task) }) else if (task.completed) Icon(Icons.Default.Check, "Completed", tint = Green) } } }

@Composable private fun Tag(text: String, color: Color) { Box(Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(.1f)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) } }
@Composable private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) { Box(Modifier.clip(RoundedCornerShape(20.dp)).background(if (selected) Purple else Color.White).clickable { onClick() }.padding(horizontal = 14.dp, vertical = 9.dp)) { Text(text, color = if (selected) Color.White else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) } }
@Composable private fun FloatingAdd(onAdd: () -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { Button(onClick = onAdd, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Purple), contentPadding = PaddingValues(0.dp), Modifier.size(54.dp)) { Icon(Icons.Default.Add, "Add task") } } }
@Composable private fun EmptyState(title: String, body: String, onAdd: () -> Unit) { Card(RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.AccessTime, null, tint = Purple, Modifier.size(30.dp)); Spacer(Modifier.height(8.dp)); Text(title, fontWeight = FontWeight.Bold); Text(body, color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center); Spacer(Modifier.height(12.dp)); OutlinedButton(onClick = onAdd) { Text("Create task") } } } }

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onCreate: (String, String, LocalDate, LocalTime, String, Priority) -> Unit) {
    var title by remember { mutableStateOf("") }; var description by remember { mutableStateOf("") }; var category by remember { mutableStateOf("Personal") }; var priority by remember { mutableStateOf(Priority.MEDIUM) }; var date by remember { mutableStateOf(LocalDate.now()) }; var time by remember { mutableStateOf(LocalTime.now().plusMinutes(5).withSecond(0).withNano(0)) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("New Task") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Task title") })
        OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Description (optional)") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { DatePickerDialog(nullContext(), {}, 0,0,0) }) { Text("Date") }; OutlinedButton(onClick = { }) { Text(time.format(DateTimeFormatter.ofPattern("hh:mm a"))) } }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) { listOf("Personal","Work","Study","Health").forEach { c -> item { FilterChip(c, c == category) { category = c } } } }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) { Priority.entries.forEach { p -> item { FilterChip(p.name.lowercase().replaceFirstChar { it.uppercase() }, p == priority) { priority = p } } } }
        Text("Reminder will use the selected date and time.", color = TextSecondary, fontSize = 11.sp)
    } }, confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = { onCreate(title.trim(), description.trim(), date, time, category, priority) }) { Text("Create", color = Purple) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

private fun nullContext(): Context? = null
private fun greeting(): String { val h = java.time.LocalTime.now().hour; return when { h < 12 -> "Good Morning, 👋"; h < 17 -> "Good Afternoon, 👋"; else -> "Good Evening, 👋" } }
private fun categoryColor(category: String) = when (category) { "Work" -> Red; "Study" -> Purple; "Health" -> Green; else -> Orange }
private fun calculateStreak(tasks: List<Task>): Int { var day = LocalDate.now(); var streak = 0; val completedDates = tasks.filter { it.completed }.map { it.date }.toSet(); while (completedDates.contains(day)) { streak++; day = day.minusDays(1) }; return streak }

@Composable
private fun OnboardingScreen(onFinish: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf("Welcome to Smaran", "Plan Your Tasks", "Smart Reminders", "Track & Improve", "Let's Achieve More")
    val bodies = listOf("Your intelligent reminder companion.", "Create tasks with date & time and never miss what matters.", "Get reminded on time with quick snooze options.", "View history, statistics and build better habits every day.", "Stay focused, stay productive and achieve your goals.")
    val icons = listOf("⌛", "✓", "🔔", "▣", "✓")
    Column(Modifier.fillMaxSize().background(PurpleDark).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onFinish) { Text("Skip", color = Color.White) } }
        Spacer(Modifier.height(70.dp)); Text(icons[page], fontSize = 68.sp); Spacer(Modifier.height(34.dp)); Text(titles[page], color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center); Spacer(Modifier.height(12.dp)); Text(bodies[page], color = Color.White.copy(.8f), fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(Modifier.weight(1f)); Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { repeat(titles.size) { Box(Modifier.size(if (it == page) 9.dp else 7.dp).clip(CircleShape).background(if (it == page) Color.White else Color.White.copy(.25f))) } }; Spacer(Modifier.height(24.dp)); Button(onClick = { if (page == titles.lastIndex) onFinish() else page++ }, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9BFFF))) { Text(if (page == titles.lastIndex) "Get Started →" else "Next", color = PurpleDark, fontWeight = FontWeight.Bold) }
    }
}
