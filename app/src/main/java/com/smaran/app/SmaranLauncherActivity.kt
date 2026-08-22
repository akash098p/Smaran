package com.smaran.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
        window.statusBarColor = android.graphics.Color.rgb(21, 23, 51)
        window.navigationBarColor = android.graphics.Color.rgb(21, 23, 51)
        window.decorView.systemUiVisibility = 0
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
                            .padding(start = 20.dp, end = 20.dp, top = 38.dp, bottom = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TopBar(
                            onSkip = {
                                prefs.edit().putBoolean("onboarding_done", true).apply()
                                launcher.startActivity(Intent(launcher, SmaranActivityPhase3::class.java))
                                launcher.finish()
                            }
                        )

                        Spacer(Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        var dragDistance = 0f
                                        detectHorizontalDragGestures(
                                            onHorizontalDrag = { change, amount ->
                                                change.consume()
                                                dragDistance += amount
                                            },
                                            onDragEnd = {
                                                if (dragDistance < -72f && page < steps.lastIndex) page++
                                                if (dragDistance > 72f && page > 0) page--
                                                dragDistance = 0f
                                            },
                                            onDragCancel = { dragDistance = 0f }
                                        )
                                    }
                                    .padding(horizontal = 22.dp, vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                AnimatedContent(
                                    targetState = page,
                                    transitionSpec = {
                                        val direction = if (targetState > initialState) 1 else -1
                                        ((slideInHorizontally(animationSpec = tween(220)) { width -> width * direction } +
                                            scaleIn(animationSpec = tween(220), initialScale = 0.88f)) togetherWith
                                            (slideOutHorizontally(animationSpec = tween(220)) { width -> -width * direction } +
                                                scaleOut(animationSpec = tween(220), targetScale = 0.88f)))
                                            .using(SizeTransform(clip = false))
                                    },
                                    label = "onboarding_tumble"
                                ) { selectedPage ->
                                    val selectedStep = steps[selectedPage]
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        HeroArt(step = selectedStep, page = selectedPage)
                                        Spacer(Modifier.height(18.dp))

                                        Text(
                                            text = selectedStep.title,
                                            color = Color.White,
                                            fontSize = 27.sp,
                                            lineHeight = 31.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(Modifier.height(12.dp))

                                        if (selectedPage < 4) {
                                            Text(
                                                text = selectedStep.body,
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

                                        if (selectedPage < 4) {
                                            Spacer(Modifier.height(18.dp))
                                            ChipRow(items = selectedStep.chips, accent = selectedStep.accent)
                                        }
                                    }
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
                                            text = if (page == 4) "Get Started →" else "Next ▶",
                                            color = OnboardingNavy,
                                            fontSize = 16.sp,
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
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "Smaran",
            modifier = Modifier.size(68.dp),
            contentScale = ContentScale.Fit
        )
        TextButton(onClick = onSkip, modifier = Modifier.align(Alignment.CenterEnd)) {
            Text(text = "Skip", color = OnboardingMuted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun HeroArt(step: Step, page: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 340.dp, max = 580.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .clip(CircleShape)
                .background(step.accent.copy(alpha = 0.10f))
        )
        Image(
            painter = painterResource(id = listOf(
                R.drawable.onboarding_page_1,
                R.drawable.onboarding_page_2,
                R.drawable.onboarding_page_3,
                R.drawable.onboarding_page_4,
                R.drawable.onboarding_page_5
            )[page]),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 340.dp, max = 580.dp)
                .scale(1.15f),
            contentScale = ContentScale.Fit
        )
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
            NameInputField(nameInput, onNameChange)
        }
    }
}

@Composable
private fun NameInputField(value: String, onValueChange: (String) -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    val isEmpty = value.isBlank()
    val transition = rememberInfiniteTransition(label = "name_input_border")
    val shimmerPosition by transition.animateFloat(
        initialValue = -0.85f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "name_input_shimmer"
    )
    val field = @Composable {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = shape,
            label = { Text("Your name") },
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
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = if (isEmpty) Color.Transparent else OnboardingPurpleSoft,
                unfocusedBorderColor = if (isEmpty) Color.Transparent else Color.White.copy(alpha = 0.16f),
                cursorColor = OnboardingPurple,
                focusedLabelColor = OnboardingPurple,
                unfocusedLabelColor = OnboardingMuted,
                focusedPlaceholderColor = OnboardingMuted.copy(alpha = 0.85f),
                unfocusedPlaceholderColor = OnboardingMuted.copy(alpha = 0.85f)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), shape)
            .border(
                1.5.dp,
                if (isEmpty) Brush.linearGradient(
                    colors = listOf(
                        OnboardingPurpleSoft.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.9f),
                        OnboardingPurpleSoft.copy(alpha = 0.35f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(shimmerPosition * 360f, 0f),
                    end = androidx.compose.ui.geometry.Offset(shimmerPosition * 360f + 240f, 120f)
                ) else Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.Transparent)
                ),
                shape
            )
            .padding(1.dp)
    ) {
        field()
    }
}

@Composable
private fun ChipRow(items: List<String>, accent: Color) {
    var glowingIndex by remember(items) { mutableIntStateOf(0) }

    LaunchedEffect(items) {
        while (true) {
            kotlinx.coroutines.delay(900L)
            glowingIndex = (glowingIndex + 1) % items.size
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEachIndexed { index, item ->
            val glow by animateFloatAsState(
                targetValue = if (index == glowingIndex) 1f else 0f,
                animationSpec = tween(280),
                label = "onboarding_chip_glow_$index"
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(accent.copy(alpha = 0.12f + glow * 0.24f))
                    .border(1.dp, accent.copy(alpha = 0.22f + glow * 0.58f), RoundedCornerShape(999.dp))
                    .graphicsLayer(alpha = 0.82f + glow * 0.18f)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = item,
                    color = Color.White.copy(alpha = 0.82f + glow * 0.18f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
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
