package com.daycarelog.app.ui.dashboard

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycarelog.app.data.api.RetrofitClient
import com.daycarelog.app.data.model.UserDto
import com.daycarelog.app.data.preferences.ThemeState
import com.daycarelog.app.data.preferences.TokenDataStore
import com.daycarelog.app.ui.theme.BorderGray
import com.daycarelog.app.ui.theme.CardSurface
import com.daycarelog.app.ui.theme.Charcoal
import com.daycarelog.app.ui.theme.DarkBorderGray
import com.daycarelog.app.ui.theme.DarkMutedGray
import com.daycarelog.app.ui.theme.DarkOnBackground
import com.daycarelog.app.ui.theme.DarkSurface
import com.daycarelog.app.ui.theme.Green30
import com.daycarelog.app.ui.theme.Green40
import com.daycarelog.app.ui.theme.Green95
import com.daycarelog.app.ui.theme.MutedGray
import com.daycarelog.app.ui.theme.StatAmberBg
import com.daycarelog.app.ui.theme.StatAmberFg
import com.daycarelog.app.ui.theme.StatBlueBg
import com.daycarelog.app.ui.theme.StatBlueFg
import com.daycarelog.app.ui.theme.StatGreenBg
import com.daycarelog.app.ui.theme.StatGreenFg
import com.daycarelog.app.ui.theme.StatVioletBg
import com.daycarelog.app.ui.theme.StatVioletFg
import com.daycarelog.app.ui.theme.White
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private data class DayAttendance(val label: String, val present: Int)

@Composable
fun DashboardScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAddChild: () -> Unit,
    onTakeAttendance: () -> Unit,
    onAddHealthRecord: () -> Unit,
    onViewReports: () -> Unit,
) {
    val ctx = LocalContext.current
    val userJson by TokenDataStore.getUser(ctx).collectAsState(initial = null)
    val user = remember(userJson) { userJson?.let { Gson().fromJson(it, UserDto::class.java) } }

    val isDark      = ThemeState.isDarkMode
    val pageBg      = if (isDark) com.daycarelog.app.ui.theme.DarkBackground else White
    val textColor   = if (isDark) DarkOnBackground else Charcoal
    val mutedColor  = if (isDark) DarkMutedGray else MutedGray
    val cardBg      = if (isDark) DarkSurface else CardSurface
    val borderColor = if (isDark) DarkBorderGray else BorderGray
    val chipBg      = if (isDark) Green30 else Green95

    var totalChildren  by remember { mutableStateOf<Int?>(null) }
    var activeChildren by remember { mutableStateOf<Int?>(null) }
    var presentToday   by remember { mutableStateOf<Int?>(null) }
    var weeklyData      by remember { mutableStateOf<List<DayAttendance>>(emptyList()) }
    var loading         by remember { mutableStateOf(true) }

    val today = LocalDate.now()

    LaunchedEffect(Unit) {
        try {
            val children = RetrofitClient.api.getChildren()
            totalChildren  = children.size
            activeChildren = children.count { it.enrollmentStatus == "active" }

            val todayStr = today.toString()
            val attendanceToday = RetrofitClient.api.getAttendance(todayStr)
            presentToday = attendanceToday.count { it.status == "present" }

            // This week's Monday through Friday only - the daycare doesn't operate on
            // weekends, so the chart never shows Sat/Sun columns regardless of what day
            // today happens to be. (DayOfWeek.value is 1=Monday..7=Sunday.)
            val mondayOffset = if (today.dayOfWeek == java.time.DayOfWeek.SUNDAY) 6L else (today.dayOfWeek.value - 1).toLong()
            val monday = today.minusDays(mondayOffset)
            val days = (0..4).map { monday.plusDays(it.toLong()) }
            val range = RetrofitClient.api.getAttendanceRange(days.first().toString(), days.last().toString())
            weeklyData = days.map { d ->
                val dayStr = d.toString()
                DayAttendance(
                    label = d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    present = range.count { it.date == dayStr && it.status == "present" },
                )
            }
        } catch (_: Exception) { }
        loading = false
    }

    val displayName = listOfNotNull(user?.firstName, user?.lastName)
        .joinToString(" ").ifEmpty { user?.email ?: "there" }
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11  -> "Good morning"
        in 12..16 -> "Good afternoon"
        else      -> "Good evening"
    }
    val dateText = today.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))

    val attendanceRate = if ((activeChildren ?: 0) > 0)
        "${((presentToday ?: 0) * 100) / (activeChildren ?: 1)}%"
    else "—"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Header ─────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp),
        ) {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Outlined.Menu, contentDescription = "Open navigation", tint = textColor)
            }
            Column(Modifier.padding(start = 4.dp)) {
                Text("$greeting, $displayName", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
                Text(dateText, fontSize = 14.sp, color = mutedColor)
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            if (loading) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green40)
                }
            } else {
                // ── Stat grid (2x2) ────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(Modifier.weight(1f), Icons.Outlined.Groups, "Active Children", activeChildren?.toString() ?: "—", StatGreenBg, StatGreenFg, cardBg, borderColor, textColor, mutedColor)
                        StatCard(Modifier.weight(1f), Icons.AutoMirrored.Outlined.FactCheck, "Present Today", presentToday?.toString() ?: "—", StatBlueBg, StatBlueFg, cardBg, borderColor, textColor, mutedColor)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(Modifier.weight(1f), Icons.Outlined.PersonAddAlt, "Total Enrolled", totalChildren?.toString() ?: "—", StatVioletBg, StatVioletFg, cardBg, borderColor, textColor, mutedColor)
                        StatCard(Modifier.weight(1f), Icons.Outlined.BarChart, "Attendance Rate", attendanceRate, StatAmberBg, StatAmberFg, cardBg, borderColor, textColor, mutedColor)
                    }
                }

                // ── Weekly attendance chart ────────────────────────────
                Column {
                    SectionHeader("Weekly Attendance", "View all →", onTakeAttendance, textColor)
                    Spacer(Modifier.height(16.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(cardBg, RoundedCornerShape(12.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                    ) {
                        WeeklyAttendanceChart(weeklyData, borderColor, mutedColor)
                    }
                }

                // ── Quick actions ───────────────────────────────────────
                Column {
                    SectionHeader("Quick Actions", null, null, textColor)
                    Spacer(Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            QuickActionCard(Modifier.weight(1f), Icons.Outlined.PersonAddAlt, "Add Child", onAddChild, cardBg, borderColor, chipBg, textColor)
                            QuickActionCard(Modifier.weight(1f), Icons.AutoMirrored.Outlined.FactCheck, "Take Attendance", onTakeAttendance, cardBg, borderColor, chipBg, textColor)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            QuickActionCard(Modifier.weight(1f), Icons.Outlined.MonitorHeart, "Health Record", onAddHealthRecord, cardBg, borderColor, chipBg, textColor)
                            QuickActionCard(Modifier.weight(1f), Icons.Outlined.BarChart, "View Reports", onViewReports, cardBg, borderColor, chipBg, textColor)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String?, onAction: (() -> Unit)?, textColor: Color = Charcoal) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = textColor)
        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Green40,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    badgeBg: Color,
    badgeFg: Color,
    cardBg: Color = CardSurface,
    borderColor: Color = BorderGray,
    textColor: Color = Charcoal,
    mutedColor: Color = MutedGray,
) {
    Row(
        modifier = modifier
            .background(cardBg, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(badgeBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = badgeFg, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(label, fontSize = 12.sp, color = mutedColor, maxLines = 1)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    cardBg: Color = CardSurface,
    borderColor: Color = BorderGray,
    chipBg: Color = Green95,
    textColor: Color = Charcoal,
) {
    Column(
        modifier = modifier
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(chipBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Green40, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textColor, textAlign = TextAlign.Center)
    }
}

@Composable
private fun WeeklyAttendanceChart(data: List<DayAttendance>, gridColor: Color = BorderGray, mutedColor: Color = MutedGray) {
    var selected by remember { mutableStateOf<Int?>(null) }
    val maxVal = (data.maxOfOrNull { it.present } ?: 0).coerceAtLeast(1)
    val chartHeight = 120.dp

    Column {
        Box(Modifier.fillMaxWidth().height(chartHeight)) {
            Canvas(Modifier.fillMaxSize()) {
                val steps = 4
                val stepY = size.height / steps
                for (i in 0..steps) {
                    val y = stepY * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                    )
                }
            }
            if (data.isEmpty()) {
                Text("No attendance data yet", fontSize = 12.sp, color = mutedColor, modifier = Modifier.align(Alignment.Center))
            } else {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    data.forEachIndexed { i, d ->
                        Box(
                            modifier = Modifier.weight(1f).fillMaxSize(),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            val frac = (d.present.toFloat() / maxVal).coerceIn(if (d.present > 0) 0.04f else 0f, 1f)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (selected == i) {
                                    Text(d.present.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Green40)
                                    Spacer(Modifier.height(2.dp))
                                }
                                Box(
                                    Modifier
                                        .width(18.dp)
                                        .height(chartHeight * frac)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(Green40)
                                        .clickable { selected = if (selected == i) null else i },
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            data.forEach { d ->
                Text(d.label, fontSize = 11.sp, color = mutedColor, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
    }
}
