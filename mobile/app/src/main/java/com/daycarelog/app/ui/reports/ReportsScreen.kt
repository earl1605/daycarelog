package com.daycarelog.app.ui.reports

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycarelog.app.data.api.RetrofitClient
import com.daycarelog.app.data.model.MonthlyReport
import kotlinx.coroutines.launch

private val Green500 = Color(0xFF16a34a)
private val Green100 = Color(0xFFdcfce7)
private val Green900 = Color(0xFF052e16)
private val Green700 = Color(0xFF15803d)

@Composable
fun ReportsScreen(onOpenDrawer: () -> Unit) {
    val scope = rememberCoroutineScope()
    val currentMonth = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
    var month   by remember { mutableStateOf(currentMonth) }
    var report  by remember { mutableStateOf<MonthlyReport?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error   by remember { mutableStateOf<String?>(null) }

    fun loadReport() {
        loading = true
        error   = null
        report  = null
        scope.launch {
            try {
                report = RetrofitClient.api.getMonthlyReport(month)
            } catch (e: Exception) {
                error = e.message
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { loadReport() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf0fdf4)),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Green900)
                .padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(Icons.Outlined.Menu, contentDescription = "Open navigation", tint = Color.White)
                }
                Text("Monthly Reports", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Month picker row
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Month (YYYY-MM)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green500, focusedLabelColor = Green500),
                    shape = RoundedCornerShape(12.dp),
                )
                Button(
                    onClick = { loadReport() },
                    colors = ButtonDefaults.buttonColors(containerColor = Green500),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("Load") }
            }

            when {
                loading -> Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green500)
                }
                !error.isNullOrBlank() -> {
                    Surface(
                        color = Color(0xFFfee2e2),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "Could not load report: $error",
                            color = Color(0xFF991b1b),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                }
                report == null -> Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No report data available.", color = Color.Gray)
                }
                else -> {
                    val r = report!!
                    // Attendance summary card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                    ) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Attendance Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Green900)
                            Surface(color = Green100, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Month", color = Green700, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(r.month, color = Green700, fontSize = 13.sp)
                                }
                            }
                            ReportRow("Total Enrolled", r.total.toString(), Green500)
                            ReportRow("School Days", r.schoolDays.toString(), Color(0xFF2563eb))
                            ReportRow("Total Present", r.presentCount.toString(), Color(0xFF16a34a))
                            ReportRow("Total Absent",  r.absentCount.toString(),  Color(0xFFdc2626))
                            // Attendance rate big badge
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .background(Green100, RoundedCornerShape(12.dp))
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "%.1f%%".format(r.attendanceRate),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Green700,
                                    )
                                    Text("Attendance Rate", fontSize = 12.sp, color = Green700)
                                }
                            }
                        }
                    }

                    // Nutritional status card
                    if (r.nutritionalStatus.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                        ) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Nutritional Status", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Green900)
                                r.nutritionalStatus.entries.sortedBy { it.key }.forEach { (status, count) ->
                                    ReportRow(
                                        status.replace('_', ' ').replaceFirstChar { it.uppercase() },
                                        count.toString(),
                                        Color(0xFF7c3aed),
                                    )
                                }
                            }
                        }
                    }

                    // Children list
                    if (r.children.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                        ) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Children in Report (${r.children.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Green900)
                                r.children.forEach { child ->
                                    Surface(
                                        color = Color(0xFFf9fafb),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(
                                            "• ${child.firstName} ${child.lastName}",
                                            fontSize = 13.sp,
                                            color = Color(0xFF374151),
                                            modifier = Modifier.padding(8.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String, valueColor: Color) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF374151))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
