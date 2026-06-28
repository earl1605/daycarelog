package com.daycarelog.app.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.daycarelog.app.ui.attendance.AttendanceScreen
import com.daycarelog.app.ui.children.ChildFormScreen
import com.daycarelog.app.ui.children.ChildrenScreen
import com.daycarelog.app.ui.dashboard.DashboardScreen
import com.daycarelog.app.ui.health.HealthFormScreen
import com.daycarelog.app.ui.health.HealthScreen
import com.daycarelog.app.ui.reports.ReportsScreen
import com.daycarelog.app.ui.settings.SettingsScreen

private val Green500 = Color(0xFF16a34a)

object InnerRoutes {
    const val DASHBOARD   = "dashboard"
    const val CHILDREN    = "children"
    const val CHILD_FORM  = "child_form"
    const val ATTENDANCE  = "attendance"
    const val HEALTH      = "health"
    const val HEALTH_FORM = "health_form"
    const val REPORTS     = "reports"
    const val SETTINGS    = "settings"
}

private data class NavItem(val route: String, val label: String, val emoji: String)

private val navItems = listOf(
    NavItem(InnerRoutes.DASHBOARD,  "Home",       "🏠"),
    NavItem(InnerRoutes.CHILDREN,   "Children",   "👦"),
    NavItem(InnerRoutes.ATTENDANCE, "Attendance", "📋"),
    NavItem(InnerRoutes.HEALTH,     "Health",     "❤️"),
    NavItem(InnerRoutes.REPORTS,    "Reports",    "📊"),
)

@Composable
fun MainScreen(onSignOut: () -> Unit) {
    val innerNav = rememberNavController()
    val backStack by innerNav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // Hide bottom bar on secondary screens
    val showBottomBar = currentRoute in navItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White) {
                    navItems.forEach { item ->
                        val selected = backStack?.destination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                innerNav.navigate(item.route) {
                                    popUpTo(innerNav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(item.emoji, fontSize = 20.sp) },
                            label = {
                                Text(
                                    item.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor = Green500,
                                indicatorColor = Color(0xFFdcfce7),
                            ),
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = innerNav,
            startDestination = InnerRoutes.DASHBOARD,
            modifier = Modifier.padding(padding),
        ) {
            composable(InnerRoutes.DASHBOARD) {
                DashboardScreen(onNavigateToSettings = { innerNav.navigate(InnerRoutes.SETTINGS) })
            }
            composable(InnerRoutes.CHILDREN) {
                ChildrenScreen(
                    onAddChild  = { innerNav.navigate(InnerRoutes.CHILD_FORM) },
                    onEditChild = { id -> innerNav.navigate("${InnerRoutes.CHILD_FORM}/$id") },
                )
            }
            composable("${InnerRoutes.CHILD_FORM}/{id}") { back ->
                val id = back.arguments?.getString("id")?.toLongOrNull()
                ChildFormScreen(childId = id, onBack = { innerNav.popBackStack() })
            }
            composable(InnerRoutes.CHILD_FORM) {
                ChildFormScreen(childId = null, onBack = { innerNav.popBackStack() })
            }
            composable(InnerRoutes.ATTENDANCE) {
                AttendanceScreen()
            }
            composable(InnerRoutes.HEALTH) {
                HealthScreen(onAdd = { innerNav.navigate(InnerRoutes.HEALTH_FORM) })
            }
            composable(InnerRoutes.HEALTH_FORM) {
                HealthFormScreen(onBack = { innerNav.popBackStack() })
            }
            composable(InnerRoutes.REPORTS) {
                ReportsScreen()
            }
            composable(InnerRoutes.SETTINGS) {
                SettingsScreen(
                    onSignOut = onSignOut,
                    onBack    = { innerNav.popBackStack() },
                )
            }
        }
    }
}
