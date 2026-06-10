package com.example.a20222356sddas.network

import com.example.a20222356sddas.data.ChatMessage
import com.example.a20222356sddas.data.HealthGoal
import com.example.a20222356sddas.data.InbodyRecord
import com.example.a20222356sddas.data.TodoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.a20222356sddas.BuildConfig
import kotlin.math.abs

object GeminiClient {
    private val EMBEDDED_API_KEY = BuildConfig.GEMINI_API_KEY

    /**
     * Performs PII masking on user data prior to sending it to the API.
     * Replaces names, emails, phone numbers, and other potential identifying labels.
     */
    private fun maskPII(text: String, userName: String): String {
        var masked = text
        if (userName.isNotEmpty()) {
            masked = masked.replace(userName, "사용자(Masked)")
        }
        // General email pattern masking
        masked = masked.replace(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"), "email@masked.com")
        // General phone number pattern masking
        masked = masked.replace(Regex("\\d{2,3}-\\d{3,4}-\\d{4}"), "010-XXXX-XXXX")
        return masked
    }

    /**
     * Calls Gemini API via HttpURLConnection.
     */
    private suspend fun callGeminiApi(prompt: String, apiKey: String): String = withContext(Dispatchers.IO) {
        val activeKey = apiKey.ifEmpty { EMBEDDED_API_KEY }
        val urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$activeKey"
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            // Request body JSON construction
            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            val wr = OutputStreamWriter(connection.outputStream)
            wr.write(requestJson.toString())
            wr.flush()
            wr.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                // Parse the response
                val responseJson = JSONObject(response.toString())
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text")
                    }
                }
                return@withContext "API 응답을 해석할 수 없습니다."
            } else {
                val reader = BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream, "UTF-8"))
                val errorResponse = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    errorResponse.append(line)
                }
                reader.close()
                return@withContext "에러 코드: $responseCode\n상세 정보: $errorResponse"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "네트워크 오류가 발생했습니다: ${e.localizedMessage}"
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * FR-02: Generates customized comprehensive analysis report.
     */
    suspend fun generateAnalysisReport(
        records: List<InbodyRecord>,
        apiKey: String,
        userName: String,
        gender: String,
        height: Double,
        age: Int
    ): String {
        if (records.isEmpty()) {
            return "기록된 인바디 데이터가 없습니다. 먼저 첫 번째 데이터를 등록해 주세요!"
        }

        val first = records.first()
        val latest = records.last()

        val weightDiff = latest.weight - first.weight
        val muscleDiff = latest.skeletalMuscleMass - first.skeletalMuscleMass
        val fatDiff = latest.bodyFatPercentage - first.bodyFatPercentage

        val prompt = """
            당신은 전문 스포츠 의학 및 영양학 전문가이자 헬스 트레이너입니다.
            다음 사용자의 신체 프로필 및 인바디 데이터 변화 상태를 분석하여 맞춤형 피드백 리포트를 정중하고 전문적인 톤으로 작성해 주세요.
            
            사용자 성별: $gender, 나이: ${age}세, 키: $height cm
            
            최초 측정일: ${formatDate(first.date)}
            - 체중: ${first.weight} kg
            - 골격근량: ${first.skeletalMuscleMass} kg
            - 체지방률: ${first.bodyFatPercentage} %
            
            최근 측정일: ${formatDate(latest.date)}
            - 체중: ${latest.weight} kg
            - 골격근량: ${latest.skeletalMuscleMass} kg
            - 체지방률: ${latest.bodyFatPercentage} %
            
            변화량:
            - 체중 변화: ${String.format("%.1f", weightDiff)} kg
            - 골격근량 변화: ${String.format("%.1f", muscleDiff)} kg
            - 체지방률 변화: ${String.format("%.1f", fatDiff)} %
            
            보고서 구조:
            1. 📊 **인바디 데이터 비교 요약**: 변화량과 현재 수치 평가
            2. 💬 **AI 종합 피드백**: 골격근량 증감, 체지방 증감에 대한 운동 및 식사 조언
            3. 💡 **향후 행동 제안**: 구체적으로 오늘부터 실천해야 할 권장 단백질 섭취량과 운동 횟수 등
        """.trimIndent()

        val maskedPrompt = maskPII(prompt, userName)

        val activeKey = apiKey.ifEmpty { EMBEDDED_API_KEY }
        if (activeKey.isNotEmpty()) {
            val response = callGeminiApi(maskedPrompt, activeKey)
            if (!response.contains("에러 코드") && !response.contains("네트워크 오류")) {
                return response
            }
        }

        // High-Fidelity Local Simulator Fallback
        return simulateAnalysisReport(first, latest, gender, age, height)
    }

    /**
     * FR-03: Real-time health chat helper.
     */
    suspend fun sendChatMessage(
        chatHistory: List<ChatMessage>,
        currentMessage: String,
        allRecords: List<InbodyRecord>,
        apiKey: String,
        userName: String,
        gender: String,
        height: Double,
        age: Int
    ): String {
        val inbodyContext = if (allRecords.isNotEmpty()) {
            val first = allRecords.first()
            val latest = allRecords.last()
            val weightDiff = latest.weight - first.weight
            val muscleDiff = latest.skeletalMuscleMass - first.skeletalMuscleMass
            val fatDiff = latest.bodyFatPercentage - first.bodyFatPercentage
            """
            [회원 인바디 상태 및 변화 추이]
            - 최초 측정일(${formatDate(first.date)}): 체중 ${first.weight}kg, 골격근량 ${first.skeletalMuscleMass}kg, 체지방률 ${first.bodyFatPercentage}%
            - 최근 측정일(${formatDate(latest.date)}): 체중 ${latest.weight}kg, 골격근량 ${latest.skeletalMuscleMass}kg, 체지방률 ${latest.bodyFatPercentage}%
            - 측정치 총 변화량: 체중 ${String.format("%.1f", weightDiff)}kg, 골격근량 ${String.format("%.1f", muscleDiff)}kg, 체지방률 ${String.format("%.1f", fatDiff)}%
            """.trimIndent()
        } else {
            "사용자의 인바디 기록이 아직 없습니다."
        }

        val historyPrompt = chatHistory.takeLast(10).joinToString("\n") {
            if (it.isUser) "회원: ${it.text}" else "트레이너: ${it.text}"
        }

        val prompt = """
            당신은 회원님의 건강을 끝까지 책임지는 열정적이고 전문적인 1:1 퍼스널 트레이너(PT) 선생님입니다.
            다음 지침에 맞춰 회원님의 건강/식단/운동 질문에 답변해 주세요:
            
            1. **어조**: 항상 친절하고 에너지가 넘치며, 질문자인 사용자를 반드시 "회원님!" 또는 "회원님,"으로 지칭하세요. 격려와 칭찬을 아끼지 않는 든든한 PT 쌤의 느낌을 주어야 합니다.
            2. **개인화**: 회원님의 기본 정보와 인바디 추세 변화 정보를 적극적으로 참고하여 맞춤형으로 이야기하세요. (예: "회원님, 이전보다 골격근이 0.5kg 느셨으니 지금처럼 하시면 됩니다!")
            3. **답변 구성**: 운동 루틴이나 식단 추천을 요구할 때는 단계를 나누어 번호 매김이나 글머리 기호(마크다운 굵은 글씨 등)를 사용하여 매우 직관적이고 알아보기 쉽게 작성해 주세요. (예: **1. 스쿼트**: 4세트 x 12회)
            4. **길이 제한**: 350자 내외로 핵심만 짚어서 전달하세요.
            
            [회원 기본 프로필]
            성별: $gender, 나이: ${age}세, 키: $height cm
            
            $inbodyContext
            
            [이전 대화 기록]
            $historyPrompt
            
            [회원의 현재 질문]
            $currentMessage
        """.trimIndent()

        val maskedPrompt = maskPII(prompt, userName)

        val activeKey = apiKey.ifEmpty { EMBEDDED_API_KEY }
        if (activeKey.isNotEmpty()) {
            val response = callGeminiApi(maskedPrompt, activeKey)
            if (!response.contains("에러") && !response.contains("오류")) {
                return response
            }
        }

        val latestRecord = allRecords.lastOrNull()
        return simulateChatResponse(currentMessage, latestRecord, gender, age, height)
    }

    /**
     * FR-04: Health Goal Warning checker.
     * Returns a warning message string if dangerous, or null if safe.
     */
    suspend fun checkGoalWarning(
        targetWeight: Double,
        durationWeeks: Int,
        latestRecord: InbodyRecord?,
        apiKey: String,
        userName: String
    ): String? {
        if (latestRecord == null) return null

        val currentWeight = latestRecord.weight
        val totalChange = targetWeight - currentWeight
        val weeklyChange = totalChange / durationWeeks
        val absWeeklyChange = abs(weeklyChange)

        // API Key logic (Ask Gemini if the goal is healthy)
        val activeKey = apiKey.ifEmpty { EMBEDDED_API_KEY }
        if (activeKey.isNotEmpty()) {
            val prompt = """
                체중 조절 목표의 건강상 적합성 여부를 판단해 주세요.
                현재 체중: $currentWeight kg
                목표 체중: $targetWeight kg
                목표 기간: $durationWeeks 주
                
                주당 변동율: ${String.format("%.2f", weeklyChange)} kg/주
                
                만약 주당 감량폭이 1.0kg을 초과하거나 주당 증량폭이 0.7kg을 초과하여 건강상 위험할 경우,
                경고 메시지(⚠️ 경고로 시작)와 구체적인 대안 수치(주당 0.5kg 권장 등) 및 추천 주간/기간을 한글로 작성해 주세요.
                만약 매우 건강하고 무난한 계획이라면 "SAFE" 라고만 답변해 주세요.
            """.trimIndent()
            val maskedPrompt = maskPII(prompt, userName)
            val response = callGeminiApi(maskedPrompt, activeKey)
            if (!response.contains("에러") && !response.contains("오류")) {
                val cleanResponse = response.trim()
                if (cleanResponse.uppercase().contains("SAFE") && cleanResponse.length < 10) {
                    return null
                }
                return cleanResponse
            }
        }

        // Local Simulation Warning Checks
        if (weeklyChange < -1.0) {
            // Dangerous weight loss (> 1.0kg per week)
            val recommendedWeeks = (abs(totalChange) / 0.5).toInt().coerceAtLeast(4)
            return "⚠️ **주의: 너무 급격한 감량 계획입니다!**\n현재 계획은 일주일에 약 ${String.format("%.2f", absWeeklyChange)}kg 감량이 필요합니다. 이는 면역력 저하, 근손실 및 요요 현상을 유발할 수 있습니다.\n\n💡 **AI 대안 제안**:\n안전한 감량 속도인 주당 **0.5kg**을 목표로 하여, 목표 기간을 최소 **${recommendedWeeks}주** 이상으로 늘리는 것을 강력히 권장합니다."
        } else if (weeklyChange > 0.7) {
            // Dangerous weight gain (> 0.7kg per week)
            val recommendedWeeks = (abs(totalChange) / 0.3).toInt().coerceAtLeast(4)
            return "⚠️ **주의: 너무 빠른 체중 증량 계획입니다!**\n현재 계획은 일주일에 약 ${String.format("%.2f", absWeeklyChange)}kg 증량이 필요합니다. 이는 골격근 증가보다 급격한 체지방 축적으로 이어질 우려가 큽니다.\n\n💡 **AI 대안 제안**:\n건강한 린매스업(Lean Mass Up)을 위해 주당 **0.3kg** 이하 증량을 목표로 잡고, 기간을 **${recommendedWeeks}주**로 설정해 보세요."
        }

        return null
    }

    /**
     * FR-05: Generates today's tailored Todo List recommendations.
     */
    suspend fun generateTodoList(
        latestRecord: InbodyRecord?,
        goal: HealthGoal,
        apiKey: String,
        userName: String
    ): List<TodoItem> {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val activeKey = apiKey.ifEmpty { EMBEDDED_API_KEY }
        if (activeKey.isNotEmpty()) {
            val currentWeight = latestRecord?.weight ?: 70.0
            val goalWeight = goal.targetWeight
            val direction = if (goalWeight >= currentWeight) "벌크업/근육량 증가" else "다이어트/체지방 감소"
            val prompt = """
                현재 체중 ${currentWeight}kg이고 목표가 '${direction}'인 사용자를 위해, 오늘 실천해야 할 최적의 맞춤형 운동 루틴 2가지와 식단 관리 3가지를 추천해 주세요.
                형식은 반드시 다음과 같은 JSON Array 형식으로만 반환해 주세요. 추가 텍스트나 markdown 코드 블록(```json) 없이 순수 JSON 텍스트만 출력해야 합니다.
                
                [
                  {"title": "운동 항목 내용 1", "category": "운동"},
                  {"title": "운동 항목 내용 2", "category": "운동"},
                  {"title": "식단 항목 내용 1", "category": "식단"},
                  {"title": "식단 항목 내용 2", "category": "식단"},
                  {"title": "식단 항목 내용 3", "category": "식단"}
                ]
            """.trimIndent()
            val response = callGeminiApi(maskPII(prompt, userName), activeKey)
            try {
                // Strip markdown fence if Gemini returns it
                var cleanJson = response.trim()
                if (cleanJson.startsWith("```")) {
                    cleanJson = cleanJson.substringAfter("\n").substringBeforeLast("```").trim()
                }
                val array = JSONArray(cleanJson)
                val list = mutableListOf<TodoItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        TodoItem(
                            title = obj.getString("title"),
                            category = obj.getString("category"),
                            dateStr = todayStr
                        )
                    )
                }
                if (list.isNotEmpty()) return list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Local Simulation Fallback
        val isDiet = goal.targetWeight < (latestRecord?.weight ?: 70.0)
        return if (isDiet) {
            listOf(
                TodoItem(title = "아침: 바나나 1개와 저지방 우유 1잔", category = "식단", dateStr = todayStr),
                TodoItem(title = "점심: 닭가슴살 100g, 현미밥 150g, 브로콜리", category = "식단", dateStr = todayStr),
                TodoItem(title = "저녁: 두부 샐러드와 삶은 계란 2개", category = "식단", dateStr = todayStr),
                TodoItem(title = "공복 유산소 운동 (러닝 또는 빠른 걷기) 40분", category = "운동", dateStr = todayStr),
                TodoItem(title = "스쿼트 15회 4세트 & 푸쉬업 12회 3세트", category = "운동", dateStr = todayStr)
            )
        } else {
            listOf(
                TodoItem(title = "매 식사 시 단백질 급원(육류/생선/계란) 꼭 포함하기", category = "식단", dateStr = todayStr),
                TodoItem(title = "간식: 바나나 1개와 구운 계란 2개", category = "식단", dateStr = todayStr),
                TodoItem(title = "물 2.5리터 이상 섭취하여 수분 및 대사 유지", category = "식단", dateStr = todayStr),
                TodoItem(title = "웨이트 트레이닝 (3대 대근육 운동 위주) 50분", category = "운동", dateStr = todayStr),
                TodoItem(title = "폼롤러 전신 스트레칭 및 쿨다운 10분", category = "운동", dateStr = todayStr)
            )
        }
    }

    // --- High-Fidelity Local Simulation Helper Functions ---

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
    }

    private fun simulateAnalysisReport(
        first: InbodyRecord,
        latest: InbodyRecord,
        gender: String,
        age: Int,
        height: Double
    ): String {
        val weightDiff = latest.weight - first.weight
        val muscleDiff = latest.skeletalMuscleMass - first.skeletalMuscleMass
        val fatDiff = latest.bodyFatPercentage - first.bodyFatPercentage

        val resultSummary = StringBuilder()
        resultSummary.append("📊 **인바디 데이터 비교 요약**\n")
        resultSummary.append("- **체중**: ${first.weight}kg ➔ ${latest.weight}kg (${formatDiff(weightDiff)}kg)\n")
        resultSummary.append("- **골격근량**: ${first.skeletalMuscleMass}kg ➔ ${latest.skeletalMuscleMass}kg (${formatDiff(muscleDiff)}kg)\n")
        resultSummary.append("- **체지방률**: ${first.bodyFatPercentage}% ➔ ${latest.bodyFatPercentage}% (${formatDiff(fatDiff)}%)\n\n")

        resultSummary.append("💬 **AI 종합 피드백**\n")
        if (muscleDiff >= 0.2 && fatDiff <= -0.5) {
            resultSummary.append("🎉 **매우 훌륭한 '린매스업(Lean Mass Up)' 상태입니다!**\n")
            resultSummary.append("골격근량은 증가하고 체지방률은 감소하였습니다. 현재 진행하시는 운동 방식과 영양 섭취 타이밍이 사용자의 신체 타입에 완벽하게 들어맞고 있음을 의미합니다. 무리하게 식단을 변경하지 마시고 현재 상태를 꾸준히 유지해 주세요.\n\n")
        } else if (muscleDiff < 0 && fatDiff <= -0.5) {
            resultSummary.append("⚠️ **다이어트는 진행 중이나, 일부 근손실이 관찰됩니다.**\n")
            resultSummary.append("체지방은 감소하였으나 골격근량도 함께 감소하였습니다. 이는 섭취 칼로리가 과도하게 부족하거나 유산소 운동 비중이 너무 높고 근력 운동이 부족할 때 나타납니다. 끼니당 단백질 섭취량을 조금 더 늘리시고 고중량 저반복 근력 운동 비중을 확대하세요.\n\n")
        } else if (muscleDiff >= 0.2 && fatDiff > 0.5) {
            resultSummary.append("💪 **벌크업(체중 및 근육 증량)이 성공적으로 진행 중입니다.**\n")
            resultSummary.append("골격근량이 양호하게 늘었으나, 잉여 칼로리로 인해 체지방률도 다소 상승했습니다. 이는 벌크업 과정에서 자연스러운 현상입니다. 만약 데피니션을 원하신다면 탄수화물 섭취를 소폭 줄이고 유산소 루틴을 주 2회 추가하세요.\n\n")
        } else {
            resultSummary.append("⚖️ **과도기 혹은 정체기 상태로 보입니다.**\n")
            resultSummary.append("전반적인 지표의 변화 폭이 미미합니다. 신체가 새로운 루틴에 적응했을 가능성이 큽니다. 운동 강도를 높이거나(점진적 과부하), 탄수화물/단백질 비율을 재조정하여 신체에 새로운 자극을 주는 것을 권장합니다.\n\n")
        }

        val estimatedProtein = (latest.weight * 1.6).toInt()
        resultSummary.append("💡 **향후 행동 제안**:\n")
        resultSummary.append("1. **권장 단백질**: 근육 유지/성장을 위해 매일 최소 **${estimatedProtein}g**의 단백질 섭취를 추천합니다. (닭가슴살 3팩 분량)\n")
        resultSummary.append("2. **운동 루틴**: 주 3~4회 근력 운동(스쿼트, 데드리프트 등 큰 근육 중심)과 주 2회 20분 내외의 고강도 인터벌 유산소 운동을 병행해 보세요.\n")
        resultSummary.append("3. **수분 섭취**: 신진대사를 촉진하고 노폐물 배출을 위해 매일 2.0L 이상의 물을 섭취해 주세요.")

        return resultSummary.toString()
    }

    private fun simulateChatResponse(
        message: String,
        latestRecord: InbodyRecord?,
        gender: String,
        age: Int,
        height: Double
    ): String {
        val weight = latestRecord?.weight ?: 70.0
        val muscle = latestRecord?.skeletalMuscleMass ?: 30.0
        val fat = latestRecord?.bodyFatPercentage ?: 20.0

        val msg = message.lowercase()
        return when {
            msg.contains("단백질") || msg.contains("식단") || msg.contains("식사") || msg.contains("음식") -> {
                val recommended = (weight * 1.6).toInt()
                "회원님의 현재 체중(${weight}kg)과 골격근량(${muscle}kg)을 바탕으로 분석했을 때, 하루 권장 단백질 섭취량은 약 **${recommended}g**입니다. 닭가슴살 한 팩에 약 20~25g의 단백질이 포함되어 있으므로 하루 3~4회에 나누어 섭취하는 것이 흡수율에 가장 좋습니다. 정제 탄수화물을 줄이고 복합 탄수화물(현미, 고구마)로 대체해 보세요!"
            }
            msg.contains("운동") || msg.contains("웨이트") || msg.contains("헬스") || msg.contains("근육") || msg.contains("루틴") -> {
                "골격근량 ${muscle}kg 상태에서 효과적으로 근력을 키우려면 대근육 위주의 분할 운동이 좋습니다. 주 3~4회 웨이트 트레이닝을 진행해 보세요.\n추천 종목: 스쿼트, 데드리프트, 체스트 프레스, 렛풀다운\n각 종목당 8~12회씩 4세트를 진행할 수 있는 무게로 점진적 과부하를 적용하는 것이 핵심입니다."
            }
            msg.contains("살") || msg.contains("다이어트") || msg.contains("체지방") || msg.contains("유산소") -> {
                "현재 체지방률 ${fat}%를 건강하게 낮추기 위해 유산소와 근력 운동의 비중을 4:6으로 유지하는 것을 추천합니다. 특히 웨이트 트레이닝 후 20~30분간 경사도를 높인 걷기(인클라인 트레드밀)나 천국의 계단을 타면 체지방 연소 효율을 극대화할 수 있습니다. 야식을 끊고 수면 시간을 7시간 이상 확보하는 것도 매우 중요합니다!"
            }
            msg.contains("안녕") || msg.contains("하이") || msg.contains("반가") || msg.contains("누구") -> {
                "안녕하세요! 회원님의 인바디 데이터(체중 ${weight}kg, 골격근량 ${muscle}kg)를 바탕으로 1:1 맞춤형 피트니스 및 식단 피드백을 제공하는 Gemini 건강 비서입니다. 식단 가이드, 운동 계획 등 궁금한 점을 편하게 질문해 주세요!"
            }
            else -> {
                "질문해 주신 내용 잘 확인했습니다. 회원님의 프로필(키 ${height}cm, 체중 ${weight}kg, 체지방률 ${fat}%)을 고려할 때, 조급한 체중 변화보다는 일일 활동량을 꾸준히 유지하고 규칙적인 식사 템포를 지켜나가는 것이 최우선입니다. 구체적인 식단 조절법이나 추천 웨이트 운동이 필요하시면 말씀해 주세요!"
            }
        }
    }

    private fun formatDiff(value: Double): String {
        return if (value >= 0) "+${String.format("%.1f", value)}" else String.format("%.1f", value)
    }
}
