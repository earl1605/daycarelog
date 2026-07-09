package com.daycarelog.app.data.model

data class Guardian(
    val id: Long? = null,
    val childId: Long = 0,
    val name: String = "",
    val relationship: String? = null,
    val contactNumber: String? = null,
    val email: String? = null,
    val address: String? = null,
    val isPrimary: Boolean = false,
    val userId: Long? = null,
)

data class GuardianRequest(
    val name: String,
    val relationship: String? = null,
    val contactNumber: String? = null,
    val address: String? = null,
    val isPrimary: Boolean = false,
    val createPortalAccount: Boolean = false,
    val email: String? = null,
)

data class CreateGuardianResponse(
    val guardian: Guardian,
    val tempPassword: String?,
)

data class GuardianAccountResponse(
    val userId: Long,
    val name: String?,
    val email: String?,
    val contactNumber: String?,
    val address: String?,
    val relationship: String?,
    val children: List<ChildSummary>,
)

data class ChildSummary(
    val id: Long,
    val firstName: String,
    val lastName: String,
)
