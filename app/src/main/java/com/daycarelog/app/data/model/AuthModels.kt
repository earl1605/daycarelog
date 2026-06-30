package com.daycarelog.app.data.model

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val suffix: String,
)

data class UserDto(
    val id: Long,
    val email: String,
    val fullName: String?,
    val firstName: String?,
    val lastName: String?,
    val middleName: String?,
    val suffix: String?,
    val role: String,
    val profilePhoto: String?,
    val isActive: Boolean = true,
)

data class AuthResponse(
    val token: String,
    val user: UserDto,
)

data class UpdateProfileRequest(val profilePhoto: String)

// Admin-only user management
data class CreateUserRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val suffix: String,
    val role: String,
)

data class CreateUserResponse(
    val user: UserDto,
    val tempPassword: String,
)

data class UpdateRoleRequest(val role: String)

data class ResetPasswordResponse(val tempPassword: String)
