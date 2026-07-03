package com.daycarelog.app.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.daycarelog.app.data.model.AttendanceRecord
import com.daycarelog.app.data.model.Child
import kotlinx.coroutines.launch

private val Green500 = Color(0xFF16a34a)
private val Green900 = Color(0xFF052e16)
private val Green100 = Color(0xFFdcfce7)

private val statusOptions = listOf("present", "absent", "late", "excused")
private val statusColors = mapOf(
    "present" to (Color(0xFF16a34a) to Color(0xFFdcfce7)),
    "absent"  to (Color(0xFFdc2626) to Color(0xFFfee2e2)),
    "late"    to (Color(0xFFd97706) to Color(0xFFfef3c7)),
    "excused" to (Color(0xFF7c3aed) to Color(0xFFede9fe)),
)

// The daycare only operates Monday-Friday. If "today" is a weekend, default to the
// most recent Friday rather than opening on a date that can never have attendance.
private fun nearestWeekday(date: java.time.LocalDate): java.time.LocalDate = when (date.dayOfWeek) {
    java.time.DayOfWeek.SATURDAY -> date.minusDays(1)
    java.time.DayOfWeek.SUNDAY   -> date.minusDays(2)
    else -> date
}

private fun isWeekend(dateStr: String): Boolean = try {
    val d = java.time.LocalDate.parse(dateStr)
    d.dayOfWeek == java.time.DayOfWeek.SATURDAY || d.dayOfWeek == java.time.DayOfWeek.SUNDAY
} catch (e: Exception) { false }

@Composable
fun AttendanceScreen(onOpenDrawer: () -> Unit) {
    val scope   = rememberCoroutineScope()
    var date    by remember { mutableStateOf(nearestWeekday(java.time.LocalDate.now()).toString()) }
    var children by remember { mutableStateOf<List<Child>>(emptyList()) }
    var loading  by remember { mutableStateOf(true) }
    var saving   by remember { mutableStateOf(false) }
    var saved    by remember { mutableStateOf(false) }
    var error    by remember { mutableStateOf<String?>(null) }
    val statusMap = remember { mutableStateMapOf<Long, String>() }

    fun loadAttendance(d: String) {
        if (isWeekend(d)) {
            error = "Attendance can only be recorded for Monday–Friday"
            return
        }
        scope.launch {
            loading = true
            saved   = false
            try {
                val ch = if (children.isEmpty()) RetrofitClient.api.getChildren() else children
                children = ch
                statusMap.clear()
                val existing = RetrofitClient.api.getAttendance(d)
                existing.forEach { rec -> statusMap[rec.childId] = rec.status }
                ch.forEach { c -> c.id?.let { if (!statusMap.containsKey(it)) statusMap[it] = "absent" } }
                error = null
            } catch (e: Exception) {
                error = e.message
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { loadAttendance(date) }

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
                Text("Attendance", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }
        }

        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD)") },
                singleLine = true,
                trailingIcon = {
                    Button(
                        onClick = { loadAttendance(date) },
                        colors = ButtonDefaults.buttonColors(containerColor = Green500),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 4.dp),
                    ) { Text("Load", fontSize = 12.sp) }
                },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green500, focusedLabelColor = Green500),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            )
            Spacer(Modifier.height(8.dp))

            if (!loading && children.isNotEmpty()) {
                val present = statusMap.values.count { it == "present" }
                val absent  = statusMap.values.count { it == "absent" }
                val late    = statusMap.values.count { it == "late" }
                val excused = statusMap.values.count { it == "excused" }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple("Present", present, Color(0xFF16a34a)),
                        Triple("Absent",  absent,  Color(0xFFdc2626)),
                        Triple("Late",    late,    Color(0xFFd97706)),
                        Triple("Excused", excused, Color(0xFF7c3aed)),
                    ).forEach { (label, count, color) ->
                        Surface(
                            color = color.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Column(
                                Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
                                Text(label, fontSize = 9.sp, color = color)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (saved) {
                Surface(color = Green100, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("✓  Attendance saved!", color = Color(0xFF15803d), fontSize = 13.sp, modifier = Modifier.padding(10.dp))
                }
                Spacer(Modifier.height(8.dp))
            }

            if (!error.isNullOrBlank()) {
                Surface(color = Color(0xFFfee2e2), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(error!!, color = Color(0xFF991b1b), fontSize = 13.sp, modifier = Modifier.padding(10.dp))
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green500)
            }
            children.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No children found. Add children first.", color = Color.Gray)
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(children) { child ->
                        val id     = child.id ?: return@items
                        val status = statusMap[id] ?: "absent"
                        AttendanceRow(child = child, status = status, onStatusChange = { statusMap[id] = it })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                Box(Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                saving = true
                                saved  = false
                                try {
                                    val records = children.mapNotNull { c ->
                                        c.id?.let { id ->
                                            AttendanceRecord(childId = id, date = date, status = statusMap[id] ?: "absent")
                                        }
                                    }
                                    RetrofitClient.api.saveAttendanceBulk(records)
                                    saved = true
                                    error = null
                                } catch (e: Exception) {
                                    error = e.message
                                }
                                saving = false
                            }
                        },
                        enabled = !saving,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green500),
                    ) {
                        if (saving) CircularProgressIndicator(modifier = Modifier.padding(4.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Save Attendance", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceRow(child: Child, status: String, onStatusChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "${child.firstName} ${child.lastName}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF111827),
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                statusOptions.forEach { s ->
                    val (fg, bg) = statusColors[s] ?: (Color.Gray to Color(0xFFf3f4f6))
                    FilterChip(
                        selected = status == s,
                        onClick = { onStatusChange(s) },
                        label = { Text(s.replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = bg,
                            selectedLabelColor = fg,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    )
                }
            }
        }
    }
}
