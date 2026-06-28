package com.daycarelog.app.data.model

data class MonthlyReport(
    val month: String = "",
    val total: Int = 0,
    val schoolDays: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val attendanceRate: Double = 0.0,
    val nutritionalStatus: Map<String, Int> = emptyMap(),
    val children: List<Child> = emptyList(),
)
