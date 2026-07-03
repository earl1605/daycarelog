package com.daycarelog.app.ui.parent

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycarelog.app.data.api.RetrofitClient
import com.daycarelog.app.data.model.AttendanceRecord
import com.daycarelog.app.data.model.Child
import com.daycarelog.app.util.formatAge

private val Green500 = Color(0xFF16a34a)
private val Green900 = Color(0xFF052e16)
private val Green100 = Color(0xFFdcfce7)

@Composable
fun ParentDashboardScreen(onOpenDrawer: () -> Unit) {
    var children   by remember { mutableStateOf<List<Child>>(emptyList()) }
    var attendance by remember { mutableStateOf<List<AttendanceRecord>>(emptyList()) }
    var loading    by remember { mutableStateOf(true) }
    var error      by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            children   = RetrofitClient.api.getMyChildren()
            attendance = RetrofitClient.api.getMyAttendance()
            error = null
        } catch (e: Exception) {
            error = e.message
        }
        loading = false
    }

    val today = java.time.LocalDate.now().toString()
    val presentToday = attendance.count { it.date == today && it.status == "present" }

    Column(Modifier.fillMaxSize().background(Color(0xFFf0fdf4))) {
        Box(
            Modifier.fillMaxWidth().background(Green900).padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(Icons.Outlined.Menu, contentDescription = "Open navigation", tint = Color.White)
                }
                Text("My Dashboard", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green500)
            }
            !error.isNullOrBlank() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $error", color = Color.Red)
            }
            else -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ParentStatCard(Modifier.weight(1f), "My Children", children.size.toString())
                    ParentStatCard(Modifier.weight(1f), "Present Today", presentToday.toString())
                }

                Text("My Children", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF111827))

                if (children.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No children linked to your account yet.", color = Color.Gray, fontSize = 13.sp)
                            Text("Contact daycare staff to link your child's records.", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                } else {
                    children.forEach { child ->
                        val todayAtt = attendance.find { it.childId == child.id && it.date == today }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp),
                        ) {
                            Row(
                                Modifier.padding(14.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    Modifier.size(44.dp).clip(CircleShape).background(Green100),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "${child.firstName.firstOrNull() ?: ' '}${child.lastName.firstOrNull() ?: ' '}",
                                        color = Color(0xFF15803d), fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text("${child.firstName} ${child.lastName}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF111827))
                                    Text("${child.sex} · ${formatAge(child.dateOfBirth)}", fontSize = 12.sp, color = Color.Gray)
                                }
                                Surface(
                                    color = if (todayAtt?.status == "present") Green100 else Color(0xFFf3f4f6),
                                    shape = RoundedCornerShape(20.dp),
                                ) {
                                    Text(
                                        todayAtt?.status?.replaceFirstChar { it.uppercase() } ?: "No record",
                                        fontSize = 11.sp, fontWeight = FontWeight.Medium,
                                        color = if (todayAtt?.status == "present") Color(0xFF15803d) else Color.Gray,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ParentStatCard(modifier: Modifier = Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        }
    }
}
