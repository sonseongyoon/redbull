package com.example.a20222356sddas.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a20222356sddas.data.ChatMessage
import com.example.a20222356sddas.data.PreferencesManager
import com.example.a20222356sddas.network.GeminiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    prefs: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var chatMessages by remember { mutableStateOf(prefs.getChatMessages()) }
    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val quickPrompts = listOf(
        "🏋️‍♂️ 오늘 맞춤 루틴 짜줘!" to "오늘 제가 할 수 있는 1:1 맞춤형 근력 운동 루틴을 추천해 주세요! 세트수와 횟수도 알려주세요.",
        "🥗 탄단지 식단 비율 추천" to "제 인바디 상태를 고려했을 때, 하루에 섭취해야 할 탄수화물, 단백질, 지방의 비율과 추천 식단 가이드를 알려주세요.",
        "📈 인바디 분석 및 목표 팁" to "제 최근 인바디 변화 추이를 종합적으로 분석해 주시고, 앞으로 운동 방향성에 대한 PT 조언 부탁드려요!"
    )

    // Auto-scroll to bottom when messages list size changes
    LaunchedEffect(chatMessages.size, isThinking) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFAB47BC), Color(0xFFEC407A))
                        )
                    )
            ) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Column {
                                Text("Gemini AI 건강 상담소", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                Text("실시간 건강 조언 & 식단 상담", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                prefs.clearChatHistory()
                                chatMessages = emptyList()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "대화 초기화",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Chat Logs
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (chatMessages.isEmpty()) {
                    // Empty Welcome State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "안녕하세요! AI 헬스 카운셀러입니다.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "식단, 권장 영양소 섭취 비율, 웨이트 트레이닝 루틴 등 신체 성장과 건강관리에 대한 궁금증을 물어보세요! 최근 인바디 지표를 기반으로 답변해 드립니다.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chatMessages) { message ->
                            ChatBubble(message = message)
                        }

                        if (isThinking) {
                            item {
                                TypingIndicatorBubble()
                            }
                        }
                    }
                }
            }

            // Quick Suggestion Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for ((label, promptText) in quickPrompts) {
                    AssistChip(
                        onClick = {
                            if (!isThinking) {
                                val userMsg = ChatMessage(text = label, isUser = true)
                                prefs.addChatMessage(userMsg)
                                chatMessages = prefs.getChatMessages()

                                coroutineScope.launch {
                                    isThinking = true
                                    delay(1000)
                                    val records = prefs.getInbodyRecords()
                                    val replyText = GeminiClient.sendChatMessage(
                                        chatHistory = chatMessages,
                                        currentMessage = promptText,
                                        allRecords = records,
                                        apiKey = prefs.geminiApiKey,
                                        userName = prefs.userName,
                                        gender = prefs.userGender,
                                        height = prefs.userHeight,
                                        age = prefs.userAge
                                    )
                                    val aiMsg = ChatMessage(text = replyText, isUser = false)
                                    prefs.addChatMessage(aiMsg)
                                    chatMessages = prefs.getChatMessages()
                                    isThinking = false
                                }
                            }
                        },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        enabled = !isThinking,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (!isThinking) {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            }
                        )
                    )
                }
            }

            // Input Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("AI 트레이너에게 질문하기...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent
                        ),
                        maxLines = 4
                    )

                    IconButton(
                        onClick = {
                            val msgText = inputText.trim()
                            if (msgText.isNotEmpty() && !isThinking) {
                                inputText = ""
                                // 1. Save user message
                                val userMsg = ChatMessage(text = msgText, isUser = true)
                                prefs.addChatMessage(userMsg)
                                chatMessages = prefs.getChatMessages()

                                // 2. Trigger AI reply
                                coroutineScope.launch {
                                    isThinking = true
                                    delay(1000) // Visual thinking state duration
                                    val records = prefs.getInbodyRecords()
                                    val replyText = GeminiClient.sendChatMessage(
                                        chatHistory = chatMessages,
                                        currentMessage = msgText,
                                        allRecords = records,
                                        apiKey = prefs.geminiApiKey,
                                        userName = prefs.userName,
                                        gender = prefs.userGender,
                                        height = prefs.userHeight,
                                        age = prefs.userAge
                                    )
                                    val aiMsg = ChatMessage(text = replyText, isUser = false)
                                    prefs.addChatMessage(aiMsg)
                                    chatMessages = prefs.getChatMessages()
                                    isThinking = false
                                }
                            }
                        },
                        enabled = inputText.trim().isNotEmpty() && !isThinking,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                brush = if (inputText.trim().isNotEmpty() && !isThinking) {
                                    Brush.horizontalGradient(listOf(Color(0xFFAB47BC), Color(0xFFEC407A)))
                                } else {
                                    Brush.horizontalGradient(listOf(Color.LightGray, Color.LightGray))
                                },
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "전송",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

fun parseMarkdownToAnnotatedString(text: String): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        for (i in parts.indices) {
            if (i % 2 == 1) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(parts[i])
                }
            } else {
                append(parts[i])
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val containerColor = if (message.isUser) {
        Color(0xFF1E88E5) // Clean modern blue
    } else {
        Color(0xFFF5F7FA) // Clean light grey/blue
    }
    val contentColor = if (message.isUser) Color.White else Color.Black

    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(18.dp, 18.dp, 2.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 2.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(containerColor)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = parseMarkdownToAnnotatedString(message.text),
                color = contentColor,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun TypingIndicatorBubble() {
    val bubbleShape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 2.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(bubbleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular pulsating dots simulation
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                
                listOf(0, 150, 300).forEach { delayTime ->
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 600, delayMillis = delayTime),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = scale))
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "PT 트레이너 분석 중...",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
