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

// POST /children/{childId}/guardians body — createPortalAccount=true always creates
// (or reuses, by email) a "parent" role login and returns a one-time temp password.
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

// GET /guardians — one row per parent-portal-account, aggregating every child that
// account is linked to. Contact-only guardians (no userId) aren't included.
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
