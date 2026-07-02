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
import com.daycarelog.app.data.preferences.TokenDataStore
import com.daycarelog.app.ui.auth.LoginScreen
import com.daycarelog.app.ui.auth.RegisterScreen
import com.daycarelog.app.ui.main.MainScreen
import kotlinx.coroutines.flow.first

private const val LOGIN    = "login"
private const val REGISTER = "register"
private const val MAIN     = "main"

@Composable
fun DaycareLogNavGraph() {
    val ctx = LocalContext.current
    val nav = rememberNavController()
    var startDest by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val token = TokenDataStore.getToken(ctx).first()
        if (!token.isNullOrBlank()) {
            TokenProvider.token = token
            startDest = MAIN
        } else {
            startDest = LOGIN
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
                onLoginSuccess       = { nav.navigate(MAIN) { popUpTo(0) { inclusive = true } } },
                onNavigateToRegister = { nav.navigate(REGISTER) },
            )
        }
        composable(REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    nav.navigate("$LOGIN?msg=Account+created+successfully") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = { nav.popBackStack() },
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
