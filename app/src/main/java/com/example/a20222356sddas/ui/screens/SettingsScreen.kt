package com.example.a20222356sddas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a20222356sddas.data.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: PreferencesManager,
    onThemeChange: () -> Unit,
    modifier: Modifier = Modifier
) {

    // Security PIN State
    var pinEnabled by remember { mutableStateOf(prefs.isPinEnabled) }
    var pin1Input by remember { mutableStateOf("") }
    var pin2Input by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }

    // Demographics Profile State
    var nameInput by remember { mutableStateOf(prefs.userName) }
    var ageInput by remember { mutableStateOf(prefs.userAge.toString()) }
    var heightInput by remember { mutableStateOf(prefs.userHeight.toString()) }
    var genderInput by remember { mutableStateOf(prefs.userGender) }

    var saveProfileSuccess by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정 및 프로필", fontWeight = FontWeight.Bold) },
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
            // 1. Demographics Profile Edit Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("신체 프로필 설정", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("이름") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = ageInput,
                            onValueChange = { ageInput = it.filter { c -> c.isDigit() } },
                            label = { Text("나이 (세)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it },
                            label = { Text("키 (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Text("성별", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("남성", "여성").forEach { item ->
                            val isSelected = genderInput == item
                            Button(
                                onClick = { genderInput = item },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(item, fontSize = 11.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val age = ageInput.toIntOrNull()
                            val height = heightInput.toDoubleOrNull()
                            if (nameInput.trim().isNotEmpty() && age != null && height != null) {
                                prefs.userName = nameInput.trim()
                                prefs.userAge = age
                                prefs.userHeight = height
                                prefs.userGender = genderInput
                                saveProfileSuccess = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("프로필 저장")
                    }

                    if (saveProfileSuccess) {
                        Text("신체 정보 프로필이 업데이트되었습니다.", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. Security Lock Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("보안 및 2차 잠금", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PIN 번호 잠금 활성화", fontSize = 13.sp)
                        Switch(
                            checked = pinEnabled,
                            onCheckedChange = { checked ->
                                pinEnabled = checked
                                if (!checked) {
                                    prefs.isPinEnabled = false
                                    prefs.pinCode = ""
                                }
                            }
                        )
                    }

                    if (pinEnabled) {
                        OutlinedTextField(
                            value = pin1Input,
                            onValueChange = { if (it.length <= 4) pin1Input = it.filter { c -> c.isDigit() } },
                            label = { Text("새 4자리 PIN 번호") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = pin2Input,
                            onValueChange = { if (it.length <= 4) pin2Input = it.filter { c -> c.isDigit() } },
                            label = { Text("PIN 번호 재입력") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (pin1Input.length == 4 && pin2Input.length == 4) {
                                    if (pin1Input == pin2Input) {
                                        prefs.pinCode = pin1Input
                                        prefs.isPinEnabled = true
                                        pinError = "PIN 비밀번호가 등록/변경되었습니다."
                                        pin1Input = ""
                                        pin2Input = ""
                                    } else {
                                        pinError = "비밀번호가 일치하지 않습니다."
                                    }
                                } else {
                                    pinError = "4자리 숫자를 온전히 기입해 주세요."
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("비밀번호 변경 적용")
                        }

                        if (pinError.isNotEmpty()) {
                            Text(pinError, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (pinError.contains("등록")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // 4. Dark Theme Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("다크 모드 적용", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Switch(
                        checked = prefs.isDarkTheme == true,
                        onCheckedChange = { checked ->
                            prefs.isDarkTheme = checked
                            onThemeChange()
                        }
                    )
                }
            }

            // 5. App Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("개발자 및 과목 정보", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text("작성자: 손성윤 (학번: 20222356)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("과목: 모바일프로그래밍 기말 과제", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("앱 버전: v1.0.0 (Android API 24+ 호환)", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}
