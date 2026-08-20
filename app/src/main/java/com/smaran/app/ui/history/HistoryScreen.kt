package com.smaran.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smaran.app.data.local.HistoryStore
import com.smaran.app.data.model.HistoryAction
import com.smaran.app.data.model.TaskHistory
import java.time.format.DateTimeFormatter

private val Muted = Color(0xFF747384)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    historyStore: HistoryStore
) {
    var filter by remember { mutableStateOf<String?>(null) }
    val allEvents = remember(historyStore, filter) {
        val events = historyStore.getAll()
        if (filter == null) events else events.filter { it.action.name == filter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filter == null,
                        onClick = { filter = null },
                        label = { Text("All") }
                    )
                    HistoryAction.entries.forEach { action ->
                        FilterChip(
                            selected = filter == action.name,
                            onClick = { filter = action.name },
                            label = { Text(action.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }
            item {
                HorizontalDivider()
            }
            if (allEvents.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No history yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(allEvents) { event ->
                HistoryRow(event)
            }
        }
    }
}

@Composable
private fun HistoryRow(event: TaskHistory) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    when (event.action) {
                        HistoryAction.COMPLETED -> Color(0xFF2E9B6B)
                        HistoryAction.RESCHEDULED -> Color(0xFFE6B34A)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                event.action.name.lowercase().replaceFirstChar { it.uppercase() },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                event.timestamp.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")),
                color = Muted,
                fontSize = 12.sp
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