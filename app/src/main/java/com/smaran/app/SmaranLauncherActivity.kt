package com.smaran.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class SmaranLauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("smaran_app", MODE_PRIVATE)
        if (prefs.getBoolean("onboarding_done", false)) {
            startActivity(Intent(this, SmaranActivityPhase3::class.java)); finish(); return
        }
        setContent {
            var page by rememberSaveable { mutableIntStateOf(0) }
            val titles = listOf("Welcome to Smaran", "Plan Your Tasks", "Smart Reminders", "Track & Improve", "Let's Achieve More")
            val bodies = listOf("Your intelligent reminder companion.", "Create tasks with date & time and never miss what matters.", "Snooze for 15 minutes, 30 minutes or 1 hour.", "Review completed, pending, missed and rescheduled activity.", "Stay focused, stay productive and achieve your goals.")
            Surface(Modifier.fillMaxSize(), color = Color(0xFF151733)) {
                Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
                    Text("SMARAN", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⌛", fontSize = 72.sp)
                        Text(titles[page], color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(10.dp))
                        Text(bodies[page], color = Color.White.copy(.75f), textAlign = TextAlign.Center)
                    }
                    Row { repeat(5) { Text(if (it == page) "●" else "•", color = Color.White, modifier = Modifier.padding(3.dp)) } }
                    Button(onClick = {
                        if (page == 4) {
                            prefs.edit().putBoolean("onboarding_done", true).apply()
                            startActivity(Intent(this@SmaranLauncherActivity, SmaranActivityPhase3::class.java)); finish()
                        } else page++
                    }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD8C5FF))) { Text(if (page == 4) "Get Started →" else "Next", color = Color(0xFF151733)) }
                }
            }
        }
    }
}
