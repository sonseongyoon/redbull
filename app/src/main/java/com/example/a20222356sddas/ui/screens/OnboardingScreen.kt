package com.example.a20222356sddas.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a20222356sddas.data.InbodyRecord
import com.example.a20222356sddas.data.PreferencesManager

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    prefs: PreferencesManager,
    onComplete: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    // Demographics State
    var name by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var heightStr by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("남성") }

    // Initial Inbody State
    var weightStr by remember { mutableStateOf("") }
    var muscleStr by remember { mutableStateOf("") }
    var fatStr by remember { mutableStateOf("") }

    // PIN Security State
    var usePin by remember { mutableStateOf(false) }
    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                for (i in 1..3) {
                    val isSelected = step == i
                    Box(
                        modifier = Modifier
                            .width(if (isSelected) 32.dp else 12.dp)
                            .height(6.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }

            // Animated Screen content
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() with
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() with
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "onboarding_navigation"
            ) { targetStep ->
                when (targetStep) {
                    1 -> {
                        Step1Demographics(
                            name = name,
                            onNameChange = { name = it },
                            age = ageStr,
                            onAgeChange = { ageStr = it },
                            height = heightStr,
                            onHeightChange = { heightStr = it },
                            gender = gender,
                            onGenderChange = { gender = it }
                        )
                    }
                    2 -> {
                        Step2Inbody(
                            weight = weightStr,
                            onWeightChange = { weightStr = it },
                            muscle = muscleStr,
                            onMuscleChange = { muscleStr = it },
                            fat = fatStr,
                            onFatChange = { fatStr = it }
                        )
                    }
                    3 -> {
                        Step3Security(
                            usePin = usePin,
                            onUsePinChange = { usePin = it },
                            pin1 = pin1,
                            onPin1Change = { pin1 = it },
                            pin2 = pin2,
                            onPin2Change = { pin2 = it },
                            errorMsg = errorMsg
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text("이전")
                    }
                }

                Button(
                    onClick = {
                        if (step == 1) {
                            if (name.trim().isEmpty() || ageStr.trim().isEmpty() || heightStr.trim().isEmpty()) {
                                return@Button
                            }
                            step = 2
                        } else if (step == 2) {
                            if (weightStr.trim().isEmpty() || muscleStr.trim().isEmpty() || fatStr.trim().isEmpty()) {
                                return@Button
                            }
                            step = 3
                        } else {
                            // Validate PIN code settings
                            if (usePin) {
                                if (pin1.length != 4 || pin2.length != 4) {
                                    errorMsg = "PIN은 4자리 숫자여야 합니다."
                                    return@Button
                                }
                                if (pin1 != pin2) {
                                    errorMsg = "비밀번호가 일치하지 않습니다."
                                    return@Button
                                }
                                prefs.pinCode = pin1
                                prefs.isPinEnabled = true
                            } else {
                                prefs.pinCode = ""
                                prefs.isPinEnabled = false
                            }

                            // Save demographics
                            prefs.userName = name.trim()
                            prefs.userAge = ageStr.toIntOrNull() ?: 25
                            prefs.userHeight = heightStr.toDoubleOrNull() ?: 175.0
                            prefs.userGender = gender

                            // Save initial Inbody record
                            val initW = weightStr.toDoubleOrNull() ?: 70.0
                            val initM = muscleStr.toDoubleOrNull() ?: 30.0
                            val initF = fatStr.toDoubleOrNull() ?: 20.0
                            val initialRecord = InbodyRecord(
                                date = System.currentTimeMillis(),
                                weight = initW,
                                skeletalMuscleMass = initM,
                                bodyFatPercentage = initF
                            )
                            prefs.saveInbodyRecords(listOf(initialRecord))

                            // Finish Onboarding
                            prefs.isOnboarded = true
                            onComplete()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (step == 3) "시작하기" else "다음")
                }
            }
        }
    }
}

@Composable
fun Step1Demographics(
    name: String,
    onNameChange: (String) -> Unit,
    age: String,
    onAgeChange: (String) -> Unit,
    height: String,
    onHeightChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "반갑습니다! 👋",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "개인 맞춤형 분석을 위해 기본 신체 정보를 입력해 주세요.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("이름") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { if (it.length <= 3) onAgeChange(it.filter { c -> c.isDigit() }) },
                    label = { Text("나이 (세)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = height,
                    onValueChange = { onHeightChange(it) },
                    label = { Text("키 (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("성별 선택", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf("남성", "여성").forEach { item ->
                    val isSelected = gender == item
                    Button(
                        onClick = { onGenderChange(item) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(item)
                    }
                }
            }
        }
    }
}

@Composable
fun Step2Inbody(
    weight: String,
    onWeightChange: (String) -> Unit,
    muscle: String,
    onMuscleChange: (String) -> Unit,
    fat: String,
    onFatChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "인바디 데이터 입력 📊",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "최근 측정하신 인바디 수치를 입력해 주세요.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = weight,
                onValueChange = onWeightChange,
                label = { Text("체중 (kg)") },
                placeholder = { Text("예: 72.5") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = muscle,
                onValueChange = onMuscleChange,
                label = { Text("골격근량 (kg)") },
                placeholder = { Text("예: 32.1") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = fat,
                onValueChange = onFatChange,
                label = { Text("체지방률 (%)") },
                placeholder = { Text("예: 18.5") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun Step3Security(
    usePin: Boolean,
    onUsePinChange: (Boolean) -> Unit,
    pin1: String,
    onPin1Change: (String) -> Unit,
    pin2: String,
    onPin2Change: (String) -> Unit,
    errorMsg: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "앱 보안 설정 🔒",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "개인정보 보호를 위해 PIN 비밀번호를 설정할 수 있습니다.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PIN 번호 잠금 기능 활성화",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Switch(checked = usePin, onCheckedChange = onUsePinChange)
            }

            if (usePin) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pin1,
                    onValueChange = { if (it.length <= 4) onPin1Change(it.filter { c -> c.isDigit() }) },
                    label = { Text("PIN 비밀번호 (4자리)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pin2,
                    onValueChange = { if (it.length <= 4) onPin2Change(it.filter { c -> c.isDigit() }) },
                    label = { Text("PIN 비밀번호 재입력") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        }
    }
}
