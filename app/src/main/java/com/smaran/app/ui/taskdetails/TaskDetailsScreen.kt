package com.smaran.app.ui.taskdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smaran.app.data.local.HistoryStore
import com.smaran.app.data.local.TaskStore
import com.smaran.app.data.model.HistoryAction
import com.smaran.app.data.model.Task
import com.smaran.app.data.model.TaskHistory
import java.time.format.DateTimeFormatter

private val Muted = Color(0xFF747384)
private val TimelineGreen = Color(0xFF2E9B6B)
private val TimelineAmber = Color(0xFFE6B34A)

/** Task details screen with a full timeline as documented in the README. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(
    taskId: Long,
    onBack: () -> Unit,
    store: TaskStore,
    historyStore: HistoryStore
) {
    val task = remember(taskId) { store.getTasks().firstOrNull { it.id == taskId } }
    if (task == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val timeline = remember(task.id, historyStore) {
        historyStore.getAll().filter { it.taskId == task.id }.sortedBy { it.timestamp }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(
                            task.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${task.date} · ${task.time.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .7f),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        CategoryChip(task.category)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            task.description.ifBlank { "No description" },
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (task.completed) item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F7E4)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = TimelineGreen)
                        Spacer(Modifier.width(10.dp))
                        Text("Completed", color = TimelineGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            item {
                Text(
                    "Timeline",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(timeline) { event ->
                TimelineRow(event)
            }
        }
    }
}

@Composable
private fun TimelineRow(event: TaskHistory) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .background(
                    if (event.action == HistoryAction.COMPLETED) TimelineGreen else TimelineAmber,
                    CircleShape
                )
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                event.action.name.lowercase().replaceFirstChar { it.uppercase() },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                event.timestamp.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")),
                color = Muted,
                fontSize = 11.sp
            )
            if (event.previousDateTime != null && event.newDateTime != null) {
                Text(
                    "${event.previousDateTime.format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))} → ${event.newDateTime.format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))}",
                    color = Muted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(category: String) {
    Surface(shape = RoundedCornerShape(50), color = Color(0xFFF1EDFF)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(category, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5B2BD9), maxLines = 1)
        }
    }
}