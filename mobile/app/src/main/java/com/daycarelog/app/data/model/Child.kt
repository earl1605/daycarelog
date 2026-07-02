package com.daycarelog.app.data.model

data class Child(
    val id: Long? = null,
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: String = "",
    val sex: String = "",
    val address: String? = null,
    val enrollmentDate: String = "",
    val enrollmentStatus: String = "active",
)
