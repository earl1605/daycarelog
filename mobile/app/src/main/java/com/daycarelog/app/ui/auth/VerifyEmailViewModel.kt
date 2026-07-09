package com.daycarelog.app.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycarelog.app.data.api.RetrofitClient
import com.daycarelog.app.data.api.TokenProvider
import com.daycarelog.app.data.model.AuthResponse
import com.daycarelog.app.data.model.ResendVerificationRequest
import com.daycarelog.app.data.model.UserDto
import com.daycarelog.app.data.model.VerifyEmailRequest
import com.daycarelog.app.data.model.VerifyEmailResponse
import com.daycarelog.app.data.model.isEmailVerified
import com.daycarelog.app.data.preferences.TokenDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class VerifyEmailState {
    object Idle           : VerifyEmailState()
    object Loading        : VerifyEmailState()
    object Resent         : VerifyEmailState()
    object NotYetVerified : VerifyEmailState()
    data class Verified(val user: UserDto) : VerifyEmailState()
    data class Error(val message: String, val code: String? = null) : VerifyEmailState()
}

class VerifyEmailViewModel(
    private val verifyEmail: suspend (VerifyEmailRequest) -> VerifyEmailResponse =
        { RetrofitClient.api.verifyEmail(it) },
    private val resendVerification: suspend (String) -> Unit =
        { email -> RetrofitClient.api.resendVerification(ResendVerificationRequest(email)) },
    private val fetchMe: suspend () -> UserDto =
        { RetrofitClient.api.me() },
    private val refreshToken: suspend () -> AuthResponse =
        { RetrofitClient.api.refreshToken() },
    private val persistSession: suspend (Context?, String, UserDto) -> Unit =
        { context, token, user ->
            TokenProvider.token = token
            if (context != null) {
                TokenDataStore.saveToken(context, token)
                TokenDataStore.saveUser(context, Gson().toJson(user))
            }
        },
) : ViewModel() {

    private val _state = MutableStateFlow<VerifyEmailState>(VerifyEmailState.Idle)
    val state: StateFlow<VerifyEmailState> = _state

    fun verifyByCode(context: Context, email: String, code: String) {
        viewModelScope.launch {
            _state.value = VerifyEmailState.Loading
            _state.value = doVerifyByCode(context, email, code)
        }
    }

    fun resend(email: String) {
        viewModelScope.launch {
            _state.value = VerifyEmailState.Loading
            _state.value = doResend(email)
        }
    }

    fun checkStatus(context: Context) {
        viewModelScope.launch {
            _state.value = VerifyEmailState.Loading
            _state.value = doCheckStatus(context)
        }
    }

    fun resetState() { _state.value = VerifyEmailState.Idle }

    internal suspend fun doVerifyByCode(context: Context?, email: String, code: String): VerifyEmailState =
        try {
            val res = verifyEmail(VerifyEmailRequest(email = email, code = code))
            persistSession(context, res.token, res.user)
            VerifyEmailState.Verified(res.user)
        } catch (e: Exception) {
            errorFrom(e)
        }

    internal suspend fun doVerifyByToken(context: Context?, token: String): VerifyEmailState =
        try {
            val res = verifyEmail(VerifyEmailRequest(token = token))
            persistSession(context, res.token, res.user)
            VerifyEmailState.Verified(res.user)
        } catch (e: Exception) {
            errorFrom(e)
        }

    internal suspend fun doResend(email: String): VerifyEmailState =
        try {
            resendVerification(email)
            VerifyEmailState.Resent
        } catch (e: Exception) {
            errorFrom(e)
        }

    internal suspend fun doCheckStatus(context: Context?): VerifyEmailState {
        if (TokenProvider.token.isNullOrBlank()) {
            return VerifyEmailState.Error("Enter the code above, or sign in once you've verified.")
        }
        return try {
            val user = fetchMe()
            if (user.isEmailVerified()) {
                val res = refreshToken()
                persistSession(context, res.token, res.user)
                VerifyEmailState.Verified(res.user)
            } else {
                VerifyEmailState.NotYetVerified
            }
        } catch (e: Exception) {
            errorFrom(e)
        }
    }

    private fun errorFrom(e: Exception): VerifyEmailState.Error {
        return try {
            val body = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()
            if (!body.isNullOrBlank()) {
                val json = com.google.gson.JsonParser.parseString(body).asJsonObject
                VerifyEmailState.Error(
                    json.get("message")?.asString ?: e.message ?: "Unknown error",
                    json.get("code")?.asString,
                )
            } else {
                VerifyEmailState.Error(e.message ?: "Network error")
            }
        } catch (_: Exception) {
            VerifyEmailState.Error(e.message ?: "Unknown error")
        }
    }
}
