package com.smaran.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

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
        setContent { SmaranApp() }
    }
}

enum class MainPage(val label: String) {
    HOME("Home"), CALENDAR("Calendar"), TASKS("All Tasks"), STATS("Statistics"), SETTINGS("Settings")
}

data class TaskUi(
    val title: String,
    val time: String,
    val category: String,
    val color: Color,
    val done: Boolean = false
)

private val sampleTasks = listOf(
    TaskUi("Complete UI Design", "10:00 AM", "Work", Red),
    TaskUi("Study Kotlin Coroutines", "01:30 PM", "Study", Purple),
    TaskUi("Gym Workout", "06:00 PM", "Health", Green),
    TaskUi("Read Atomic Habits", "09:30 PM", "Personal", Orange)
)

@Composable
fun SmaranApp() {
    var showOnboarding by rememberSaveable { mutableStateOf(true) }
    var selectedPage by rememberSaveable { mutableIntStateOf(0) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            if (showOnboarding) {
                OnboardingScreen(onFinish = { showOnboarding = false })
            } else {
                MainShell(
                    selectedPage = selectedPage,
                    onPageSelected = { selectedPage = it }
                )
            }
        }
    }
}

@Composable
fun MainShell(selectedPage: Int, onPageSelected: (Int) -> Unit) {
    val page = MainPage.entries[selectedPage]
    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 3.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                MainPage.entries.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedPage == index,
                        onClick = { onPageSelected(index) },
                        icon = { Icon(pageIcon(item), contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 10.sp, fontWeight = FontWeight.Medium) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (page) {
                MainPage.HOME -> HomeScreen(onAdd = { })
                MainPage.CALENDAR -> CalendarScreen()
                MainPage.TASKS -> TasksScreen()
                MainPage.STATS -> StatisticsScreen()
                MainPage.SETTINGS -> SettingsScreen()
            }
        }
    }
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
fun HomeScreen(onAdd: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Good Morning, 👋", color = TextSecondary, fontSize = 14.sp)
                    Text("Akash", color = TextPrimary, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(PurpleSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", color = Purple, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Purple),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "The secret of getting ahead is getting started.",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 21.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("— Mark Twain", color = Color.White.copy(alpha = .78f), fontSize = 12.sp)
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Today · 19 August", fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                TextButton(onClick = { }) { Text("View all", color = Purple, fontSize = 12.sp) }
            }
        }
        items(sampleTasks) { task -> TaskRow(task) }
        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Button(
                    onClick = onAdd,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    modifier = Modifier.size(54.dp)
                ) { Icon(Icons.Default.Add, contentDescription = "Add task", tint = Color.White) }
            }
        }
    }
}

@Composable
fun TaskRow(task: TaskUi) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(3.dp).height(42.dp).clip(RoundedCornerShape(4.dp)).background(task.color))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(task.time, color = TextSecondary, fontSize = 12.sp)
            }
            Tag(task.category, task.color)
        }
    }
}

@Composable
fun Tag(text: String, color: Color) {
    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = .11f)).padding(horizontal = 9.dp, vertical = 5.dp)) {
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun CalendarScreen() {
    val today = LocalDate.now()
    var selectedDay by remember { mutableStateOf(today.dayOfMonth) }
    val monthName = today.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val firstDay = today.withDayOfMonth(1).dayOfWeek.value
    val daysInMonth = today.lengthOfMonth()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { }) { Icon(Icons.Default.ArrowBack, null) }
                Text("$monthName ${today.year}", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                IconButton(onClick = { }) { Icon(Icons.Default.CalendarMonth, null, tint = Purple) }
            }
        }
        item {
            CalendarCard(firstDay, daysInMonth, selectedDay) { selectedDay = it }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${selectedDay} ${monthName}", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text("4 Tasks", color = TextSecondary, fontSize = 12.sp)
            }
        }
        items(sampleTasks) { TaskRow(it) }
        item {
            Button(onClick = { }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Task")
            }
        }
    }
}

@Composable
private fun CalendarCard(firstDay: Int, daysInMonth: Int, selectedDay: Int, onSelect: (Int) -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { Text(it, color = TextSecondary, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(8.dp))
            val cells = (0 until (firstDay - 1)) + (1..daysInMonth).toList()
            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { index ->
                        val day = week.getOrNull(index) ?: 0
                        Box(modifier = Modifier.weight(1f).padding(vertical = 5.dp), contentAlignment = Alignment.Center) {
                            if (day > 0) {
                                Box(
                                    modifier = Modifier.size(34.dp).clip(CircleShape).background(if (day == selectedDay) Purple else Color.Transparent).clickable { onSelect(day) },
                                    contentAlignment = Alignment.Center
                                ) { Text(day.toString(), color = if (day == selectedDay) Color.White else TextPrimary, fontSize = 12.sp, fontWeight = if (day == selectedDay) FontWeight.Bold else FontWeight.Normal) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TasksScreen() {
    var filter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Today", "Upcoming", "Completed")
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("All Tasks", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White).padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(8.dp))
                Text("Search tasks...", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Default.FilterList, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { item ->
                    FilterChip(item, selected = filter == item) { filter = item }
                }
            }
        }
        item { Text("Today · 19 August", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
        items(sampleTasks) { TaskRow(it) }
        item { Text("Tomorrow · 20 August", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp)) }
        item { TaskRow(TaskUi("Client Meeting", "11:00 AM", "Work", Purple)) }
        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Button(onClick = { }, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Purple), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp), modifier = Modifier.size(54.dp)) { Icon(Icons.Default.Add, null) }
            }
        }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (selected) Purple else Color.White).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 9.dp)) {
        Text(text, color = if (selected) Color.White else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StatisticsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Statistics", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Box(modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(Color.White).padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text("This Week", fontSize = 11.sp); Spacer(Modifier.width(4.dp)); Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp)) }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = PurpleDark)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Tasks Completed", color = Color.White.copy(.7f), fontSize = 12.sp)
                    Spacer(Modifier.height(5.dp))
                    Text("23", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text("↗ 18% from last week", color = Color(0xFF74D9A6), fontSize = 11.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                        repeat(12) { index ->
                            Box(modifier = Modifier.weight(1f).height((12 + index % 5 * 8).dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF8360EF)))
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox("Total", "48", Color(0xFFF1EFF8), Modifier.weight(1f))
                StatBox("Completed", "23", Color(0xFFEAF8F0), Modifier.weight(1f))
                StatBox("Pending", "25", Color(0xFFFFF3E5), Modifier.weight(1f))
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Completion Rate", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(92.dp).clip(CircleShape).border(8.dp, Purple, CircleShape), contentAlignment = Alignment.Center) { Text("72%", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                        Spacer(Modifier.width(18.dp))
                        Column { Text("Great Job! 🎉", fontWeight = FontWeight.Bold); Spacer(Modifier.height(5.dp)); Text("You are building a strong completion habit.", color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp) }
                    }
                }
            }
        }
        item {
            Text("Top Categories", fontWeight = FontWeight.Bold)
            CategoryProgress("Work", 14, Purple)
            CategoryProgress("Study", 10, Orange)
            CategoryProgress("Health", 8, Green)
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = PurpleDark)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Current Streak 🔥", color = Color.White.copy(.75f), fontSize = 12.sp)
                    Text("7 Days", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { i, day ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(if (i < 5) Green else Color.White.copy(.2f)), contentAlignment = Alignment.Center) { if (i < 5) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(13.dp)) }
                                Spacer(Modifier.height(3.dp)); Text(day, color = Color.White.copy(.65f), fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox("Best Streak", "14 Days", Color.White, Modifier.weight(1f))
                StatBox("Total Completed", "89", Color.White, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, background: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = background)) {
        Column(modifier = Modifier.padding(13.dp)) { Text(label, color = TextSecondary, fontSize = 10.sp); Spacer(Modifier.height(5.dp)); Text(value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun CategoryProgress(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
        Text(label, fontSize = 11.sp, modifier = Modifier.width(55.dp))
        Box(modifier = Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(5.dp)).background(color.copy(.14f))) { Box(modifier = Modifier.fillMaxWidth(count / 16f).height(7.dp).clip(RoundedCornerShape(5.dp)).background(color)) }
        Spacer(Modifier.width(8.dp)); Text("$count tasks", color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
fun SettingsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 20.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Settings", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = TextPrimary) }
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(58.dp).clip(CircleShape).background(PurpleSoft), contentAlignment = Alignment.Center) { Text("A", color = Purple, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) { Text("Akash", fontWeight = FontWeight.Bold, fontSize = 16.sp); Text("ak.pramanik@example.com", color = TextSecondary, fontSize = 12.sp) }
                Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column {
                    SettingsRow(Icons.Default.Tune, "General")
                    SettingsRow(Icons.Default.NotificationsNone, "Reminders")
                    SettingsRow(Icons.Default.DarkMode, "Appearance", "System")
                    SettingsRow(Icons.Default.Restore, "Backup & Restore")
                    SettingsRow(Icons.Default.Storage, "Data & Storage")
                    SettingsRow(Icons.Default.NotificationsNone, "Notifications")
                    SettingsRow(Icons.Default.PrivacyTip, "Privacy")
                    SettingsRow(Icons.Default.Info, "About Smaran")
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, trailing: String? = null) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().clickable { }.padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(14.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            if (trailing != null) Text(trailing, color = TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        Divider(color = Color(0xFFF0EFF4), thickness = 1.dp)
    }
}

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val slides = listOf(
        Triple("Welcome to", "Smaran", "Your intelligent reminder companion."),
        Triple("Plan Your Tasks", "", "Create tasks with date & time and never miss what matters."),
        Triple("Smart Reminders", "", "Get reminded on time with snooze & reschedule options."),
        Triple("Track & Improve", "", "View history, statistics and build better habits every day."),
        Triple("Let's Achieve More", "", "Stay focused, stay productive and achieve your goals.")
    )
    val icons = listOf("⌛", "☑", "🔔", "▣", "✓")
    val slide = slides[page]

    Box(modifier = Modifier.fillMaxSize().background(PurpleDark)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onFinish) { Text("Skip", color = Color.White, fontSize = 12.sp) }
            }
            Spacer(Modifier.height(42.dp))
            Box(modifier = Modifier.size(210.dp).clip(RoundedCornerShape(48.dp)).background(Color(0xFF211F49)), contentAlignment = Alignment.Center) {
                Text(icons[page], fontSize = 82.sp)
            }
            Spacer(Modifier.height(42.dp))
            Text(slide.first, color = Color.White, fontSize = 22.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
            if (slide.second.isNotEmpty()) Text(slide.second, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Text(slide.third, color = Color.White.copy(.82f), fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 21.sp)
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                slides.indices.forEach { i -> Box(modifier = Modifier.size(if (i == page) 8.dp else 7.dp).clip(CircleShape).background(if (i == page) Color.White else Color.White.copy(.28f))) }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { if (page == slides.lastIndex) onFinish() else page++ },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD8C3FF))
            ) {
                Text(if (page == slides.lastIndex) "Get Started" else "Next", color = PurpleDark, fontWeight = FontWeight.Bold)
                if (page == slides.lastIndex) { Spacer(Modifier.width(8.dp)); Icon(Icons.Default.ArrowForward, null, tint = PurpleDark) }
            }
        }
    }
}
