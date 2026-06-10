package com.example.a20222356sddas.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("inbody_tracker_prefs", Context.MODE_PRIVATE)

    // User Profile
    var isOnboarded: Boolean
        get() = prefs.getBoolean("is_onboarded", false)
        set(value) = prefs.edit().putBoolean("is_onboarded", value).apply()

    var userName: String
        get() = prefs.getString("user_name", "") ?: ""
        set(value) = prefs.edit().putString("user_name", value).apply()

    var userAge: Int
        get() = prefs.getInt("user_age", 25)
        set(value) = prefs.edit().putInt("user_age", value).apply()

    var userHeight: Double
        get() = prefs.getFloat("user_height", 175.0f).toDouble()
        set(value) = prefs.edit().putFloat("user_height", value.toFloat()).apply()

    var userGender: String
        get() = prefs.getString("user_gender", "남성") ?: "남성"
        set(value) = prefs.edit().putString("user_gender", value).apply()

    // API Key & Security Settings
    var geminiApiKey: String
        get() = prefs.getString("gemini_api_key", "") ?: ""
        set(value) = prefs.edit().putString("gemini_api_key", value).apply()

    var pinCode: String
        get() = prefs.getString("pin_code", "") ?: ""
        set(value) = prefs.edit().putString("pin_code", value).apply()

    var isPinEnabled: Boolean
        get() = prefs.getBoolean("pin_enabled", false)
        set(value) = prefs.edit().putBoolean("pin_enabled", value).apply()

    var isDarkTheme: Boolean?
        get() = if (prefs.contains("dark_theme")) prefs.getBoolean("dark_theme", false) else null
        set(value) {
            if (value == null) {
                prefs.edit().remove("dark_theme").apply()
            } else {
                prefs.edit().putBoolean("dark_theme", value).apply()
            }
        }

    // Inbody Records
    fun getInbodyRecords(): List<InbodyRecord> {
        val jsonStr = prefs.getString("inbody_records", null) ?: return emptyList()
        val list = mutableListOf<InbodyRecord>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    InbodyRecord(
                        id = obj.optString("id"),
                        date = obj.optLong("date"),
                        weight = obj.optDouble("weight"),
                        skeletalMuscleMass = obj.optDouble("skeletalMuscleMass"),
                        bodyFatPercentage = obj.optDouble("bodyFatPercentage"),
                        bmi = obj.optDouble("bmi", 0.0),
                        visceralFatLevel = obj.optDouble("visceralFatLevel", 0.0),
                        boneMass = obj.optDouble("boneMass", 0.0),
                        bmr = obj.optDouble("bmr", 0.0),
                        bodyWaterPercentage = obj.optDouble("bodyWaterPercentage", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list.sortedBy { it.date }
    }

    fun saveInbodyRecords(records: List<InbodyRecord>) {
        val array = JSONArray()
        for (record in records) {
            val obj = JSONObject()
            obj.put("id", record.id)
            obj.put("date", record.date)
            obj.put("weight", record.weight)
            obj.put("skeletalMuscleMass", record.skeletalMuscleMass)
            obj.put("bodyFatPercentage", record.bodyFatPercentage)
            obj.put("bmi", record.bmi)
            obj.put("visceralFatLevel", record.visceralFatLevel)
            obj.put("boneMass", record.boneMass)
            obj.put("bmr", record.bmr)
            obj.put("bodyWaterPercentage", record.bodyWaterPercentage)
            array.put(obj)
        }
        prefs.edit().putString("inbody_records", array.toString()).apply()
    }

    fun addInbodyRecord(record: InbodyRecord) {
        val current = getInbodyRecords().toMutableList()
        current.add(record)
        saveInbodyRecords(current)
    }

    fun deleteInbodyRecord(id: String) {
        val current = getInbodyRecords().filter { it.id != id }
        saveInbodyRecords(current)
    }

    // Health Goal
    fun getHealthGoal(): HealthGoal {
        val jsonStr = prefs.getString("health_goal", null)
        if (jsonStr == null) {
            // Default goal
            val records = getInbodyRecords()
            val startWeight = records.lastOrNull()?.weight ?: 70.0
            return HealthGoal(targetWeight = startWeight, durationWeeks = 4, startDate = System.currentTimeMillis())
        }
        return try {
            val obj = JSONObject(jsonStr)
            HealthGoal(
                targetWeight = obj.optDouble("targetWeight"),
                durationWeeks = obj.optInt("durationWeeks"),
                startDate = obj.optLong("startDate"),
                warningMessage = if (obj.isNull("warningMessage")) null else obj.optString("warningMessage")
            )
        } catch (e: Exception) {
            HealthGoal()
        }
    }

    fun saveHealthGoal(goal: HealthGoal) {
        val obj = JSONObject()
        obj.put("targetWeight", goal.targetWeight)
        obj.put("durationWeeks", goal.durationWeeks)
        obj.put("startDate", goal.startDate)
        obj.put("warningMessage", goal.warningMessage)
        prefs.edit().putString("health_goal", obj.toString()).apply()
    }

    // Daily Todo Items
    fun getTodoItems(): List<TodoItem> {
        val jsonStr = prefs.getString("todo_items", null) ?: return emptyList()
        val list = mutableListOf<TodoItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TodoItem(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        category = obj.optString("category"),
                        isCompleted = obj.optBoolean("isCompleted"),
                        dateStr = obj.optString("dateStr")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveTodoItems(items: List<TodoItem>) {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("category", item.category)
            obj.put("isCompleted", item.isCompleted)
            obj.put("dateStr", item.dateStr)
            array.put(obj)
        }
        prefs.edit().putString("todo_items", array.toString()).apply()
    }

    fun toggleTodoCompleted(id: String): List<TodoItem> {
        val items = getTodoItems().map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        saveTodoItems(items)
        return items
    }

    fun addTodoItem(item: TodoItem) {
        val items = getTodoItems().toMutableList()
        items.add(item)
        saveTodoItems(items)
    }

    fun deleteTodoItem(id: String) {
        val items = getTodoItems().filter { it.id != id }
        saveTodoItems(items)
    }

    // Chat History
    fun getChatMessages(): List<ChatMessage> {
        val jsonStr = prefs.getString("chat_messages", null) ?: return emptyList()
        val list = mutableListOf<ChatMessage>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ChatMessage(
                        id = obj.optString("id"),
                        text = obj.optString("text"),
                        isUser = obj.optBoolean("isUser"),
                        timestamp = obj.optLong("timestamp")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveChatMessages(messages: List<ChatMessage>) {
        val array = JSONArray()
        for (msg in messages) {
            val obj = JSONObject()
            obj.put("id", msg.id)
            obj.put("text", msg.text)
            obj.put("isUser", msg.isUser)
            obj.put("timestamp", msg.timestamp)
            array.put(obj)
        }
        prefs.edit().putString("chat_messages", array.toString()).apply()
    }

    fun addChatMessage(msg: ChatMessage) {
        val msgs = getChatMessages().toMutableList()
        msgs.add(msg)
        saveChatMessages(msgs)
    }

    fun clearChatHistory() {
        prefs.edit().remove("chat_messages").apply()
    }
}
