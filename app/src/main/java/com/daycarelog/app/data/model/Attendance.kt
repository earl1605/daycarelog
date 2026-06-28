package com.daycarelog.app.data.model

data class AttendanceRecord(
    val id: Long? = null,
    val childId: Long = 0,
    val date: String = "",
    val status: String = "absent",
)
