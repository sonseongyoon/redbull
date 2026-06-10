package com.example.a20222356sddas.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a20222356sddas.data.HealthGoal
import com.example.a20222356sddas.data.PreferencesManager
import com.example.a20222356sddas.data.TodoItem
import com.example.a20222356sddas.network.GeminiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalTodoScreen(
    prefs: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentGoal by remember { mutableStateOf(prefs.getHealthGoal()) }
    var todoItems by remember { mutableStateOf(prefs.getTodoItems()) }

    var targetWeightInput by remember { mutableStateOf(if (currentGoal.targetWeight > 0) currentGoal.targetWeight.toString() else "") }
    var durationWeeksInput by remember { mutableStateOf(currentGoal.durationWeeks.toString()) }

    var warningBanner by remember { mutableStateOf(currentGoal.warningMessage) }
    var isCheckingGoal by remember { mutableStateOf(false) }

    // Custom Todo input state
    var showCustomTodoDialog by remember { mutableStateOf(false) }
    var isGeneratingTodos by remember { mutableStateOf(false) }

    // Celebration Dialog state
    var showCelebrationDialog by remember { mutableStateOf(false) }
    var praiseMessage by remember { mutableStateOf("") }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    
    // Filter today's items
    val todayTodoItems = remember(todoItems) {
        todoItems.filter { it.dateStr == todayStr }
    }

    // Trigger celebration dialog when all of today's items are completed
    val allCompleted = remember(todayTodoItems) {
        todayTodoItems.isNotEmpty() && todayTodoItems.all { it.isCompleted }
    }

    LaunchedEffect(allCompleted) {
        if (allCompleted) {
            isGeneratingTodos = true
            // Simulate Gemini creating a personalized praise message
            delay(500)
            praiseMessage = if (prefs.userName.isNotEmpty()) {
                "🎉 대단합니다, ${prefs.userName}님!\n오늘 계획한 모든 인바디 케어 루틴을 무사히 완료하셨습니다. 꾸준함이 모여 완벽한 신체 성장을 만듭니다. 제미나이가 당신의 건강한 여정을 적극 응원합니다! 내일도 함께 달려볼까요? 💪"
            } else {
                "🎉 완벽합니다!\n오늘의 식단 조절과 피트니스 가이드 루틴을 전부 완료하셨습니다. 매일의 작은 습관이 건강한 신체 변화를 이끌어냅니다. 정말 수고하셨습니다! 🔥"
            }
            showCelebrationDialog = true
            isGeneratingTodos = false
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("AI 목표 & 일일 투두", fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isGeneratingTodos = true
                                val latestRecord = prefs.getInbodyRecords().lastOrNull()
                                val list = GeminiClient.generateTodoList(
                                    latestRecord = latestRecord,
                                    goal = currentGoal,
                                    apiKey = prefs.geminiApiKey,
                                    userName = prefs.userName
                                )
                                // Clear old items for today
                                val filtered = todoItems.filter { it.dateStr != todayStr }
                                val updated = filtered + list
                                prefs.saveTodoItems(updated)
                                todoItems = updated
                                isGeneratingTodos = false
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "AI 투두 추천 받기")
                    }
                    IconButton(onClick = { showCustomTodoDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "할 일 추가")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Goal Setting Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "나의 건강 목표 설정",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = targetWeightInput,
                            onValueChange = { targetWeightInput = it },
                            label = { Text("목표 체중 (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = durationWeeksInput,
                            onValueChange = { durationWeeksInput = it.filter { c -> c.isDigit() } },
                            label = { Text("목표 기간 (주)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    if (isCheckingGoal) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = {
                                val targetW = targetWeightInput.toDoubleOrNull()
                                val durationW = durationWeeksInput.toIntOrNull()

                                if (targetW != null && durationW != null) {
                                    coroutineScope.launch {
                                        isCheckingGoal = true
                                        val latest = prefs.getInbodyRecords().lastOrNull()
                                        val warning = GeminiClient.checkGoalWarning(
                                            targetWeight = targetW,
                                            durationWeeks = durationW,
                                            latestRecord = latest,
                                            apiKey = prefs.geminiApiKey,
                                            userName = prefs.userName
                                        )
                                        warningBanner = warning
                                        val goal = HealthGoal(
                                            targetWeight = targetW,
                                            durationWeeks = durationW,
                                            warningMessage = warning
                                        )
                                        prefs.saveHealthGoal(goal)
                                        currentGoal = goal
                                        isCheckingGoal = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("목표 저장 및 검증")
                        }
                    }
                }
            }

            // 2. Goal Warnings Alert Banner
            warningBanner?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "AI 목표 안전 가이드 경고",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // 3. To-Do List Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "오늘의 실천 루틴 (${todayTodoItems.count { it.isCompleted }}/${todayTodoItems.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (todayTodoItems.isEmpty()) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                isGeneratingTodos = true
                                val latestRecord = prefs.getInbodyRecords().lastOrNull()
                                val list = GeminiClient.generateTodoList(
                                    latestRecord = latestRecord,
                                    goal = currentGoal,
                                    apiKey = prefs.geminiApiKey,
                                    userName = prefs.userName
                                )
                                val updated = todoItems + list
                                prefs.saveTodoItems(updated)
                                todoItems = updated
                                isGeneratingTodos = false
                            }
                        }
                    ) {
                        Text("추천받기", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 4. To-Do List Items
            if (isGeneratingTodos) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else if (todayTodoItems.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.List, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                        Text(
                            "오늘의 할 일이 등록되지 않았습니다.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Text(
                            "상단 새로고침 아이콘이나 우측 '추천받기' 버튼으로 제미나이 추천 식단과 운동 루틴을 받아보세요!",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    todayTodoItems.forEach { item ->
                        TodoCard(
                            item = item,
                            onToggle = {
                                todoItems = prefs.toggleTodoCompleted(item.id)
                            },
                            onDelete = {
                                prefs.deleteTodoItem(item.id)
                                todoItems = prefs.getTodoItems()
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal: Add Custom Todo Dialog
    if (showCustomTodoDialog) {
        var todoTitle by remember { mutableStateOf("") }
        var categorySelected by remember { mutableStateOf("운동") }

        AlertDialog(
            onDismissRequest = { showCustomTodoDialog = false },
            title = { Text("직접 할 일 추가") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = todoTitle,
                        onValueChange = { todoTitle = it },
                        label = { Text("할 일 제목") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("운동", "식단", "기타").forEach { cat ->
                            val isSelected = categorySelected == cat
                            Button(
                                onClick = { categorySelected = cat },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(cat, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (todoTitle.trim().isNotEmpty()) {
                            val newItem = TodoItem(
                                title = todoTitle.trim(),
                                category = categorySelected,
                                dateStr = todayStr
                            )
                            prefs.addTodoItem(newItem)
                            todoItems = prefs.getTodoItems()
                            showCustomTodoDialog = false
                        }
                    }
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTodoDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    // Modal: All Tasks Completed Celebration Dialog
    if (showCelebrationDialog) {
        AlertDialog(
            onDismissRequest = { showCelebrationDialog = false },
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Celebration Emoji / Glow
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "오늘의 루틴 완벽 달성!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = praiseMessage,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCelebrationDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("자랑스럽게 확인!")
                }
            }
        )
    }
}

@Composable
fun TodoCard(
    item: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (item.category) {
        "운동" -> MaterialTheme.colorScheme.primary
        "식단" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Completed Checkbox
                Icon(
                    imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (item.isCompleted) Color(0xFF4CAF50) else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null,
                        color = if (item.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "삭제",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
