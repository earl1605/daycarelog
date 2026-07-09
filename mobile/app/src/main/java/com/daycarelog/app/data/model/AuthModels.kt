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
    val emailVerified: Boolean? = null,
)

fun UserDto.isEmailVerified(): Boolean = emailVerified != false

data class AuthResponse(
    val token: String,
    val user: UserDto,
)

data class VerifyEmailRequest(
    val token: String? = null,
    val email: String? = null,
    val code: String? = null,
)

data class VerifyEmailResponse(
    val message: String,
    val token: String,
    val user: UserDto,
)

data class ResendVerificationRequest(val email: String)

data class GenericMessageResponse(val message: String)

data class UpdateProfileRequest(
    val profilePhoto: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val suffix: String? = null,
)

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
