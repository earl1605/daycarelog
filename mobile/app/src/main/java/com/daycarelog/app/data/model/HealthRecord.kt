package com.daycarelog.app.data.model

data class HealthRecord(
    val id: Long? = null,
    val childId: Long = 0,
    val measurementDate: String = "",
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val remarks: String? = null,
    val nutritionalStatus: String? = null,
)
