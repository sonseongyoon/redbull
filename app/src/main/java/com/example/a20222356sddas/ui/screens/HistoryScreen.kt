package com.example.a20222356sddas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
fun HistoryScreen(
    prefs: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val records = prefs.getInbodyRecords().reversed() // 최신순 정렬

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "측정 히스토리",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "과거 측정 데이터를 조회합니다",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    ) { innerPadding ->
        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F7FA))
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("아직 기록된 측정 데이터가 없습니다.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F7FA))
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(records) { record ->
                    HistoryCard(record = record)
                }
            }
        }
    }
}

fun getRelativeTimeBadge(recordDate: Long): String {
    val diffMs = System.currentTimeMillis() - recordDate
    val diffDays = diffMs / (1000 * 60 * 60 * 24)
    return when {
        diffDays < 0L -> "오늘"
        diffDays == 0L -> "오늘"
        diffDays == 1L -> "어제"
        diffDays in 2L..6L -> "${diffDays}일 전"
        diffDays in 7L..13L -> "1주 전"
        diffDays in 14L..20L -> "2주 전"
        diffDays in 21L..27L -> "3주 전"
        diffDays in 28L..30L -> "4주 전"
        else -> "${diffDays / 30}달 전"
    }
}

@Composable
fun HistoryCard(record: InbodyRecord) {
    val dateStr = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault()).format(Date(record.date))
    val badgeText = getRelativeTimeBadge(record.date)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE3F2FD))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color(0xFF1E88E5),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("체중", fontSize = 12.sp, color = Color.Gray)
                    Text("${String.format(Locale.US, "%.1f", record.weight)}kg", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("체지방률", fontSize = 12.sp, color = Color.Gray)
                    Text("${String.format(Locale.US, "%.1f", record.bodyFatPercentage)}%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SubMetricItem("근육량", "${String.format(Locale.US, "%.1f", record.skeletalMuscleMass)}kg")
                SubMetricItem("BMI", if (record.bmi > 0) String.format(Locale.US, "%.1f", record.bmi) else "-")
                SubMetricItem("체수분", if (record.bodyWaterPercentage > 0) "${String.format(Locale.US, "%.1f", record.bodyWaterPercentage)}%" else "-")
                SubMetricItem("내장지방", if (record.visceralFatLevel > 0) "${record.visceralFatLevel.toInt()}Lv" else "-")
            }
        }
    }
}

@Composable
fun SubMetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}
