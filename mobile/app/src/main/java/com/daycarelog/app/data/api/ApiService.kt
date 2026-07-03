package com.daycarelog.app.data.api

import com.daycarelog.app.data.model.AttendanceRecord
import com.daycarelog.app.data.model.AuthResponse
import com.daycarelog.app.data.model.Child
import com.daycarelog.app.data.model.CreateGuardianResponse
import com.daycarelog.app.data.model.CreateUserRequest
import com.daycarelog.app.data.model.CreateUserResponse
import com.daycarelog.app.data.model.Guardian
import com.daycarelog.app.data.model.GuardianAccountResponse
import com.daycarelog.app.data.model.GuardianRequest
import com.daycarelog.app.data.model.HealthRecord
import com.daycarelog.app.data.model.LoginRequest
import com.daycarelog.app.data.model.MonthlyReport
import com.daycarelog.app.data.model.RegisterRequest
import com.daycarelog.app.data.model.ResetPasswordResponse
import com.daycarelog.app.data.model.UpdateProfileRequest
import com.daycarelog.app.data.model.UpdateRoleRequest
import com.daycarelog.app.data.model.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    // Children
    @GET("children")
    suspend fun getChildren(): List<Child>

    @GET("children/{id}")
    suspend fun getChild(@Path("id") id: Long): Child

    @POST("children")
    suspend fun createChild(@Body child: Child): Child

    @PUT("children/{id}")
    suspend fun updateChild(@Path("id") id: Long, @Body child: Child): Child

    @DELETE("children/{id}")
    suspend fun deleteChild(@Path("id") id: Long)

    // Parent-facing "mine" endpoints — resolve the caller's own linked children
    // server-side from the JWT, never from a request param, so there's no ID to
    // tamper with. Admin/Staff calling these just get an empty list back.
    @GET("children/mine")
    suspend fun getMyChildren(): List<Child>

    @GET("attendance/mine")
    suspend fun getMyAttendance(): List<AttendanceRecord>

    @GET("health-records/mine")
    suspend fun getMyHealthRecords(): List<HealthRecord>

    // Guardians — per-child contact/portal-account management (Admin/Staff only)
    @GET("children/{childId}/guardians")
    suspend fun getGuardians(@Path("childId") childId: Long): List<Guardian>

    @POST("children/{childId}/guardians")
    suspend fun addGuardian(@Path("childId") childId: Long, @Body request: GuardianRequest): CreateGuardianResponse

    @DELETE("children/{childId}/guardians/{guardianId}")
    suspend fun deleteGuardian(@Path("childId") childId: Long, @Path("guardianId") guardianId: Long)

    // Guardians — portal-account directory (Admin/Staff only)
    @GET("guardians")
    suspend fun getGuardianAccounts(): List<GuardianAccountResponse>

    @DELETE("guardians/user/{userId}")
    suspend fun removeGuardianAccount(@Path("userId") userId: Long)

    // Attendance
    @GET("attendance")
    suspend fun getAttendance(@Query("date") date: String): List<AttendanceRecord>

    @GET("attendance/range")
    suspend fun getAttendanceRange(@Query("start") start: String, @Query("end") end: String): List<AttendanceRecord>

    @POST("attendance/bulk")
    suspend fun saveAttendanceBulk(@Body records: List<AttendanceRecord>): List<AttendanceRecord>

    // Health records
    @GET("health-records")
    suspend fun getHealthRecords(): List<HealthRecord>

    @POST("health-records")
    suspend fun createHealthRecord(@Body record: HealthRecord): HealthRecord

    @DELETE("health-records/{id}")
    suspend fun deleteHealthRecord(@Path("id") id: Long)

    // Reports
    @GET("reports/monthly")
    suspend fun getMonthlyReport(@Query("month") month: String): MonthlyReport

    // Users
    @PUT("users/{id}")
    suspend fun updateProfile(@Path("id") id: Long, @Body request: UpdateProfileRequest): UserDto

    // Users — admin only
    @GET("users")
    suspend fun getUsers(): List<UserDto>

    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): CreateUserResponse

    @PUT("users/{id}/role")
    suspend fun updateUserRole(@Path("id") id: Long, @Body request: UpdateRoleRequest): UserDto

    @PUT("users/{id}/deactivate")
    suspend fun deactivateUser(@Path("id") id: Long): UserDto

    @PUT("users/{id}/reactivate")
    suspend fun reactivateUser(@Path("id") id: Long): UserDto

    @PUT("users/{id}/reset-password")
    suspend fun resetUserPassword(@Path("id") id: Long): ResetPasswordResponse

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Long)
}
