package com.daycarelog.app.ui.health

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.daycarelog.app.data.model.Child
import com.daycarelog.app.data.model.HealthRecord
import kotlinx.coroutines.launch

private val Green500 = Color(0xFF16a34a)
private val Green900 = Color(0xFF052e16)

@Composable
fun HealthScreen(onAdd: () -> Unit) {
    val scope   = rememberCoroutineScope()
    var records      by remember { mutableStateOf<List<HealthRecord>>(emptyList()) }
    var children     by remember { mutableStateOf<Map<Long, Child>>(emptyMap()) }
    var loading      by remember { mutableStateOf(true) }
    var error        by remember { mutableStateOf<String?>(null) }
    var search       by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<HealthRecord?>(null) }

    fun load() {
        scope.launch {
            loading = true
            try {
                records  = RetrofitClient.api.getHealthRecords()
                children = RetrofitClient.api.getChildren().associateBy { it.id ?: 0L }
                error    = null
            } catch (e: Exception) {
                error = e.message
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    deleteTarget?.let { rec ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Record") },
            text  = { Text("Remove this health record?") },
            confirmButton = {
                TextButton(onClick = {
                    val id = rec.id ?: return@TextButton
                    deleteTarget = null
                    scope.launch {
                        try { RetrofitClient.api.deleteHealthRecord(id); load() } catch (_: Exception) {}
                    }
                }) { Text("Delete", color = Color(0xFFef4444)) }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } },
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = Green500,
                contentColor = Color.White,
                shape = CircleShape,
            ) { Text("+", fontSize = 28.sp, fontWeight = FontWeight.Light) }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFf0fdf4))
                .padding(padding),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Green900)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Text("Health Records", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search by child name…") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
            )

            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green500)
                }
                !error.isNullOrBlank() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $error", color = Color.Red)
                }
                else -> {
                    val filtered = records.filter { rec ->
                        val child = children[rec.childId]
                        val name  = "${child?.firstName} ${child?.lastName}".lowercase()
                        search.isBlank() || name.contains(search.trim().lowercase())
                    }
                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                if (search.isBlank()) "No health records.\nTap + to add one."
                                else "No results for \"$search\"",
                                color = Color.Gray,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            item { Spacer(Modifier.height(4.dp)) }
                            items(filtered) { rec ->
                                HealthRecordCard(
                                    record   = rec,
                                    child    = children[rec.childId],
                                    onDelete = { deleteTarget = rec },
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthRecordCard(record: HealthRecord, child: Child?, onDelete: () -> Unit) {
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
                    "${child?.firstName ?: "?"} ${child?.lastName ?: ""}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF111827),
                )
                Text("Date: ${record.date}", fontSize = 12.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    record.weightKg?.let { Text("⚖️ ${it}kg", fontSize = 12.sp, color = Color(0xFF374151)) }
                    record.heightCm?.let { Text("📏 ${it}cm", fontSize = 12.sp, color = Color(0xFF374151)) }
                }
                if (!record.remarks.isNullOrBlank()) {
                    Surface(color = Color(0xFFf9fafb), shape = RoundedCornerShape(8.dp)) {
                        Text(record.remarks, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(6.dp))
                    }
                }
            }
            TextButton(
                onClick = onDelete,
                contentPadding = PaddingValues(0.dp),
            ) { Text("Delete", color = Color(0xFFef4444), fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
        }
    }
}
