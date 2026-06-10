package com.example.a20222356sddas.data

data class HealthGoal(
    val targetWeight: Double = 0.0,
    val durationWeeks: Int = 4,
    val startDate: Long = System.currentTimeMillis(),
    val warningMessage: String? = null
)
