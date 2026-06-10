package com.example.a20222356sddas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a20222356sddas.data.InbodyRecord
import com.example.a20222356sddas.data.PreferencesManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    prefs: PreferencesManager,
    onNavigateToGoal: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var records by remember { mutableStateOf(prefs.getInbodyRecords()) }
    val latestRecord = records.lastOrNull()
    val previousRecord = if (records.size > 1) records[records.size - 2] else null

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "InBody AI",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = Color(0xFF1E88E5)
                        )
                        Text(
                            text = "AI 기반 체성분 분석",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Row {
                        Text(
                            text = "투두 🏁",
                            modifier = Modifier
                                .clickable { onNavigateToGoal() }
                                .padding(8.dp),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "설정 ⚙️",
                            modifier = Modifier
                                .clickable { onNavigateToSettings() }
                                .padding(8.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header: Welcome Blue Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF1E88E5), Color(0xFF00ACC1))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(text = "안녕하세요! \uD83D\uDC4B", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val dateStr = latestRecord?.let {
                        SimpleDateFormat("M월 d일", Locale.getDefault()).format(Date(it.date))
                    } ?: "-"
                    
                    Text(text = "최근 측정: $dateStr", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(text = "목표까지", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(text = "-2.8 kg", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .clickable { /* Navigate to Measurement */ }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("새로 측정", color = Color(0xFF1E88E5), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            Text(text = "현재 상태", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)

            // 2x3 Grid
            val gridItems = listOf(
                GridItemData("체중", latestRecord?.weight, previousRecord?.weight, "kg", Icons.Default.MonitorWeight, Color(0xFF1E88E5)),
                GridItemData("체지방률", latestRecord?.bodyFatPercentage, previousRecord?.bodyFatPercentage, "%", Icons.Default.ShowChart, Color(0xFFFF7043)),
                GridItemData("근육량", latestRecord?.skeletalMuscleMass, previousRecord?.skeletalMuscleMass, "kg", Icons.Default.TrendingUp, Color(0xFF43A047)),
                GridItemData("BMI", latestRecord?.bmi, previousRecord?.bmi, "", Icons.Default.Timeline, Color(0xFFAB47BC)),
                GridItemData("체수분", latestRecord?.bodyWaterPercentage, previousRecord?.bodyWaterPercentage, "%", Icons.Default.WaterDrop, Color(0xFF29B6F6)),
                GridItemData("기초대사량", latestRecord?.bmr, previousRecord?.bmr, "kcal", Icons.Default.Bolt, Color(0xFFFFA000))
            )

            for (i in gridItems.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCard(modifier = Modifier.weight(1f), data = gridItems[i])
                    if (i + 1 < gridItems.size) {
                        MetricCard(modifier = Modifier.weight(1f), data = gridItems[i + 1])
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // AI Insight Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF3E5F5))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFAB47BC), Color(0xFFEC407A)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI 인사이트",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF4A148C)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val insightText = if (latestRecord != null && previousRecord != null) {
                        val weightDiff = latestRecord.weight - previousRecord.weight
                        val muscleDiff = latestRecord.skeletalMuscleMass - previousRecord.skeletalMuscleMass
                        val weightStr = if (weightDiff < 0) "${String.format(Locale.getDefault(), "%.1f", -weightDiff)}kg 감소했습니다!" else if (weightDiff > 0) "${String.format(Locale.getDefault(), "%.1f", weightDiff)}kg 증가했습니다!" else "체중이 유지되었습니다."
                        val muscleStr = if (muscleDiff < 0) "근육량은 ${String.format(Locale.getDefault(), "%.1f", -muscleDiff)}kg 감소하여" else if (muscleDiff > 0) "근육량은 ${String.format(Locale.getDefault(), "%.1f", muscleDiff)}kg 증가하여" else "근육량은 유지하여"
                        "지난주 대비 체중이 $weightStr $muscleStr 건강한 체중 관리를 하고 계시네요. \uD83D\uDC4F"
                    } else {
                        "충분한 기록이 모이면 AI가 건강 상태를 분석해 드립니다."
                    }
                    
                    Text(
                        text = insightText,
                        fontSize = 14.sp,
                        color = Color(0xFF6A1B9A),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "AI 상담받기 →",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E24AA)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

data class GridItemData(
    val title: String,
    val currentValue: Double?,
    val prevValue: Double?,
    val unit: String,
    val icon: ImageVector,
    val iconColor: Color
)

@Composable
fun MetricCard(modifier: Modifier = Modifier, data: GridItemData) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Row: Icon and Diff
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(data.iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = data.icon, contentDescription = null, tint = data.iconColor, modifier = Modifier.size(24.dp))
                }
                
                if (data.currentValue != null && data.prevValue != null && data.prevValue > 0) {
                    val diff = data.currentValue - data.prevValue
                    val diffStr = String.format(Locale.getDefault(), "%+.1f", diff)
                    val color = if (diff > 0) Color(0xFFE53935) else if (diff < 0) Color(0xFF43A047) else Color.Gray
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (diff > 0) "↗" else if (diff < 0) "↘" else "-",
                            fontSize = 12.sp,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = diffStr.replace("+", "").replace("-", ""),
                            fontSize = 14.sp,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Bottom Row: Title and Value
            Text(text = data.title, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            val valueStr = if (data.currentValue != null && data.currentValue > 0) String.format(Locale.getDefault(), "%.1f", data.currentValue) else "-"
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = valueStr, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                if (data.unit.isNotEmpty()) {
                    Text(text = data.unit, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 3.dp))
                }
            }
        }
    }
}
