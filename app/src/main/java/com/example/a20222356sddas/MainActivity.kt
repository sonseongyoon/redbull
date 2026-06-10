package com.example.a20222356sddas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a20222356sddas.data.PreferencesManager
import com.example.a20222356sddas.ui.screens.*
import com.example.a20222356sddas.ui.theme._20222356sddasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val prefs = PreferencesManager(this)

        setContent {
            // Dynamic theme controller state
            var darkThemeSetting by remember { mutableStateOf(prefs.isDarkTheme ?: false) }

            _20222356sddasTheme(darkTheme = darkThemeSetting, dynamicColor = false) {
                // Navigation States
                var isOnboarded by remember { mutableStateOf(prefs.isOnboarded) }
                var isLocked by remember { mutableStateOf(prefs.isPinEnabled) }

                if (!isOnboarded) {
                    OnboardingScreen(
                        prefs = prefs,
                        onComplete = {
                            isOnboarded = true
                            isLocked = prefs.isPinEnabled
                        }
                    )
                } else if (isLocked) {
                    PinLockScreen(
                        correctPin = prefs.pinCode,
                        onSuccess = {
                            isLocked = false
                        }
                    )
                } else {
                    MainAppScaffold(
                        prefs = prefs,
                        onThemeChange = {
                            darkThemeSetting = prefs.isDarkTheme ?: false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    prefs: PreferencesManager,
    onThemeChange: () -> Unit
) {
    var currentTab by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            if (currentTab in listOf("home", "analysis", "measurement", "history", "chat")) {
                NavigationBar(
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    NavigationBarItem(
                        selected = currentTab == "home",
                        onClick = { currentTab = "home" },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "홈") },
                        label = { Text("홈", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    NavigationBarItem(
                        selected = currentTab == "analysis",
                        onClick = { currentTab = "analysis" },
                        icon = { Icon(imageVector = Icons.Default.Analytics, contentDescription = "분석") },
                        label = { Text("분석", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    NavigationBarItem(
                        selected = currentTab == "measurement",
                        onClick = { currentTab = "measurement" },
                        icon = { Icon(imageVector = Icons.Default.AddCircle, contentDescription = "측정") },
                        label = { Text("측정", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    NavigationBarItem(
                        selected = currentTab == "history",
                        onClick = { currentTab = "history" },
                        icon = { Icon(imageVector = Icons.Default.History, contentDescription = "히스토리") },
                        label = { Text("히스토리", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    NavigationBarItem(
                        selected = currentTab == "chat",
                        onClick = { currentTab = "chat" },
                        icon = { Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = "ai 상담") },
                        label = { Text("ai 상담", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "home" -> DashboardScreen(
                    prefs = prefs,
                    onNavigateToGoal = { currentTab = "goal_todo" },
                    onNavigateToSettings = { currentTab = "settings" }
                )
                "analysis" -> AnalysisScreen(prefs = prefs)
                "measurement" -> MeasurementScreen(
                    prefs = prefs,
                    onSave = { currentTab = "home" }
                )
                "history" -> HistoryScreen(prefs = prefs)
                "chat" -> ChatScreen(prefs = prefs)
                
                // Hidden screens accessed from Home
                "goal_todo" -> GoalTodoScreen(prefs = prefs)
                "settings" -> SettingsScreen(
                    prefs = prefs,
                    onThemeChange = onThemeChange
                )
            }
        }
    }
}