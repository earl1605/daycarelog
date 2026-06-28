package com.daycarelog.app.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycarelog.app.data.api.RetrofitClient
import com.daycarelog.app.data.api.TokenProvider
import com.daycarelog.app.data.model.LoginRequest
import com.daycarelog.app.data.model.RegisterRequest
import com.daycarelog.app.data.model.UserDto
import com.daycarelog.app.data.preferences.TokenDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle       : AuthState()
    object Loading    : AuthState()
    object Registered : AuthState()
    data class Success(val user: UserDto) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun login(context: Context, email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val res = RetrofitClient.api.login(LoginRequest(email, password))
                TokenProvider.token = res.token
                TokenDataStore.saveToken(context, res.token)
                TokenDataStore.saveUser(context, Gson().toJson(res.user))
                _state.value = AuthState.Success(res.user)
            } catch (e: Exception) {
                _state.value = AuthState.Error(friendlyMessage(e))
            }
        }
    }

    fun register(
        context: Context,
        email: String, password: String,
        firstName: String, lastName: String,
        middleName: String, suffix: String,
        role: String,
    ) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                RetrofitClient.api.register(
                    RegisterRequest(email, password, firstName, lastName, middleName, suffix, role)
                )
                _state.value = AuthState.Registered
            } catch (e: Exception) {
                _state.value = AuthState.Error(friendlyMessage(e))
            }
        }
    }

    fun resetState() { _state.value = AuthState.Idle }

    private fun friendlyMessage(e: Exception): String {
        return try {
            val body = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()
            if (!body.isNullOrBlank()) {
                com.google.gson.JsonParser.parseString(body)
                    .asJsonObject?.get("message")?.asString ?: e.message ?: "Unknown error"
            } else {
                e.message ?: "Network error"
            }
        } catch (_: Exception) {
            e.message ?: "Unknown error"
        }
    }
}
