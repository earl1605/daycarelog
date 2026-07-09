package com.daycarelog.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.daycarelog.app.data.api.TokenProvider
import com.daycarelog.app.data.model.UserDto
import com.daycarelog.app.data.model.isEmailVerified
import com.daycarelog.app.data.preferences.TokenDataStore
import com.daycarelog.app.ui.auth.LoginScreen
import com.daycarelog.app.ui.auth.RegisterScreen
import com.daycarelog.app.ui.auth.VerifyEmailScreen
import com.daycarelog.app.ui.main.MainScreen
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import java.net.URLEncoder

private const val LOGIN        = "login"
private const val REGISTER     = "register"
private const val MAIN         = "main"
private const val VERIFY_EMAIL = "verify_email"

private fun encode(value: String) = URLEncoder.encode(value, "UTF-8")

@Composable
fun DaycareLogNavGraph() {
    val ctx = LocalContext.current
    val nav = rememberNavController()
    var startDest by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val token = TokenDataStore.getToken(ctx).first()
        if (token.isNullOrBlank()) {
            startDest = LOGIN
        } else {
            TokenProvider.token = token
            val userJson = TokenDataStore.getUser(ctx).first()
            val user = userJson?.let { runCatching { Gson().fromJson(it, UserDto::class.java) }.getOrNull() }
            startDest = if (user != null && !user.isEmailVerified()) {
                "$VERIFY_EMAIL/${encode(user.email)}"
            } else {
                MAIN
            }
        }
    }

    if (startDest == null) return

    NavHost(navController = nav, startDestination = startDest!!) {
        composable(
            route = "$LOGIN?msg={msg}",
            arguments = listOf(navArgument("msg") { nullable = true; defaultValue = null; type = NavType.StringType }),
        ) { backStack ->
            val msg = backStack.arguments?.getString("msg")
            LoginScreen(
                successMessage       = msg,
                onLoginSuccess       = { user ->
                    if (user.isEmailVerified()) {
                        nav.navigate(MAIN) { popUpTo(0) { inclusive = true } }
                    } else {
                        nav.navigate("$VERIFY_EMAIL/${encode(user.email)}") { popUpTo(0) { inclusive = true } }
                    }
                },
                onNavigateToRegister = { nav.navigate(REGISTER) },
            )
        }
        composable(REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { email ->
                    nav.navigate("$VERIFY_EMAIL/${encode(email)}") { popUpTo(0) { inclusive = true } }
                },
                onNavigateToLogin = { nav.popBackStack() },
            )
        }
        composable(
            route = "$VERIFY_EMAIL/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType }),
        ) { backStack ->
            val email = backStack.arguments?.getString("email") ?: ""
            VerifyEmailScreen(
                email = email,
                onVerified = { nav.navigate(MAIN) { popUpTo(0) { inclusive = true } } },
                onSignOut = {
                    TokenProvider.token = null
                    nav.navigate(LOGIN) { popUpTo(0) { inclusive = true } }
                },
            )
        }
        composable(MAIN) {
            MainScreen(
                onSignOut = {
                    nav.navigate(LOGIN) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}
