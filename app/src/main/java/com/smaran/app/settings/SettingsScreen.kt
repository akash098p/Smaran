package com.smaran.app.settings

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import com.smaran.app.profile.ProfilePreferences
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
    val profile = remember { ProfilePreferences(context) }
    var state by remember { mutableStateOf(manager.read()) }
    var page by rememberSaveable { mutableStateOf<String?>(null) }
    var profileVersion by remember { mutableIntStateOf(0) }

    if (page != null) {
        if (page == "Profile") {
            ProfileEditor(
                profile = profile,
                onSaved = { profileVersion++; page = null },
                onBack = { page = null }
            )
        } else {
            SettingsDetailPage(
                title = page!!,
                state = state,
                manager = manager,
                context = context,
                refresh = { state = manager.read() },
                onBack = { page = null }
            )
        }
        return
    }

    // Read the current profile on every recomposition triggered by profileVersion.
    val currentName = profile.name
    val currentEmail = profile.email
    val currentImage = profile.profileImageUri
    @Suppress("UNUSED_VARIABLE") val profileRefresh = profileVersion

    val entries = listOf(
        SettingsEntry("General", "Basic app preferences", Icons.Default.Tune),
        SettingsEntry("Reminders", "Sound, vibration and snooze", Icons.Default.NotificationsActive),
        SettingsEntry("Appearance", "Theme and colors", Icons.Default.Palette),
        SettingsEntry("Backup & Restore", "Protect your local tasks", Icons.Default.Backup),
        SettingsEntry("Data & Storage", "Local task data", Icons.Default.Storage),
        SettingsEntry("Notifications", "Notification behavior", Icons.Default.Notifications),
        SettingsEntry("Privacy", "Your local data", Icons.Default.Lock),
        SettingsEntry("About Smaran", "Version and project information", Icons.Default.Info)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Settings", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = SettingsNavy) }
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().clickable { page = "Profile" }
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(currentName, currentImage, 58)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(currentName.ifBlank { "Your name" }, fontWeight = FontWeight.Bold, color = SettingsNavy, fontSize = 17.sp)
                        Text(currentEmail.ifBlank { "Add your email" }, color = SettingsMuted, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.ChevronRight, "Edit profile", tint = SettingsMuted)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column {
                    entries.forEachIndexed { index, entry ->
                        SettingsItem(entry) { page = entry.title }
                        if (index != entries.lastIndex) HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = Color(0xFFF0EEF4))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(name: String, imageUri: String, size: Int) {
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(Color(0xFFEDE5FF)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri.isNotBlank()) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Profile picture",
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                name.trim().firstOrNull()?.uppercase() ?: "A",
                color = SettingsPurple,
                fontWeight = FontWeight.Bold,
                fontSize = (size / 2.5f).sp
            )
        }
    }
}

@Composable
private fun SettingsItem(entry: SettingsEntry, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(entry.icon, entry.title, tint = SettingsMuted, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(entry.title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = SettingsNavy)
            Text(entry.subtitle, color = SettingsMuted, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, "Open ${entry.title}", tint = SettingsMuted)
    }
}

@Composable
private fun ProfileEditor(profile: ProfilePreferences, onSaved: () -> Unit, onBack: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(profile.name) }
    var email by rememberSaveable { mutableStateOf(profile.email) }
    var age by rememberSaveable { mutableStateOf(profile.age) }
    var imageUri by rememberSaveable { mutableStateOf(profile.profileImageUri) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUri = uri.toString()
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                Text("Profile", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = SettingsNavy)
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    ProfileAvatar(name, imageUri, 96)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = { imagePicker.launch("image/*") }) {
                        Icon(Icons.Default.PhotoCamera, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Change profile picture")
                    }
                    Text("Only stored locally on this device", color = SettingsMuted, fontSize = 11.sp)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Name") }, leadingIcon = { Icon(Icons.Default.Person, null) })
                    OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Email") }, leadingIcon = { Icon(Icons.Default.Email, null) })
                    OutlinedTextField(age, { value -> if (value.all { it.isDigit() } && value.length <= 3) age = value }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Age") }, leadingIcon = { Icon(Icons.Default.Cake, null) })
                }
            }
        }
        item {
            Button(
                onClick = {
                    profile.name = name.trim().ifBlank { "Akash" }
                    profile.email = email.trim()
                    profile.age = age.trim()
                    profile.profileImageUri = imageUri
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SettingsPurple)
            ) { Text("Save Profile", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun SettingsDetailPage(title: String, state: SettingsUiState, manager: SettingsManager, context: Context, refresh: () -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = SettingsNavy)
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (title) {
                        "General" -> InfoBlock(Icons.Default.Tune, "General", "Smaran uses a local-first design for tasks and reminders.")
                        "Reminders" -> {
                            SettingSwitch("Reminder sound", "Play sound with reminder notifications", Icons.Default.VolumeUp, state.soundEnabled) { manager.setSoundEnabled(it); refresh() }
                            SettingSwitch("Vibration", "Vibrate for reminder notifications", Icons.Default.Vibration, state.vibrationEnabled) { manager.setVibrationEnabled(it); refresh() }
                            SettingAction("Exact alarm permission", "Allow reliable scheduled reminders", Icons.Default.Alarm) { AlarmPermissionHelper.openExactAlarmSettings(context) }
                            Text("Quick snooze", fontWeight = FontWeight.Bold, color = SettingsNavy, modifier = Modifier.padding(top = 8.dp))
                            SettingsScreenModel.snoozeOptions().forEach { minutes ->
                                SettingChoice(SettingsScreenModel.snoozeLabel(minutes), minutes == state.defaultSnoozeMinutes) { manager.setDefaultSnoozeMinutes(minutes); refresh() }
                            }
                        }
                        "Appearance" -> {
                            SettingSwitch("Dark mode", "Save your preferred dark appearance", Icons.Default.DarkMode, state.darkMode) { manager.setDarkMode(it); refresh() }
                            SettingSwitch("Dynamic color", "Use Android dynamic colors when supported", Icons.Default.Palette, state.dynamicColor) { manager.setDynamicColor(it); refresh() }
                        }
                        "Backup & Restore" -> InfoBlock(Icons.Default.Backup, "Backup & Restore", "Backup controls can be added here without changing the current task store.")
                        "Data & Storage" -> InfoBlock(Icons.Default.Storage, "Data & Storage", "Your current task data remains stored locally on this device.")
                        "Notifications" -> InfoBlock(Icons.Default.Notifications, "Notifications", "Notification behavior is kept separate from task scheduling.")
                        "Privacy" -> InfoBlock(Icons.Default.Lock, "Privacy", "Your profile and task information are stored locally on this device.")
                        "About Smaran" -> InfoBlock(Icons.Default.Info, "About Smaran", "Smaran — Remember. Plan. Achieve.")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, title, tint = SettingsPurple, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold, color = SettingsNavy); Text(subtitle, color = SettingsMuted, fontSize = 11.sp) }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingAction(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, title, tint = SettingsPurple, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold, color = SettingsNavy); Text(subtitle, color = SettingsMuted, fontSize = 11.sp) }
        Icon(Icons.Default.ChevronRight, "Open", tint = SettingsMuted)
    }
}

@Composable
private fun SettingChoice(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(if (selected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, title, tint = if (selected) SettingsPurple else SettingsMuted, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(title, color = SettingsNavy)
    }
}

@Composable
private fun InfoBlock(icon: ImageVector, title: String, message: String) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, title, tint = SettingsPurple, modifier = Modifier.size(44.dp))
        Spacer(Modifier.height(12.dp))
        Text(title, fontWeight = FontWeight.Bold, color = SettingsNavy)
        Spacer(Modifier.height(6.dp))
        Text(message, color = SettingsMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}
