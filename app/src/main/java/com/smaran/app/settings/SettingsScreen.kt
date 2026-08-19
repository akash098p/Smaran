package com.smaran.app.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smaran.app.reminder.AlarmPermissionHelper

private val SettingsPurple = Color(0xFF5B2BD9)
private val SettingsMuted = Color(0xFF747384)
private val SettingsNavy = Color(0xFF151733)

private data class SettingsEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun Settings(context: Context) {
    val manager = remember { SettingsManager(context) }
    var state by remember { mutableStateOf(manager.read()) }
    var page by rememberSaveable { mutableStateOf<String?>(null) }

    if (page != null) {
        SettingsDetailPage(
            title = page!!,
            state = state,
            manager = manager,
            context = context,
            refresh = { state = manager.read() },
            onBack = { page = null }
        )
        return
    }

    val entries = listOf(
        SettingsEntry("General", "Basic app preferences", Icons.Default.Tune),
        SettingsEntry("Reminders", "Sound, vibration and snooze", Icons.Default.NotificationsActive),
        SettingsEntry("Appearance", "Theme and colors", Icons.Default.Palette),
        SettingsEntry("Backup & Restore", "Protect your local tasks", Icons.Default.Backup),
        SettingsEntry("Data & Storage", "Local task data", Icons.Default.Storage),
        SettingsEntry("Notifications", "Notification behavior", Icons.Default.Notifications),
        SettingsEntry("Privacy", "Your local data", Icons.Default.PrivacyTip),
        SettingsEntry("About Smaran", "Version and project information", Icons.Default.Info)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Settings", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = SettingsNavy)
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFEDE5FF),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("A", color = SettingsPurple, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Akash", fontWeight = FontWeight.Bold, color = SettingsNavy)
                        Text("Local Smaran profile", color = SettingsMuted, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    entries.forEachIndexed { index, entry ->
                        SettingsItem(entry) { page = entry.title }
                        if (index != entries.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 54.dp),
                                color = Color(0xFFF0EEF4)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(entry: SettingsEntry, onClick: () -> Unit) {
    // The whole row is clickable, including the icon, text and arrow.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = entry.icon,
            contentDescription = entry.title,
            tint = SettingsMuted,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                entry.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SettingsNavy
            )
            Text(entry.subtitle, color = SettingsMuted, fontSize = 11.sp)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "Open ${entry.title}",
            tint = SettingsMuted
        )
    }
}

@Composable
private fun SettingsDetailPage(
    title: String,
    state: SettingsUiState,
    manager: SettingsManager,
    context: Context,
    refresh: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
                Text(
                    title,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = SettingsNavy
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (title) {
                        "General" -> InfoBlock(
                            Icons.Default.Tune,
                            "General",
                            "Smaran uses a local-first design for tasks and reminders."
                        )

                        "Reminders" -> {
                            SettingSwitch(
                                "Reminder sound",
                                "Play sound with reminder notifications",
                                Icons.Default.VolumeUp,
                                state.soundEnabled
                            ) {
                                manager.setSoundEnabled(it)
                                refresh()
                            }
                            SettingSwitch(
                                "Vibration",
                                "Vibrate for reminder notifications",
                                Icons.Default.Vibration,
                                state.vibrationEnabled
                            ) {
                                manager.setVibrationEnabled(it)
                                refresh()
                            }
                            SettingAction(
                                "Exact alarm permission",
                                "Allow reliable scheduled reminders",
                                Icons.Default.Alarm
                            ) {
                                AlarmPermissionHelper.openExactAlarmSettings(context)
                            }
                            Text(
                                "Quick snooze",
                                fontWeight = FontWeight.Bold,
                                color = SettingsNavy,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            SettingsScreenModel.snoozeOptions().forEach { minutes ->
                                SettingChoice(
                                    SettingsScreenModel.snoozeLabel(minutes),
                                    minutes == state.defaultSnoozeMinutes
                                ) {
                                    manager.setDefaultSnoozeMinutes(minutes)
                                    refresh()
                                }
                            }
                        }

                        "Appearance" -> {
                            SettingSwitch(
                                "Dark mode",
                                "Save your preferred dark appearance",
                                Icons.Default.DarkMode,
                                state.darkMode
                            ) {
                                manager.setDarkMode(it)
                                refresh()
                            }
                            SettingSwitch(
                                "Dynamic color",
                                "Use Android dynamic colors when supported",
                                Icons.Default.Palette,
                                state.dynamicColor
                            ) {
                                manager.setDynamicColor(it)
                                refresh()
                            }
                            Text(
                                "The preference is stored locally and ready for theme integration.",
                                color = SettingsMuted,
                                fontSize = 12.sp
                            )
                        }

                        "Backup & Restore" -> InfoBlock(
                            Icons.Default.Backup,
                            "Backup & Restore",
                            "Backup controls can be added here without changing the current task store."
                        )

                        "Data & Storage" -> InfoBlock(
                            Icons.Default.Storage,
                            "Data & Storage",
                            "Your current task data remains stored locally on this device."
                        )

                        "Notifications" -> InfoBlock(
                            Icons.Default.Notifications,
                            "Notifications",
                            "Notification behavior is kept separate from task scheduling."
                        )

                        "Privacy" -> InfoBlock(
                            Icons.Default.PrivacyTip,
                            "Privacy",
                            "Smaran is designed around local task data and local reminder scheduling."
                        )

                        "About Smaran" -> InfoBlock(
                            Icons.Default.Info,
                            "About Smaran",
                            "Smaran — Remember. Plan. Achieve."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, title, tint = SettingsPurple)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = SettingsNavy)
            Text(subtitle, color = SettingsMuted, fontSize = 11.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, title, tint = SettingsPurple)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = SettingsNavy)
            Text(subtitle, color = SettingsMuted, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, "Open", tint = SettingsMuted)
    }
}

@Composable
private fun SettingChoice(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (selected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
            title,
            tint = if (selected) SettingsPurple else SettingsMuted
        )
        Spacer(Modifier.width(10.dp))
        Text(title, color = SettingsNavy)
    }
}

@Composable
private fun InfoBlock(icon: ImageVector, title: String, message: String) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, title, tint = SettingsPurple, modifier = Modifier.size(44.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, fontWeight = FontWeight.Bold, color = SettingsNavy)
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            color = SettingsMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
