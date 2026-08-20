package com.smaran.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smaran.app.profile.ProfilePreferences

private val OnboardingNavy = Color(0xFF151733)
private val OnboardingPurple = Color(0xFFD9BFFF)
private val OnboardingPurpleSoft = Color(0xFFB68BFF)
private val OnboardingMuted = Color(0xFFC9C7D8)

private data class Step(
    val title: String,
    val body: String,
    val icon: ImageVector,
    val accent: Color,
    val chips: List<String>
)

private val steps = listOf(
    Step(
        title = "Welcome to Smaran",
        body = "Your intelligent reminder companion.",
        icon = Icons.Default.AccessTime,
        accent = Color(0xFFFFB65C),
        chips = listOf("Simple", "Fast", "Calm")
    ),
    Step(
        title = "Plan Your Tasks",
        body = "Create tasks with date and time so nothing slips through.",
        icon = Icons.Default.Assignment,
        accent = Color(0xFF8E6BFF),
        chips = listOf("Daily plan", "Due dates", "Priority")
    ),
    Step(
        title = "Smart Reminders",
        body = "Snooze or reschedule with quick reminder actions.",
        icon = Icons.Default.Notifications,
        accent = Color(0xFFFFC24D),
        chips = listOf("15 min", "30 min", "1 hour")
    ),
    Step(
        title = "Track & Improve",
        body = "See your history, streaks and progress over time.",
        icon = Icons.Default.CheckCircle,
        accent = Color(0xFF7FD2FF),
        chips = listOf("History", "Insights", "Streaks")
    ),
    Step(
        title = "What should Smaran call you?",
        body = "Add your name so the app can feel more personal.",
        icon = Icons.Default.Person,
        accent = Color(0xFF98E0B2),
        chips = listOf("Personalized", "Local only", "Easy to change")
    )
)

class SmaranLauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val launcher = this
        val prefs = getSharedPreferences("smaran_app", Context.MODE_PRIVATE)

        if (prefs.getBoolean("onboarding_done", false)) {
            startActivity(Intent(this, SmaranActivityPhase3::class.java))
            finish()
            return
        }

        setContent {
            val context = LocalContext.current
            val profile = remember(context) { ProfilePreferences(context) }
            var page by rememberSaveable { mutableIntStateOf(0) }
            var nameInput by rememberSaveable { mutableStateOf("") }
            val step = steps[page]

            Surface(modifier = Modifier.fillMaxSize(), color = OnboardingNavy) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF2A2C67).copy(alpha = 0.9f), OnboardingNavy),
                                radius = 1200f
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset((-48).dp, (-36).dp)
                            .size(190.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6D4AE8).copy(alpha = 0.16f))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(52.dp, 76.dp)
                            .size(220.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF9B7BFF).copy(alpha = 0.14f))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 22.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TopBar(
                            onSkip = {
                                prefs.edit().putBoolean("onboarding_done", true).apply()
                                launcher.startActivity(Intent(launcher, SmaranActivityPhase3::class.java))
                                launcher.finish()
                            }
                        )

                        Spacer(Modifier.height(18.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF191B36).copy(alpha = 0.96f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 22.dp, vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    HeroArt(step = step, page = page)
                                    Spacer(Modifier.height(18.dp))

                                    Text(
                                        text = step.title,
                                        color = Color.White,
                                        fontSize = 27.sp,
                                        lineHeight = 31.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    if (page < 4) {
                                        Text(
                                            text = step.body,
                                            color = OnboardingMuted,
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    } else {
                                        NamePanel(
                                            nameInput = nameInput,
                                            onNameChange = { nameInput = it }
                                        )
                                    }

                                    Spacer(Modifier.height(18.dp))
                                    ChipRow(items = step.chips, accent = step.accent)
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Dots(page = page)
                                    Spacer(Modifier.height(18.dp))

                                    Button(
                                        onClick = {
                                            if (page == 4) {
                                                profile.name = nameInput.trim().ifBlank { "User" }
                                                prefs.edit().putBoolean("onboarding_done", true).apply()
                                                launcher.startActivity(Intent(launcher, SmaranActivityPhase3::class.java))
                                                launcher.finish()
                                            } else {
                                                page++
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = OnboardingPurple)
                                    ) {
                                        Text(
                                            text = if (page == 4) "Get Started" else "Next",
                                            color = OnboardingNavy,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = ">",
                                            color = OnboardingNavy,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(onSkip: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SMARAN",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.8.sp
            )
            Text(text = "Onboarding Flow", color = OnboardingMuted, fontSize = 11.sp)
        }
        TextButton(onClick = onSkip) {
            Text(text = "Skip", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun HeroArt(step: Step, page: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(228.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(step.accent.copy(alpha = 0.12f))
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(44.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(44.dp))
        )
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(step.accent.copy(alpha = 0.95f), step.accent.copy(alpha = 0.45f))
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )
        androidx.compose.material3.Icon(
            imageVector = step.icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(52.dp)
        )

        when (page) {
            0 -> {
                FloatingBadge("Focus", Modifier.align(Alignment.TopStart).offset(12.dp, 34.dp), step.accent)
                FloatingBadge("Tap once", Modifier.align(Alignment.TopEnd).offset((-4).dp, 52.dp), Color(0xFFFFC47D))
                FloatingBadge("No clutter", Modifier.align(Alignment.BottomEnd).offset(10.dp, (-4).dp), Color(0xFF98E0B2))
            }
            1 -> {
                FloatingBadge("Work", Modifier.align(Alignment.TopStart).offset(18.dp, 40.dp), Color(0xFF9D88FF))
                FloatingBadge("Study", Modifier.align(Alignment.TopEnd).offset((-4).dp, 64.dp), Color(0xFF78C6FF))
                FloatingBadge("Routine", Modifier.align(Alignment.BottomStart).offset(16.dp, (-2).dp), Color(0xFFFFC24D))
            }
            2 -> {
                FloatingBadge("15 min", Modifier.align(Alignment.TopEnd).offset((-4).dp, 42.dp), Color(0xFFFFD66B))
                FloatingBadge("30 min", Modifier.align(Alignment.CenterEnd).offset(0.dp, 0.dp), Color(0xFF9C8BFF))
                FloatingBadge("1 hour", Modifier.align(Alignment.BottomEnd).offset((-6).dp, (-12).dp), Color(0xFF9DE3B8))
            }
            3 -> {
                FloatingBadge("History", Modifier.align(Alignment.TopStart).offset(20.dp, 44.dp), Color(0xFF8CD7FF))
                FloatingBadge("Streak", Modifier.align(Alignment.TopEnd).offset((-2).dp, 48.dp), Color(0xFFFFC24D))
                FloatingBadge("Insights", Modifier.align(Alignment.BottomStart).offset(18.dp, (-10).dp), Color(0xFF98E0B2))
            }
            4 -> {
                ProfilePreviewCard(modifier = Modifier.align(Alignment.BottomCenter))
                FloatingBadge("Local only", Modifier.align(Alignment.TopStart).offset(18.dp, 50.dp), Color(0xFF8CD7FF))
                FloatingBadge("Personal", Modifier.align(Alignment.TopEnd).offset((-4).dp, 60.dp), Color(0xFFFFC47D))
            }
        }
    }
}

@Composable
private fun FloatingBadge(text: String, modifier: Modifier, color: Color) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProfilePreviewCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(bottom = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF23274D))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF9DE3B8), Color(0xFF8CD7FF)))),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = OnboardingNavy, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Your profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Name will appear in greetings and reminders.", color = OnboardingMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun NamePanel(nameInput: String, onNameChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Let's make it yours",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Add a name for a more personal greeting and a friendlier home screen.",
                color = OnboardingMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = nameInput,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                label = { Text("Your name") },
                placeholder = { Text("e.g. Akash") },
                leadingIcon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = OnboardingPurple
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.04f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                    focusedBorderColor = OnboardingPurpleSoft,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                    cursorColor = OnboardingPurple,
                    focusedLabelColor = OnboardingPurple,
                    unfocusedLabelColor = OnboardingMuted,
                    focusedPlaceholderColor = OnboardingMuted.copy(alpha = 0.85f),
                    unfocusedPlaceholderColor = OnboardingMuted.copy(alpha = 0.85f)
                )
            )
        }
    }
}

@Composable
private fun ChipRow(items: List<String>, accent: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(accent.copy(alpha = 0.12f))
                    .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(text = item, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun Dots(page: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(steps.size) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == page) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (index == page) OnboardingPurple else Color.White.copy(alpha = 0.22f))
            )
        }
    }
}
