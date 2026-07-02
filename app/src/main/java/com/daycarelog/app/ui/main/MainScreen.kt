package com.daycarelog.app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.daycarelog.app.R
import com.daycarelog.app.data.api.TokenProvider
import com.daycarelog.app.data.model.UserDto
import com.daycarelog.app.data.preferences.TokenDataStore
import com.daycarelog.app.ui.attendance.AttendanceScreen
import com.daycarelog.app.ui.children.ChildFormScreen
import com.daycarelog.app.ui.children.ChildrenScreen
import com.daycarelog.app.ui.dashboard.DashboardScreen
import com.daycarelog.app.ui.health.HealthFormScreen
import com.daycarelog.app.ui.health.HealthScreen
import com.daycarelog.app.ui.reports.ReportsScreen
import com.daycarelog.app.ui.settings.SettingsScreen
import com.daycarelog.app.ui.theme.BorderGray
import com.daycarelog.app.ui.theme.Charcoal
import com.daycarelog.app.ui.theme.Green40
import com.daycarelog.app.ui.theme.Green95
import com.daycarelog.app.ui.theme.MutedGray
import com.daycarelog.app.ui.theme.OffWhite
import com.daycarelog.app.ui.users.UsersScreen
import com.google.gson.Gson
import androidx.compose.material.icons.outlined.Home
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object InnerRoutes {
    const val DASHBOARD   = "dashboard"
    const val CHILDREN    = "children"
    const val CHILD_FORM  = "child_form"
    const val ATTENDANCE  = "attendance"
    const val HEALTH      = "health"
    const val HEALTH_FORM = "health_form"
    const val REPORTS     = "reports"
    const val SETTINGS    = "settings"
    const val USERS       = "users"
}

private data class NavEntry(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val mainSection = listOf(
    NavEntry(InnerRoutes.DASHBOARD,  "Dashboard",      Icons.Outlined.Home),
    NavEntry(InnerRoutes.CHILDREN,   "Children",       Icons.Outlined.ChildCare),
    NavEntry(InnerRoutes.ATTENDANCE, "Attendance",     Icons.AutoMirrored.Outlined.FactCheck),
    NavEntry(InnerRoutes.HEALTH,     "Health Records", Icons.Outlined.MonitorHeart),
)
private val managementSection = listOf(
    NavEntry(InnerRoutes.REPORTS,  "Reports",  Icons.Outlined.BarChart),
    NavEntry(InnerRoutes.SETTINGS, "Settings", Icons.Outlined.Settings),
)
private val adminSection = listOf(
    NavEntry(InnerRoutes.USERS, "Users", Icons.Outlined.ManageAccounts),
)

@Composable
fun MainScreen(onSignOut: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val innerNav = rememberNavController()
    val backStack by innerNav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showSignOutDialog by remember { mutableStateOf(false) }

    var user by remember { mutableStateOf<UserDto?>(null) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val json = TokenDataStore.getUser(ctx).first()
        if (json != null) user = Gson().fromJson(json, UserDto::class.java)
    }
    val isAdmin = user?.role == "admin"

    fun navigateTo(route: String) {
        innerNav.navigate(route) {
            popUpTo(innerNav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        scope.launch { drawerState.close() }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text  = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        scope.launch {
                            TokenDataStore.clear(ctx)
                            TokenProvider.token = null
                            onSignOut()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.daycarelog.app.ui.theme.Red40),
                ) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Cancel") }
            },
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = OffWhite,
                drawerContentColor = Charcoal,
                modifier = Modifier.width(280.dp),
            ) {
                // ── Logo header ──────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Green40),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(R.drawable.ic_child),
                            contentDescription = "DaycareLog",
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Column {
                        Text("DaycareLog", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Charcoal)
                        Text("BARANGAY SYSTEM", fontSize = 10.sp, color = MutedGray, letterSpacing = 0.8.sp)
                    }
                }
                HorizontalDivider(color = BorderGray)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    DrawerSectionLabel("MAIN")
                    mainSection.forEach { item ->
                        DrawerNavItem(item, currentRoute, ::navigateTo)
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = BorderGray)
                    Spacer(Modifier.height(8.dp))

                    DrawerSectionLabel("MANAGEMENT")
                    managementSection.forEach { item ->
                        DrawerNavItem(item, currentRoute, ::navigateTo)
                    }

                    if (isAdmin) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = BorderGray)
                        Spacer(Modifier.height(8.dp))

                        DrawerSectionLabel("ADMIN")
                        adminSection.forEach { item ->
                            DrawerNavItem(item, currentRoute, ::navigateTo)
                        }
                    }
                }

                HorizontalDivider(color = BorderGray)
                // ── User footer ──────────────────────────────────────────
                Column(Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                    ) {
                        val displayName = listOfNotNull(user?.firstName, user?.lastName)
                            .joinToString(" ").ifEmpty { user?.email ?: "User" }
                        val initial = (user?.firstName?.firstOrNull() ?: user?.email?.firstOrNull() ?: 'U').uppercaseChar()
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Green95),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(initial.toString(), color = Green40, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(displayName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Charcoal, maxLines = 1)
                            Text(
                                user?.role?.replaceFirstChar { it.uppercase() } ?: "",
                                fontSize = 11.sp, color = MutedGray,
                            )
                        }
                    }
                    TextButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(16.dp), tint = MutedGray)
                        Spacer(Modifier.width(8.dp))
                        Text("Sign out", fontSize = 13.sp, color = MutedGray, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
    ) {
        Scaffold { padding ->
            NavHost(
                navController = innerNav,
                startDestination = InnerRoutes.DASHBOARD,
                modifier = Modifier.padding(padding),
            ) {
                composable(InnerRoutes.DASHBOARD) {
                    DashboardScreen(
                        onOpenDrawer        = { scope.launch { drawerState.open() } },
                        onNavigateToSettings = { navigateTo(InnerRoutes.SETTINGS) },
                        onAddChild          = { innerNav.navigate(InnerRoutes.CHILD_FORM) },
                        onTakeAttendance    = { navigateTo(InnerRoutes.ATTENDANCE) },
                        onAddHealthRecord   = { innerNav.navigate(InnerRoutes.HEALTH_FORM) },
                        onViewReports       = { navigateTo(InnerRoutes.REPORTS) },
                    )
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
                        onSignOut      = onSignOut,
                        onBack         = { innerNav.popBackStack() },
                        onManageStaff  = { innerNav.navigate(InnerRoutes.USERS) },
                    )
                }
                composable(InnerRoutes.USERS) {
                    UsersScreen(onBack = { innerNav.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = MutedGray,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
    )
}

@Composable
private fun DrawerNavItem(item: NavEntry, currentRoute: String?, onClick: (String) -> Unit) {
    val selected = currentRoute == item.route ||
        currentRoute?.startsWith("${item.route}/") == true
    NavigationDrawerItem(
        label = { Text(item.label, fontSize = 14.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
        icon = { Icon(item.icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
        selected = selected,
        onClick = { onClick(item.route) },
        shape = RoundedCornerShape(8.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Green95,
            selectedIconColor = Charcoal,
            selectedTextColor = Charcoal,
            unselectedIconColor = MutedGray,
            unselectedTextColor = MutedGray,
        ),
        modifier = Modifier.padding(vertical = 1.dp),
    )
}
