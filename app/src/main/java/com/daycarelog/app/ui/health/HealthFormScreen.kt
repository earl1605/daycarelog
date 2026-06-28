package com.daycarelog.app.ui.health

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycarelog.app.data.api.RetrofitClient
import com.daycarelog.app.data.model.Child
import com.daycarelog.app.data.model.HealthRecord
import com.daycarelog.app.ui.common.DateField
import com.daycarelog.app.ui.common.digitsToIso
import com.daycarelog.app.ui.common.isoToDigits
import kotlinx.coroutines.launch

private val Green500 = Color(0xFF16a34a)
private val Green900 = Color(0xFF052e16)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthFormScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var children       by remember { mutableStateOf<List<Child>>(emptyList()) }
    var selectedChild  by remember { mutableStateOf<Child?>(null) }
    var childExpanded  by remember { mutableStateOf(false) }
    var dateDigits     by remember { mutableStateOf(isoToDigits(java.time.LocalDate.now().toString())) }
    var weight         by remember { mutableStateOf("") }
    var height         by remember { mutableStateOf("") }
    var remarks        by remember { mutableStateOf("") }
    var loading        by remember { mutableStateOf(true) }
    var saving         by remember { mutableStateOf(false) }
    var error          by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            children = RetrofitClient.api.getChildren()
        } catch (e: Exception) {
            error = e.message
        }
        loading = false
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Green500,
        focusedLabelColor  = Green500,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf0fdf4)),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Green900)
                .padding(horizontal = 8.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← Back", color = Color.White) }
                Text("Add Health Record", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green500)
            }
            else -> Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!error.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFFfee2e2),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(error!!, color = Color(0xFF991b1b), fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                    }
                }

                // Child picker
                ExposedDropdownMenuBox(
                    expanded = childExpanded,
                    onExpandedChange = { childExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedChild?.let { "${it.firstName} ${it.lastName}" } ?: "— Select Child —",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Child *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(childExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = fieldColors, shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(expanded = childExpanded, onDismissRequest = { childExpanded = false }) {
                        children.forEach { c ->
                            DropdownMenuItem(
                                text = { Text("${c.firstName} ${c.lastName}") },
                                onClick = { selectedChild = c; childExpanded = false },
                            )
                        }
                    }
                }

                DateField(
                    digits = dateDigits,
                    onDigitsChange = { dateDigits = it },
                    label = "Date",
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = weight, onValueChange = { weight = it },
                        label = { Text("Weight (kg)") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = fieldColors, shape = RoundedCornerShape(12.dp),
                    )
                    OutlinedTextField(
                        value = height, onValueChange = { height = it },
                        label = { Text("Height (cm)") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = fieldColors, shape = RoundedCornerShape(12.dp),
                    )
                }

                OutlinedTextField(
                    value = remarks, onValueChange = { remarks = it },
                    label = { Text("Remarks / Notes") },
                    minLines = 3, maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors, shape = RoundedCornerShape(12.dp),
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        when {
                            selectedChild == null -> error = "Please select a child"
                            dateDigits.length < 8 -> error = "Enter a complete date (MM/DD/YYYY)"
                            weight.isBlank() && height.isBlank() -> error = "Enter at least weight or height"
                            else -> {
                                saving = true
                                error  = null
                                scope.launch {
                                    try {
                                        RetrofitClient.api.createHealthRecord(
                                            HealthRecord(
                                                childId  = selectedChild!!.id!!,
                                                date     = digitsToIso(dateDigits),
                                                weightKg = weight.toDoubleOrNull(),
                                                heightCm = height.toDoubleOrNull(),
                                                remarks  = remarks.trim().ifBlank { null },
                                            )
                                        )
                                        onBack()
                                    } catch (e: Exception) {
                                        error = e.message
                                    }
                                    saving = false
                                }
                            }
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green500),
                ) {
                    if (saving) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Save Record", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
