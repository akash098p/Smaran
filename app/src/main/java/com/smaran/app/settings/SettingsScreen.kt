package com.smaran.app.settings

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream
import com.smaran.app.profile.ProfilePreferences
import com.smaran.app.reminder.AlarmPermissionHelper

private val SettingsPurple = Color(0xFF5B2BD9)
private val SettingsMuted = Color(0xFF747384)
private val SettingsNavy = Color(0xFF151733)

private data class SettingsEntry(val title: String, val subtitle: String, val icon: ImageVector)

@Composable
fun Settings(context: Context, profileEditRequest: Int = 0, notificationsRequest: Int = 0, onSettingsChanged: () -> Unit = {}) {
    val colorScheme = MaterialTheme.colorScheme
    val manager = remember { SettingsManager(context) }
    val profile = remember { ProfilePreferences(context) }
    var state by remember { mutableStateOf(manager.read()) }
    var page by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(profileEditRequest) {
        if (profileEditRequest > 0) page = "Profile"
    }
    LaunchedEffect(notificationsRequest) {
        if (notificationsRequest > 0) page = "Notifications"
    }
    var profileVersion by remember { mutableIntStateOf(0) }

    if (page != null) {
        if (page == "Profile") {
            ProfileEditor(profile, { profileVersion++; page = null }, { page = null })
        } else {
            SettingsDetailPage(page!!, state, manager, context, refresh = {
                state = manager.read()
                onSettingsChanged()
            }, { page = null })
        }
        return
    }

    val currentName = profile.name
    val currentEmail = profile.email
    val currentImage = profile.profileImageUri
    @Suppress("UNUSED_VARIABLE") val version = profileVersion

    val entries = listOf(
        SettingsEntry("Reminders", "Sound, vibration and snooze", Icons.Default.NotificationsActive),
        SettingsEntry("Appearance", "Theme and colors", Icons.Default.Palette),
        SettingsEntry("Backup & Restore", "Protect your local tasks", Icons.Default.Backup),
        SettingsEntry("Data & Storage", "Local task data", Icons.Default.Storage),
        SettingsEntry("Notifications", "Notification behavior", Icons.Default.Notifications),
        SettingsEntry("Privacy", "Your local data", Icons.Default.Lock),
        SettingsEntry("About Smaran", "Version and project information", Icons.Default.Info)
    )

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Settings", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground) }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline.copy(alpha = .28f)), modifier = Modifier.fillMaxWidth().clickable { page = "Profile" }) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(currentName, currentImage, 58)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(currentName.ifBlank { "Your name" }, fontWeight = FontWeight.Bold, color = colorScheme.onSurface, fontSize = 17.sp)
                        Text(currentEmail.ifBlank { "Add your email" }, color = colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Icon(Icons.Default.ChevronRight, "Edit profile", tint = colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline.copy(alpha = .28f)), modifier = Modifier.fillMaxWidth()) {
                Column {
                    entries.forEachIndexed { index, entry ->
                        SettingsItem(entry) { page = entry.title }
                        if (index != entries.lastIndex) HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(name: String, imageUri: String, size: Int) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val bitmap = remember(imageUri) {
        if (imageUri.isBlank()) null else runCatching {
            context.contentResolver.openInputStream(android.net.Uri.parse(imageUri))?.use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
    }
    Box(Modifier.size(size.dp).clip(CircleShape).background(colorScheme.primary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(bitmap.asImageBitmap(), "Profile picture", Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
        } else {
            Text(name.trim().firstOrNull()?.uppercase() ?: "A", color = SettingsPurple, fontWeight = FontWeight.Bold, fontSize = (size / 2.5f).sp)
        }
    }
}

@Composable
private fun SettingsItem(entry: SettingsEntry, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = colorScheme.primary.copy(alpha = 0.12f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(entry.icon, entry.title, tint = SettingsPurple, modifier = Modifier.size(21.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(entry.title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurface)
            Text(entry.subtitle, color = colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, "Open ${entry.title}", tint = colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProfileEditor(profile: ProfilePreferences, onSaved: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    var name by rememberSaveable { mutableStateOf(profile.name) }
    var email by rememberSaveable { mutableStateOf(profile.email) }
    var age by rememberSaveable { mutableStateOf(profile.age) }
    var imageUri by rememberSaveable { mutableStateOf(profile.profileImageUri) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val persistedUri = copyToInternalStorage(context, uri)
            if (persistedUri != null) imageUri = persistedUri
        }
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = colorScheme.onSurface) }; Text("Profile", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface) } }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline.copy(alpha = .28f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    ProfileAvatar(name, imageUri, 96)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = { imagePicker.launch("image/*") }) { Icon(Icons.Default.PhotoCamera, null); Spacer(Modifier.width(8.dp)); Text("Change profile picture") }
                    Text("Only stored locally on this device", color = colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline.copy(alpha = .28f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Name") }, leadingIcon = { Icon(Icons.Default.Person, null) })
                    OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Email") }, leadingIcon = { Icon(Icons.Default.Email, null) })
                    OutlinedTextField(age, { value -> if (value.all { it.isDigit() } && value.length <= 3) age = value }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Age") }, leadingIcon = { Icon(Icons.Default.Cake, null) })
                }
            }
        }
        item {
            Button(onClick = {
                profile.name = name.trim().ifBlank { "User" }
                profile.email = email.trim()
                profile.age = age.trim()
                profile.profileImageUri = imageUri
                onSaved()
            }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = SettingsPurple)) { Text("Save Profile", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun SettingsDetailPage(title: String, state: SettingsUiState, manager: SettingsManager, context: Context, refresh: () -> Unit, onBack: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val soundPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            manager.setCustomSoundUri(uri.toString())
            refresh()
        }
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = colorScheme.onSurface) }; Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface) } }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline.copy(alpha = .28f)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (title) {
                        "Reminders" -> {
                            SettingSwitch("Reminder sound", "Play sound with reminder notifications", Icons.Default.VolumeUp, state.soundEnabled) { manager.setSoundEnabled(it); refresh() }
                            SettingSwitch("Vibration", "Vibrate for reminder notifications", Icons.Default.Vibration, state.vibrationEnabled) { manager.setVibrationEnabled(it); refresh() }
                            SettingAction("Exact alarm permission", "Allow reliable scheduled reminders", Icons.Default.Alarm, context) { AlarmPermissionHelper.openExactAlarmSettings(context) }
                            Text("Quick snooze", fontWeight = FontWeight.Bold, color = colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp))
                            SettingsScreenModel.snoozeOptions().forEach { minutes -> SettingChoice(SettingsScreenModel.snoozeLabel(minutes), minutes == state.defaultSnoozeMinutes) { manager.setDefaultSnoozeMinutes(minutes); refresh() } }
                        }
                        "Appearance" -> {
                            SettingSwitch("Dark mode", "Save your preferred dark appearance", Icons.Default.DarkMode, state.darkMode) { manager.setDarkMode(it); refresh() }
                            SettingSwitch("Dynamic color", "Use Android dynamic colors when supported", Icons.Default.Palette, state.dynamicColor) { manager.setDynamicColor(it); refresh() }
                        }
                        "Backup & Restore" -> InfoBlock(Icons.Default.Backup, "Backup & Restore", "Backup controls can be added here without changing the current task store.")
                        "Data & Storage" -> InfoBlock(Icons.Default.Storage, "Data & Storage", "Your current task data remains stored locally on this device.")
                        "Notifications" -> {
                            SettingSwitch("Alarm-style notification", "Loop the reminder sound until snoozed or completed", Icons.Default.Alarm, state.alarmStyleEnabled) { manager.setAlarmStyleEnabled(it); refresh() }
                            SettingAction(
                                "Notification sound",
                                if (state.customSoundUri.isBlank()) "Use the default Smaran sound" else "Custom sound selected",
                                Icons.Default.MusicNote,
                                context
                            ) { soundPicker.launch(arrayOf("audio/*")) }
                            if (state.customSoundUri.isNotBlank()) {
                                SettingAction("Use default sound", "Restore the bundled Smaran notification sound", Icons.Default.RestartAlt, context) {
                                    manager.setCustomSoundUri("")
                                    refresh()
                                }
                            }
                        }
                        "Privacy" -> InfoBlock(Icons.Default.Lock, "Privacy", "Your profile and task information are stored locally on this device.")
                        "About Smaran" -> {
                            InfoBlock(Icons.Default.Info, "About Smaran", "Smaran — Remember. Plan. Achieve.")
                            Spacer(Modifier.height(8.dp))
                            Text("1.0.0", color = colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "A simple, private task manager that helps you remember and plan your day.",
                                color = colorScheme.onSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("Report an Issue / Feedback", fontWeight = FontWeight.Bold, color = colorScheme.onSurface, fontSize = 15.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "We'd love to hear from you! If you encounter any issues or have suggestions to improve Smaran, " +
                                    "please reach out to us at:",
                                color = colorScheme.onSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Start
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                        data = android.net.Uri.parse("mailto:nexteraf@gmail.com")
                                    }
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(Icons.Default.Email, null, tint = SettingsPurple, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("nexteraf@gmail.com", color = SettingsPurple, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(38.dp), shape = RoundedCornerShape(11.dp), color = colorScheme.primary.copy(alpha = 0.12f)) { Box(contentAlignment = Alignment.Center) { Icon(icon, title, tint = SettingsPurple, modifier = Modifier.size(21.dp)) } }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurface); Text(subtitle, color = colorScheme.onSurfaceVariant, fontSize = 11.sp) }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingAction(title: String, subtitle: String, icon: ImageVector, context: Context, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(38.dp), shape = RoundedCornerShape(11.dp), color = colorScheme.primary.copy(alpha = 0.12f)) { Box(contentAlignment = Alignment.Center) { Icon(icon, title, tint = SettingsPurple, modifier = Modifier.size(21.dp)) } }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurface); Text(subtitle, color = colorScheme.onSurfaceVariant, fontSize = 11.sp) }
        Icon(Icons.Default.ChevronRight, "Open", tint = colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingChoice(title: String, selected: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(30.dp), shape = CircleShape, color = if (selected) colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent) { Box(contentAlignment = Alignment.Center) { Icon(if (selected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, title, tint = if (selected) SettingsPurple else colorScheme.onSurfaceVariant, modifier = Modifier.size(21.dp)) } }
        Spacer(Modifier.width(10.dp))
        Text(title, color = colorScheme.onSurface)
    }
}

@Composable
private fun InfoBlock(icon: ImageVector, title: String, message: String) {
    val colorScheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = colorScheme.primary.copy(alpha = 0.12f)) { Box(contentAlignment = Alignment.Center) { Icon(icon, title, tint = SettingsPurple, modifier = Modifier.size(30.dp)) } }
        Spacer(Modifier.height(12.dp))
        Text(title, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
        Spacer(Modifier.height(6.dp))
        Text(message, color = colorScheme.onSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

private fun copyToInternalStorage(context: Context, uri: android.net.Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val dir = File(context.filesDir, "profile_images")
        dir.mkdirs()
        val file = File(dir, "profile_picture.jpg")
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        file.toURI().toString()
    } catch (e: Exception) {
        null
    }
}
