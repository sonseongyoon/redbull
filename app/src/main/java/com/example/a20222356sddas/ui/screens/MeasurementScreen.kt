package com.example.a20222356sddas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a20222356sddas.data.InbodyRecord
import com.example.a20222356sddas.data.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementScreen(
    prefs: PreferencesManager,
    onSave: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var weightStr by remember { mutableStateOf("") }
    var fatStr by remember { mutableStateOf("") }
    var muscleStr by remember { mutableStateOf("") }
    var visceralFatStr by remember { mutableStateOf("") }
    var boneMassStr by remember { mutableStateOf("") }
    var bmrStr by remember { mutableStateOf("") }
    var waterStr by remember { mutableStateOf("") }

    var saveStatus by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "새로운 측정",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "체성분 데이터를 입력하세요",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Measurement Inputs
            MeasurementInputCard(
                icon = Icons.Default.MonitorWeight,
                iconColor = Color(0xFF1E88E5),
                title = "체중",
                unit = "kg",
                value = weightStr,
                onValueChange = { weightStr = it },
                placeholder = "체중 입력"
            )
            MeasurementInputCard(
                icon = Icons.Default.ShowChart,
                iconColor = Color(0xFFFF7043),
                title = "체지방률",
                unit = "%",
                value = fatStr,
                onValueChange = { fatStr = it },
                placeholder = "체지방률 입력"
            )
            MeasurementInputCard(
                icon = Icons.Default.TrendingUp,
                iconColor = Color(0xFF43A047),
                title = "근육량",
                unit = "kg",
                value = muscleStr,
                onValueChange = { muscleStr = it },
                placeholder = "근육량 입력"
            )
            MeasurementInputCard(
                icon = Icons.Default.WaterDrop,
                iconColor = Color(0xFF29B6F6),
                title = "체수분",
                unit = "%",
                value = waterStr,
                onValueChange = { waterStr = it },
                placeholder = "체수분 입력",
                isRequired = false
            )
            MeasurementInputCard(
                icon = Icons.Outlined.FavoriteBorder,
                iconColor = Color(0xFFE53935),
                title = "내장지방",
                unit = "level",
                value = visceralFatStr,
                onValueChange = { visceralFatStr = it },
                placeholder = "내장지방 입력",
                isRequired = false
            )
            MeasurementInputCard(
                icon = Icons.Default.Timeline,
                iconColor = Color(0xFFAB47BC),
                title = "골량",
                unit = "kg",
                value = boneMassStr,
                onValueChange = { boneMassStr = it },
                placeholder = "골량 입력",
                isRequired = false
            )
            MeasurementInputCard(
                icon = Icons.Default.Bolt,
                iconColor = Color(0xFFFFA000),
                title = "기초대사량",
                unit = "kcal",
                value = bmrStr,
                onValueChange = { bmrStr = it },
                placeholder = "기초대사량 입력",
                isRequired = false
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Save Button
            Button(
                onClick = {
                    val w = weightStr.toDoubleOrNull()
                    val f = fatStr.toDoubleOrNull()
                    val m = muscleStr.toDoubleOrNull()
                    if (w != null && f != null && m != null) {
                        val newRecord = InbodyRecord(
                            date = System.currentTimeMillis(),
                            weight = w,
                            bodyFatPercentage = f,
                            skeletalMuscleMass = m,
                            bmi = w / ((1.75) * (1.75)), // 임시 키 175cm 기준 (이후 사용자 정보 연동 가능)
                            visceralFatLevel = visceralFatStr.toDoubleOrNull() ?: 0.0,
                            boneMass = boneMassStr.toDoubleOrNull() ?: 0.0,
                            bmr = bmrStr.toDoubleOrNull() ?: 0.0,
                            bodyWaterPercentage = waterStr.toDoubleOrNull() ?: 0.0
                        )
                        val records = prefs.getInbodyRecords().toMutableList()
                        records.add(newRecord)
                        prefs.saveInbodyRecords(records)

                        saveStatus = "✅ 측정 결과가 저장되었습니다."
                        weightStr = ""; fatStr = ""; muscleStr = ""
                        visceralFatStr = ""; boneMassStr = ""; bmrStr = ""; waterStr = ""
                        onSave()
                    } else {
                        saveStatus = "❌ 필수 항목(*표시)을 모두 숫자로 입력해 주세요."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(listOf(Color(0xFF1E88E5), Color(0xFF00ACC1)))),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "저장", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("측정 결과 저장", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (saveStatus.isNotEmpty()) {
                Text(
                    text = saveStatus,
                    color = if (saveStatus.startsWith("✅")) Color(0xFF43A047) else Color(0xFFE53935),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Tip Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE3F2FD))
                    .padding(16.dp)
            ) {
                Text(
                    text = "💡 팁: 정확한 측정을 위해 아침 공복 상태에서 측정하는 것을 권장합니다.",
                    fontSize = 13.sp,
                    color = Color(0xFF1565C0)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementInputCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    unit: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isRequired: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        if (isRequired) {
                            Text(text = " *", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(text = unit, color = Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = iconColor,
                    unfocusedBorderColor = Color.LightGray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                singleLine = true
            )
        }
    }
}
