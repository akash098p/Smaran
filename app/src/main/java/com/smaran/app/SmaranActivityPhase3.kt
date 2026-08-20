package com.smaran.app

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.vector.ImageVector
import com.smaran.app.data.local.HistoryStore
import com.smaran.app.data.local.TaskStore
import com.smaran.app.data.model.HistoryAction
import com.smaran.app.data.model.Priority
import com.smaran.app.data.model.Task
import com.smaran.app.reminder.scheduler.ReminderScheduler
import com.smaran.app.settings.AppearancePreferences
import com.smaran.app.settings.Settings
import com.smaran.app.profile.ProfilePreferences
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

private val Purple = Color(0xFF5B2BD9)
private val Background = Color(0xFFF9F8FD)
private val Muted = Color(0xFF747384)
private val WarmSurface = Color(0xFFF7EFF7)
private val WarmChipBorder = Color(0xFF8C8391)
private val CategoryPersonal = Color(0xFF9B7AF5)
private val CategoryWork = Color(0xFFF08A5D)
private val CategoryStudy = Color(0xFF3FA7D6)
private val CategoryHealth = Color(0xFF41B86A)
private val PriorityLowColor = Color(0xFF41B86A)
private val PriorityMediumColor = Color(0xFFF1B84E)
private val PriorityHighColor = Color(0xFFE35D5D)

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
    var editorDate by remember { mutableStateOf(LocalDate.now()) }
    var settingsProfileRequest by rememberSaveable { mutableIntStateOf(0) }
    var settingsNotificationRequest by rememberSaveable { mutableIntStateOf(0) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tasks = remember(refresh) { store.getTasks() }
    val appearancePrefs = remember { AppearancePreferences(context) }
    var settingsVersion by remember { mutableIntStateOf(0) }
    val isDark = remember(settingsVersion) { appearancePrefs.darkMode }
    val useDynamic = remember(settingsVersion) { appearancePrefs.dynamicColor }

    val colorScheme = if (useDynamic && Build.VERSION.SDK_INT >= 31) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (isDark) darkColorScheme(primary = Purple, surface = Color(0xFF121212), background = Color(0xFF121212))
    else lightColorScheme(primary = Purple, surface = Color.White, background = Background)

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                if (tab != 2) {
                    FloatingActionButton(onClick = { editor = null; editorDate = LocalDate.now(); showEditor = true }, containerColor = Purple) {
                        Icon(Icons.Default.Add, "Add task")
                    }
                }
            },
            bottomBar = {
                MagicNavigationBar(
                    selectedIndex = tab,
                    isDark = isDark,
                    onSelected = { tab = it }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (tab) {
                    0 -> HomePhase3Modern(
                        tasks = tasks,
                        onComplete = { complete(it, store, history, scheduler) { refresh++ } },
                        onViewAll = { tab = 2 },
                        onOpenProfile = { settingsProfileRequest++ ; tab = 4 },
                        onOpenNotifications = { settingsNotificationRequest++ ; tab = 4 }
                    )
                    1 -> CalendarPhase3(
                        tasks = tasks,
                        onAddTask = { date -> editor = null; editorDate = date; showEditor = true },
                        onEditTask = { task -> editor = task; editorDate = task.date; showEditor = true }
                    )
                    2 -> TasksPhase3(
                        tasks,
                        add = { editor = null; editorDate = LocalDate.now(); showEditor = true },
                        onEdit = { editor = it; showEditor = true },
                        onComplete = { complete(it, store, history, scheduler) { refresh++ } },
                        onDelete = { delete(it, store, scheduler, history) { refresh++ } }
                    )
                    3 -> StatisticsPhase3(tasks, history)
                    else -> Settings(
                        context,
                        profileEditRequest = settingsProfileRequest,
                        notificationsRequest = settingsNotificationRequest
                    ) { settingsVersion++ }
                }
            }
        }
    }

    if (showEditor) TaskEditorPhase3(editor, initialDate = editorDate, onDismiss = { showEditor = false }) { task ->
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
    val context = LocalContext.current
    val profile = remember { ProfilePreferences(context) }
    val name = remember(profile.name) { profile.name.ifBlank { "Akash" } }
    val today = LocalDate.now(); val list = tasks.filter { it.date == today }.sortedBy { it.time }
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
        item {
            Column {
                TypewriterText(greeting(), MaterialTheme.colorScheme.onSurfaceVariant, TextStyle(fontSize = 16.sp), repeat = true)
                TypewriterText(name, MaterialTheme.colorScheme.onSurface, TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold))
            }
        }
        item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(18.dp)) { Text("The secret of getting ahead is getting started.", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold); Text("— Mark Twain", color = MaterialTheme.colorScheme.onPrimary.copy(.7f), fontSize = 12.sp) } } }
        item { Text("Today · ${today.format(DateTimeFormatter.ofPattern("d MMMM"))}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) }
        if (list.isEmpty()) item { Text("No tasks today. Tap + to create a reminder.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(list) { TaskRowPhase3(it, onComplete, null, null) }
    }
}

@Composable private fun HomePhase3Modern(tasks: List<Task>, onComplete: (Task) -> Unit, onViewAll: () -> Unit, onOpenProfile: () -> Unit, onOpenNotifications: () -> Unit) {
    val context = LocalContext.current
    val profile = remember { ProfilePreferences(context) }
    val name = remember(profile.name) { profile.name.ifBlank { "Akash" } }
    val today = LocalDate.now()
    val list = tasks.filter { it.date == today }.sortedBy { it.time }
    val hasNotifications = tasks.any { !it.completed && it.date <= today.plusDays(1) }
    val streakDays = remember(tasks) {
        val doneDays = tasks.filter { it.completed }.map { it.date }.toSet()
        var count = 0
        var day = today
        while (doneDays.contains(day)) {
            count++
            day = day.minusDays(1)
        }
        count
    }
    val weekStart = remember(today) { today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val weekCompletionCounts = remember(tasks, weekStart) {
        (0..6).associateWith { index ->
            val day = weekStart.plusDays(index.toLong())
            tasks.count { it.completed && it.date == day }
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    TypewriterText(greeting(), MaterialTheme.colorScheme.onSurfaceVariant, TextStyle(fontSize = 16.sp), repeat = true)
                    Text(name, color = MaterialTheme.colorScheme.onSurface, fontSize = 29.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Box(Modifier.clickable { onOpenNotifications() }) {
                        NotificationBell(hasNotifications)
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.clickable { onOpenProfile() }) {
                        ProfileAvatarSmall(name, profile.profileImageUri, 56)
                    }
                }
            }
        }
        item { HomeFeatureCarousel() }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Current Streak", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Consecutive days with completed tasks.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                    Text(
                        "$streakDays days",
                        color = Purple,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        item {
            WeekStreakBar(weekStart = weekStart, completionCounts = weekCompletionCounts)
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Today · ${today.format(DateTimeFormatter.ofPattern("d MMMM"))}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onViewAll) { Text("View all", color = MaterialTheme.colorScheme.primary) }
            }
        }
        if (list.isEmpty()) item { Text("No tasks today. Tap + to create a reminder.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(list) { TaskRowPhase3(it, if (!it.completed) onComplete else null, null, null) }
    }
}

private data class HomeFeatureSlide(
    val image: Int,
    val title: String,
    val body: String
)

private val homeFeatureSlides = listOf(
    HomeFeatureSlide(
        R.drawable.carousel_focus,
        "Welcome to Smaran",
        "Your intelligent reminder companion."
    ),
    HomeFeatureSlide(
        R.drawable.carousel_planning,
        "Plan Your Tasks",
        "Create tasks with date and time so nothing slips through."
    ),
    HomeFeatureSlide(
        R.drawable.carousel_reminders,
        "Smart Reminders",
        "Snooze or reschedule with quick reminder actions."
    ),
    HomeFeatureSlide(
        R.drawable.carousel_progress,
        "Track & Improve",
        "See your history, streaks and progress over time."
    ),
    HomeFeatureSlide(
        R.drawable.carousel_achievement,
        "Let's Achieve More",
        "Stay focused, stay productive and achieve your goals."
    )
)

@Composable
private fun HomeFeatureCarousel() {
    var slideIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(3_000L)
            slideIndex = (slideIndex + 1) % homeFeatureSlides.size
        }
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = homeFeatureSlides[slideIndex],
                transitionSpec = {
                    slideInHorizontally { width -> width } togetherWith
                        slideOutHorizontally { width -> -width }
                },
                label = "home_feature_slide"
            ) { slide ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .padding(horizontal = 14.dp, vertical = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(slide.image),
                        contentDescription = slide.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = slide.title,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = slide.body,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        homeFeatureSlides.indices.forEach { index ->
                            Box(
                                Modifier
                                    .size(if (index == slideIndex) 7.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(if (index == slideIndex) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.30f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun NotificationBell(hasNotifications: Boolean) {
    Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.size(30.dp)) {
        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        if (hasNotifications) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30))
                    .border(1.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable private fun ProfileAvatarSmall(name: String, imageUri: String, size: Int) {
    val context = LocalContext.current
    val bitmap = remember(imageUri) {
        if (imageUri.isBlank()) null else runCatching {
            context.contentResolver.openInputStream(android.net.Uri.parse(imageUri))?.use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
    }
    Box(Modifier.size(size.dp).clip(CircleShape).background(Color(0xFFEDE5FF)), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(
                bitmap.asImageBitmap(),
                "Profile picture",
                Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(name.trim().firstOrNull()?.uppercase() ?: "A", color = Purple, fontWeight = FontWeight.Bold, fontSize = (size / 2.5f).sp)
        }
    }
}

@Composable
private fun MagicNavigationBar(
    selectedIndex: Int,
    isDark: Boolean,
    onSelected: (Int) -> Unit
) {
    data class NavItem(val label: String, val icon: ImageVector)

    val items = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Calendar", Icons.Default.CalendarMonth),
        NavItem("All Tasks", Icons.Default.CheckCircle),
        NavItem("Statistics", Icons.Default.BarChart),
        NavItem("Settings", Icons.Default.Settings)
    )

    val barColor = if (isDark) Color(0xFF1B1B2A) else Color(0xFFF4EEFF)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFE2D5FF)
    val indicatorBrush = Brush.linearGradient(
        colors = listOf(
            Purple,
            Color(0xFF7A55F4)
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        val itemWidth = maxWidth / items.size
        val indicatorSize = 60.dp
        val indicatorY = (-18).dp
        val indicatorX by animateDpAsState(
            targetValue = itemWidth * selectedIndex + (itemWidth - indicatorSize) / 2,
            animationSpec = spring(dampingRatio = 0.82f, stiffness = 520f),
            label = "magic_indicator_x"
        )

        val activeIcon by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            label = "magic_active_icon"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .size(width = 82.dp, height = 6.dp)
                    .clip(CircleShape)
                    .background(Purple.copy(alpha = if (isDark) 0.20f else 0.12f))
                    .zIndex(1f)
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorX, y = indicatorY)
                    .size(indicatorSize)
                    .shadow(18.dp, CircleShape, clip = false)
                    .background(brush = indicatorBrush, shape = CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                    .zIndex(2f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = items[selectedIndex].icon,
                    contentDescription = items[selectedIndex].label,
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(scaleX = activeIcon, scaleY = activeIcon)
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = barColor,
                tonalElevation = 0.dp,
                shadowElevation = 14.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .padding(horizontal = 4.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val selected = index == selectedIndex
                        val lift by animateDpAsState(
                            targetValue = if (selected) (-8).dp else 0.dp,
                            animationSpec = spring(dampingRatio = 0.86f, stiffness = 650f),
                            label = "magic_icon_lift_$index"
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.02f else 1f,
                            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                            label = "magic_icon_scale_$index"
                        )
                        val alpha by animateFloatAsState(
                            targetValue = if (selected) 0f else 1f,
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                            label = "magic_icon_alpha_$index"
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onSelected(index) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset(y = lift)
                                    .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
                                    .zIndex(if (selected) 0f else 1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selected) Purple else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun CalendarPhase3(tasks: List<Task>, onAddTask: (LocalDate) -> Unit, onEditTask: (Task) -> Unit) {
    var displayedMonth by rememberSaveable { mutableStateOf(YearMonth.now()) }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    if (selectedDate.year != displayedMonth.year || selectedDate.monthValue != displayedMonth.monthValue) {
        selectedDate = displayedMonth.atDay(minOf(selectedDate.dayOfMonth, displayedMonth.lengthOfMonth()))
    }

    val selectedTasks = remember(tasks, selectedDate) {
        tasks.filter { it.date == selectedDate }.sortedBy { it.time }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 110.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    displayedMonth = displayedMonth.minusMonths(1)
                    selectedDate = displayedMonth.atDay(minOf(selectedDate.dayOfMonth, displayedMonth.lengthOfMonth()))
                }) {
                    Icon(Icons.Default.ChevronLeft, null, tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = displayedMonth.month.name.lowercase().replaceFirstChar { it.uppercase() } + " " + displayedMonth.year,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = {
                    displayedMonth = YearMonth.now()
                    selectedDate = LocalDate.now()
                }) {
                    Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    val firstOffset = displayedMonth.atDay(1).dayOfWeek.value - 1
                    val cells = List(42) { index ->
                        val day = index - firstOffset + 1
                        if (day in 1..displayedMonth.lengthOfMonth()) displayedMonth.atDay(day) else null
                    }
                    cells.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            week.forEach { date ->
                                DayCell(
                                    date = date,
                                    selected = date == selectedDate,
                                    hasTask = date?.let { d -> tasks.any { it.date == d } } == true,
                                    modifier = Modifier.weight(1f),
                                    onClick = { if (date != null) selectedDate = date }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${selectedTasks.size} Tasks",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (selectedTasks.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                        Text("No tasks scheduled", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Tap Add Task to create something for this day.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(selectedTasks, key = { it.id }) { task ->
                CalendarAgendaRow(task = task, onClick = { onEditTask(task) })
            }
        }

        item {
            Button(
                onClick = { onAddTask(selectedDate) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Task")
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    selected: Boolean,
    hasTask: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val isToday = date == today
    Box(
        modifier = modifier
            .padding(vertical = 4.dp)
            .height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        if (date == null) {
            Spacer(Modifier.size(34.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (selected) Purple else Color.Transparent)
                    .border(
                        width = if (isToday && !selected) 1.dp else 0.dp,
                        color = if (isToday) Purple else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (hasTask) {
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (selected) Color.White else Purple)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarAgendaRow(task: Task, onClick: () -> Unit) {
    val accent = when (task.category.lowercase()) {
        "work" -> Color(0xFF6B4EFF)
        "study" -> Color(0xFF3E82FF)
        "health" -> Color(0xFF2BB673)
        else -> Color(0xFFFFA847)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(42.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(task.time.format(DateTimeFormatter.ofPattern("hh:mm")), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                Text(task.time.format(DateTimeFormatter.ofPattern("a")), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(accent)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    task.title,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(accent.copy(alpha = .12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(task.category, color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            if (task.completed) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF27A96B))
            }
        }
    }
}

@Composable private fun TasksPhase3(tasks: List<Task>, add: () -> Unit, onEdit: (Task) -> Unit, onComplete: (Task) -> Unit, onDelete: (Task) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("All") }
    val today = LocalDate.now()

    val visible = remember(tasks, query, filter) {
        tasks.asSequence()
            .filter { task ->
                when (filter) {
                    "Today" -> task.date == today
                    "Pending" -> !task.completed
                    "Completed" -> task.completed
                    else -> true
                }
            }
            .filter { task ->
                val term = query.trim()
                term.isBlank() || task.title.contains(term, ignoreCase = true) || task.description.contains(term, ignoreCase = true) || task.category.contains(term, ignoreCase = true)
            }
            .sortedBy { it.date.atTime(it.time) }
            .toList()
    }

    val grouped = visible.groupBy { it.date }.toSortedMap { a, b ->
        when {
            a == today && b != today -> -1
            a != today && b == today -> 1
            a.isAfter(today) && b.isBefore(today) -> -1
            a.isBefore(today) && b.isAfter(today) -> 1
            else -> a.compareTo(b)
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 110.dp)
        ) {
            item {
                Text("All Tasks", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(14.dp))
                SearchBar(query = query, onQueryChange = { query = it })
                Spacer(Modifier.height(14.dp))
                TaskFilterRow(filter = filter, onFilterChange = { filter = it })
            }

            if (visible.isEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text("No tasks found", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "Try a different keyword or switch filters.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                grouped.forEach { (date, itemsForDay) ->
                    item {
                        Text(
                            text = formatTaskSection(date, today),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                    }
                    items(itemsForDay, key = { it.id }) { task ->
                        TaskTaskCard(task, onEdit = onEdit, onComplete = onComplete, onDelete = onDelete)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = add,
            containerColor = Purple,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .size(58.dp)
        ) {
            Icon(Icons.Default.Add, "Add task")
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Search tasks...") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = { Icon(Icons.Default.FilterList, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = .22f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = .14f),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun TaskFilterRow(filter: String, onFilterChange: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        listOf("All", "Today", "Pending", "Completed").forEach { value ->
            val selected = filter == value
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selected) Purple else MaterialTheme.colorScheme.surface)
                    .border(1.dp, if (selected) Purple else MaterialTheme.colorScheme.outline.copy(alpha = .14f), RoundedCornerShape(999.dp))
                    .clickable { onFilterChange(value) }
                    .padding(horizontal = 15.dp, vertical = 10.dp)
            ) {
                Text(
                    text = value,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun formatTaskSection(date: LocalDate, today: LocalDate): String {
    val label = when (date) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE"))
    }
    return "$label · ${date.format(DateTimeFormatter.ofPattern("d MMM"))}"
}

@Composable
private fun TaskTaskCard(task: Task, onEdit: (Task) -> Unit, onComplete: (Task) -> Unit, onDelete: (Task) -> Unit) {
    val accent = when (task.category.lowercase()) {
        "work" -> Color(0xFF6B4EFF)
        "study" -> Color(0xFF3E82FF)
        "health" -> Color(0xFF2BB673)
        else -> Color(0xFFFFA847)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEdit(task) }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(46.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(accent)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = task.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = task.time.format(DateTimeFormatter.ofPattern("hh:mm a")),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(accent.copy(alpha = .12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = task.category,
                        color = accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            IconButton(onClick = { if (!task.completed) onComplete(task) }) {
                Icon(
                    imageVector = if (task.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (task.completed) Color(0xFF27A96B) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable private fun StatisticsPhase3(tasks: List<Task>, history: HistoryStore) {
    val today = LocalDate.now()
    val weekStart = remember(today) { today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val weekEnd = weekStart.plusDays(6)
    val weekTasks = tasks.filter { !it.date.isBefore(weekStart) && !it.date.isAfter(weekEnd) }
    val total = weekTasks.size
    val done = weekTasks.count { it.completed }
    val pending = total - done
    val rate = if (total == 0) 0 else (done * 100 / total)

    val doneDays = tasks.filter { it.completed }.map { it.date }.toSet()
    var streak = 0
    var d = today
    while (doneDays.contains(d)) {
        streak++
        d = d.minusDays(1)
    }

    val weekCompletionCounts = remember(tasks, weekStart) {
        (0..6).associateWith { index ->
            val day = weekStart.plusDays(index.toLong())
            weekTasks.count { it.completed && it.date == day }
        }
    }

    val historyActions = remember(history, weekStart) {
        history.getAll().filter { !it.timestamp.toLocalDate().isBefore(weekStart) }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Statistics", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text("This Week") },
                    trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) }
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tasks Completed", color = MaterialTheme.colorScheme.onPrimary.copy(.82f), fontWeight = FontWeight.Medium)
                            Text(done.toString(), color = MaterialTheme.colorScheme.onPrimary, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, null, tint = Color(0xFF7DE0A3), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${if (rate >= 0) "+" else ""}${rate}% from this week",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(.78f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        WeekSparkline(completionCounts = weekCompletionCounts.values.toList(), tint = Color(0xFF8E6BFF))
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Total", total.toString(), Modifier.weight(1f))
                StatTile("Completed", done.toString(), Modifier.weight(1f), highlight = true)
                StatTile("Pending", pending.toString(), Modifier.weight(1f))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CompletionRing(progress = rate / 100f)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Completion Rate", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(completionHeadline(rate), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                        Text(
                            completionBody(rate),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Current Streak", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("🔥")
                    }
                    Text("${streak} Days", color = MaterialTheme.colorScheme.primary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    WeekStreakBar(weekStart = weekStart, completionCounts = weekCompletionCounts)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Recent Activity", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    if (historyActions.isEmpty()) {
                        Text("No activity yet this week.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    } else {
                        historyActions.take(6).forEach { hist ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    hist.action.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                                Text(
                                    hist.timestamp.format(DateTimeFormatter.ofPattern("dd MMM HH:mm")),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun TaskRowPhase3(task: Task, onComplete: ((Task) -> Unit)?, onEdit: ((Task) -> Unit)?, onDelete: ((Task) -> Unit)?) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().clickable(enabled = onEdit != null) { onEdit?.invoke(task) }) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(task.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface); Text("${task.date} · ${task.time.format(DateTimeFormatter.ofPattern("hh:mm a"))}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp); Text("${task.category} · ${task.priority.name.lowercase()}", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp) }; if(onComplete != null) IconButton({ onComplete(task) }) { Icon(Icons.Default.CheckCircle, "Complete", tint = MaterialTheme.colorScheme.primary) }; if(onDelete != null) IconButton({ onDelete(task) }) { Icon(Icons.Default.DeleteOutline, "Delete", tint = Color.Red) } else if(task.completed) Icon(Icons.Default.Check, "Done", tint = Color(0xFF27A96B)) } }
}

@Composable private fun StatTile(label: String, value: String, modifier: Modifier, highlight: Boolean = false) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (highlight) MaterialTheme.colorScheme.primary.copy(alpha = .08f) else MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable private fun CompletionRing(progress: Float) {
    val trackColor = MaterialTheme.colorScheme.primary.copy(alpha = .12f)
    val labelColor = MaterialTheme.colorScheme.onSurface
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(118.dp)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )
            drawArc(
                color = Purple,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )
        }
        Text("${(progress * 100).toInt()}%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = labelColor)
    }
}

@Composable private fun WeekSparkline(completionCounts: List<Int>, tint: Color) {
    val max = (completionCounts.maxOrNull() ?: 1).coerceAtLeast(1)
    Canvas(modifier = Modifier.size(width = 130.dp, height = 78.dp)) {
        val w = size.width
        val h = size.height
        val stepX = if (completionCounts.size > 1) w / (completionCounts.size - 1) else w
        val points = completionCounts.mapIndexed { index, value ->
            val x = index * stepX
            val y = h - (value.toFloat() / max.toFloat()).coerceIn(0f, 1f) * (h - 8f) - 4f
            androidx.compose.ui.geometry.Offset(x, y)
        }
        for (i in 0 until points.lastIndex) {
            drawLine(color = tint, start = points[i], end = points[i + 1], strokeWidth = 5f, cap = StrokeCap.Round)
        }
        points.forEach { p ->
            drawCircle(color = Color.White, radius = 3.5f, center = p)
            drawCircle(color = tint, radius = 2.8f, center = p)
        }
    }
}

@Composable private fun WeekStreakBar(weekStart: LocalDate, completionCounts: Map<Int, Int>) {
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEachIndexed { index, label ->
            val day = weekStart.plusDays(index.toLong())
            val active = (completionCounts[index] ?: 0) > 0
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (active) Purple else MaterialTheme.colorScheme.onSurface.copy(alpha = .12f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (active) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
        }
    }
}

@Composable private fun TaskEditorPhase3(existing: Task?, initialDate: LocalDate = LocalDate.now(), onDismiss: () -> Unit, onSave: (Task) -> Unit) {
    val context = LocalContext.current; var title by remember(existing) { mutableStateOf(existing?.title ?: "") }; var description by remember(existing) { mutableStateOf(existing?.description ?: "") }; var date by remember(existing) { mutableStateOf(existing?.date ?: initialDate) }; var time by remember(existing) { mutableStateOf(existing?.time ?: LocalTime.now().plusHours(1).withMinute(0)) }; var category by remember(existing) { mutableStateOf(existing?.category ?: "Personal") }; var priority by remember(existing) { mutableStateOf(existing?.priority ?: Priority.MEDIUM) }; var recurring by remember(existing) { mutableStateOf(existing?.recurring ?: false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if(existing == null) "Create Task" else "Edit / Reschedule") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Task title") }); OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Description (optional)") })
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { OutlinedButton({ DatePickerDialog(context, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) }, date.year, date.monthValue - 1, date.dayOfMonth).show() }, Modifier.weight(1f)) { Text(date.toString()) }; OutlinedButton({ TimePickerDialog(context, { _, h, m -> time = LocalTime.of(h, m) }, time.hour, time.minute, true).show() }, Modifier.weight(1f)) { Text(time.format(DateTimeFormatter.ofPattern("hh:mm a"))) } }
        Text("Category", color = Muted, fontSize = 11.sp)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                CategoryChip("Personal", category == "Personal", CategoryPersonal, modifier = Modifier.weight(1f)) { category = "Personal" }
                CategoryChip("Work", category == "Work", CategoryWork, modifier = Modifier.weight(1f)) { category = "Work" }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                CategoryChip("Study", category == "Study", CategoryStudy, modifier = Modifier.weight(1f)) { category = "Study" }
                CategoryChip("Health", category == "Health", CategoryHealth, modifier = Modifier.weight(1f)) { category = "Health" }
            }
        }
        Text("Priority", color = Muted, fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            PriorityChip("Low", priority == Priority.LOW, PriorityLowColor) { priority = Priority.LOW }
            PriorityChip("Medium", priority == Priority.MEDIUM, PriorityMediumColor) { priority = Priority.MEDIUM }
            PriorityChip("High", priority == Priority.HIGH, PriorityHighColor) { priority = Priority.HIGH }
        }
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(recurring, { recurring = it }); Text("Repeat daily") }
    } }, confirmButton = { Button(enabled = title.isNotBlank(), onClick = { onSave(Task(existing?.id ?: System.currentTimeMillis(), title.trim(), description.trim(), date, time, category, priority, existing?.completed ?: false, recurring)) }, colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

private fun greeting() = when(LocalTime.now().hour) { in 0..11 -> "Good Morning, 👋"; in 12..16 -> "Good Afternoon, 👋"; else -> "Good Evening, 👋" }

private fun completionHeadline(rate: Int): String = when (rate) {
    0 -> "No progress yet"
    in 1..24 -> "Small steps count"
    in 25..49 -> "Good momentum"
    in 50..74 -> "Solid progress"
    in 75..99 -> "Excellent work"
    else -> "Perfect week"
}

private fun completionBody(rate: Int): String = when (rate) {
    0 -> "No completed tasks this week yet. Start with one task and build from there."
    in 1..24 -> "You have started moving. Keep the streak going with a few more wins."
    in 25..49 -> "You are building consistency. A few more completions will push this higher."
    in 50..74 -> "You are past the halfway mark and making real progress this week."
    in 75..99 -> "You are close to a full win. One or two more tasks will finish the week strong."
    else -> "All tasks are completed. That is a perfect week."
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, accent: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val background = if (selected) accent.copy(alpha = 0.16f) else WarmSurface
    val border = if (selected) accent else WarmChipBorder.copy(alpha = 0.65f)
    val textColor = if (selected) Purple else Color(0xFF4B4652)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Text(label, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PriorityChip(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    val background = if (selected) accent.copy(alpha = 0.16f) else Color(0xFFF8F2F8)
    val border = if (selected) accent else WarmChipBorder.copy(alpha = 0.65f)
    val textColor = if (selected) Color(0xFF2C2330) else Color(0xFF4B4652)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Text(label, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TypewriterText(text: String, color: Color, style: TextStyle, delayMillis: Long = 50L, repeat: Boolean = false) {
    var visibleText by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        do {
            visibleText = ""
            text.forEach { char ->
                visibleText += char
                kotlinx.coroutines.delay(delayMillis)
            }
            if (repeat) kotlinx.coroutines.delay(3000L) // Stay for 3s before restarting
        } while (repeat)
    }
    Text(text = visibleText, color = color, style = style)
}
