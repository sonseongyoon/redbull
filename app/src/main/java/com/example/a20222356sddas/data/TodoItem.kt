package com.example.a20222356sddas.data

import java.util.UUID

data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String, // "운동" (Workout) or "식단" (Diet) or "기타" (Others)
    val isCompleted: Boolean = false,
    val dateStr: String // "yyyy-MM-dd" format
)
