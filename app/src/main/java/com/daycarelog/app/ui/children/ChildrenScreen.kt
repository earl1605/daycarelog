package com.daycarelog.app.ui.children

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
import kotlinx.coroutines.launch

private val Green500 = Color(0xFF16a34a)
private val Green100 = Color(0xFFdcfce7)
private val Green900 = Color(0xFF052e16)

@Composable
fun ChildrenScreen(
    onAddChild: () -> Unit,
    onEditChild: (Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var children     by remember { mutableStateOf<List<Child>>(emptyList()) }
    var loading      by remember { mutableStateOf(true) }
    var error        by remember { mutableStateOf<String?>(null) }
    var search       by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<Child?>(null) }

    fun refresh() {
        scope.launch {
            loading = true
            try {
                children = RetrofitClient.api.getChildren()
                error    = null
            } catch (e: Exception) {
                error = e.message
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    deleteTarget?.let { child ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Child") },
            text  = { Text("Remove ${child.firstName} ${child.lastName} from the system?") },
            confirmButton = {
                TextButton(onClick = {
                    val id = child.id ?: return@TextButton
                    deleteTarget = null
                    scope.launch {
                        try {
                            RetrofitClient.api.deleteChild(id)
                            children = RetrofitClient.api.getChildren()
                        } catch (_: Exception) {}
                    }
                }) { Text("Delete", color = Color(0xFFef4444)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddChild,
                containerColor = Green500,
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Light)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFf0fdf4))
                .padding(padding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Green900)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Text("Children", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search by name…") },
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
                error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $error", color = Color.Red)
                }
                else -> {
                    val filtered = children.filter {
                        val q = search.trim().lowercase()
                        q.isBlank() || "${it.firstName} ${it.lastName}".lowercase().contains(q)
                    }
                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                if (search.isBlank()) "No children found.\nTap + to add one."
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
                            items(filtered) { child ->
                                ChildCard(
                                    child    = child,
                                    onEdit   = { child.id?.let { onEditChild(it) } },
                                    onDelete = { deleteTarget = child },
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
private fun ChildCard(child: Child, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Green100, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    child.firstName.firstOrNull()?.uppercase() ?: "?",
                    color = Color(0xFF15803d),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${child.firstName} ${child.lastName}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF111827),
                )
                Text(
                    "DOB: ${child.dateOfBirth}  •  ${child.sex.replaceFirstChar { it.uppercase() }}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (child.enrollmentStatus == "active") Green100 else Color(0xFFf3f4f6),
            ) {
                Text(
                    child.enrollmentStatus.replaceFirstChar { it.uppercase() },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (child.enrollmentStatus == "active") Color(0xFF15803d) else Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                TextButton(onClick = onEdit, contentPadding = PaddingValues(0.dp)) {
                    Text("Edit", color = Color(0xFF2563eb), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onDelete, contentPadding = PaddingValues(0.dp)) {
                    Text("Delete", color = Color(0xFFef4444), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
