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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycarelog.app.data.api.RetrofitClient
import com.daycarelog.app.data.model.Child
import com.daycarelog.app.data.model.HealthRecord
import com.daycarelog.app.util.classifyNutritionalStatus
import com.daycarelog.app.util.nutritionalStatusColors

private val Green500 = Color(0xFF16a34a)
private val Green900 = Color(0xFF052e16)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentHealthScreen(onOpenDrawer: () -> Unit) {
    var children    by remember { mutableStateOf<List<Child>>(emptyList()) }
    var records     by remember { mutableStateOf<List<HealthRecord>>(emptyList()) }
    var loading     by remember { mutableStateOf(true) }
    var error       by remember { mutableStateOf<String?>(null) }
    var childFilter by remember { mutableStateOf<Long?>(null) }
    var filterExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            children = RetrofitClient.api.getMyChildren()
            records  = RetrofitClient.api.getMyHealthRecords()
            error = null
        } catch (e: Exception) {
            error = e.message
        }
        loading = false
    }

    val childMap = children.associateBy { it.id }
    val filtered = if (childFilter == null) records else records.filter { it.childId == childFilter }

    Column(Modifier.fillMaxSize().background(Color(0xFFf0fdf4))) {
        Box(Modifier.fillMaxWidth().background(Green900).padding(horizontal = 8.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(Icons.Outlined.Menu, contentDescription = "Open navigation", tint = Color.White)
                }
                Text("Health Records", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }
        }

        if (children.size > 1) {
            ExposedDropdownMenuBox(
                expanded = filterExpanded,
                onExpandedChange = { filterExpanded = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                OutlinedTextField(
                    value = childMap[childFilter]?.let { "${it.firstName} ${it.lastName}" } ?: "All children",
                    onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(filterExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                )
                ExposedDropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                    DropdownMenuItem(text = { Text("All children") }, onClick = { childFilter = null; filterExpanded = false })
                    children.forEach { c ->
                        DropdownMenuItem(
                            text = { Text("${c.firstName} ${c.lastName}") },
                            onClick = { childFilter = c.id; filterExpanded = false },
                        )
                    }
                }
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green500)
            }
            !error.isNullOrBlank() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $error", color = Color.Red)
            }
            filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No health records yet.", color = Color.Gray)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(filtered.sortedByDescending { it.measurementDate }) { rec ->
                    val child = childMap[rec.childId]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                Modifier.size(44.dp).background(Color(0xFFfce7f3), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) { Text("❤️", fontSize = 20.sp) }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    child?.let { "${it.firstName} ${it.lastName}" } ?: "—",
                                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF111827),
                                )
                                Text("Date: ${rec.measurementDate}", fontSize = 12.sp, color = Color.Gray)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    rec.weightKg?.let { Text("⚖️ ${it}kg", fontSize = 12.sp, color = Color(0xFF374151)) }
                                    rec.heightCm?.let { Text("📏 ${it}cm", fontSize = 12.sp, color = Color(0xFF374151)) }
                                }
                                if (child != null) {
                                    val status = classifyNutritionalStatus(rec.weightKg, child.dateOfBirth, child.sex)
                                    val (bg, fg) = nutritionalStatusColors(status.color)
                                    Surface(color = bg, shape = RoundedCornerShape(20.dp)) {
                                        Text(
                                            status.label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = fg,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        )
                                    }
                                }
                                if (!rec.remarks.isNullOrBlank()) {
                                    Surface(color = Color(0xFFf9fafb), shape = RoundedCornerShape(8.dp)) {
                                        Text(rec.remarks, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(6.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}
