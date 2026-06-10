package com.example.a20222356sddas.data

import java.util.UUID

data class InbodyRecord(
    val id: String = UUID.randomUUID().toString(),
    val date: Long, // timestamp
    val weight: Double,
    val skeletalMuscleMass: Double,
    val bodyFatPercentage: Double,
    val bmi: Double = 0.0,
    val visceralFatLevel: Double = 0.0,
    val boneMass: Double = 0.0,
    val bmr: Double = 0.0,
    val bodyWaterPercentage: Double = 0.0
)
