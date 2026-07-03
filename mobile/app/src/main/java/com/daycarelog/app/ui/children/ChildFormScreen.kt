package com.daycarelog.app.ui.children

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycarelog.app.data.api.RetrofitClient
import com.daycarelog.app.data.model.Child
import com.daycarelog.app.ui.common.DateField
import com.daycarelog.app.ui.common.digitsToIso
import com.daycarelog.app.ui.common.isoToDigits
import com.daycarelog.app.ui.guardians.GuardiansSection
import com.daycarelog.app.ui.theme.rememberScreenPalette
import com.daycarelog.app.util.capitalizeWords
import kotlinx.coroutines.launch

private val Green500 = Color(0xFF16a34a)
private val Green900 = Color(0xFF052e16)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildFormScreen(childId: Long?, onBack: () -> Unit) {
    val scope  = rememberCoroutineScope()
    val palette = rememberScreenPalette()
    val isEdit = childId != null
    var firstName        by remember { mutableStateOf("") }
    var lastName         by remember { mutableStateOf("") }
    var dobDigits        by remember { mutableStateOf("") }
    var sex              by remember { mutableStateOf("male") }
    var address          by remember { mutableStateOf("") }
    var enrollDateDigits by remember { mutableStateOf(isoToDigits(java.time.LocalDate.now().toString())) }
    var enrollmentStatus by remember { mutableStateOf("active") }
    var loading          by remember { mutableStateOf(isEdit) }
    var saving           by remember { mutableStateOf(false) }
    var error            by remember { mutableStateOf<String?>(null) }
    var sexExpanded      by remember { mutableStateOf(false) }
    var statusExpanded   by remember { mutableStateOf(false) }

    LaunchedEffect(childId) {
        if (childId != null) {
            try {
                val c = RetrofitClient.api.getChild(childId)
                firstName        = c.firstName
                lastName         = c.lastName
                dobDigits        = isoToDigits(c.dateOfBirth)
                sex              = c.sex
                address          = c.address ?: ""
                enrollDateDigits = isoToDigits(c.enrollmentDate)
                enrollmentStatus = c.enrollmentStatus
            } catch (e: Exception) {
                error = e.message
            }
            loading = false
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Green500,
        focusedLabelColor = Green500,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.pageBg),
    ) {
        // Top bar
        Box(
            Modifier
                .fillMaxWidth()
                .background(Green900)
                .padding(horizontal = 8.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← Back", color = Color.White) }
                Text(
                    if (isEdit) "Edit Child" else "Add Child",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp),
                )
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

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = firstName, onValueChange = { firstName = capitalizeWords(it) },
                        label = { Text("First Name *") }, singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors, shape = RoundedCornerShape(12.dp),
                    )
                    OutlinedTextField(
                        value = lastName, onValueChange = { lastName = capitalizeWords(it) },
                        label = { Text("Last Name *") }, singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = fieldColors, shape = RoundedCornerShape(12.dp),
                    )
                }

                DateField(
                    digits = dobDigits,
                    onDigitsChange = { dobDigits = it },
                    label = "Date of Birth *",
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                )

                ExposedDropdownMenuBox(
                    expanded = sexExpanded,
                    onExpandedChange = { sexExpanded = it },
                ) {
                    OutlinedTextField(
                        value = sex.replaceFirstChar { it.uppercase() },
                        onValueChange = {}, readOnly = true,
                        label = { Text("Sex *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sexExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = fieldColors, shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(expanded = sexExpanded, onDismissRequest = { sexExpanded = false }) {
                        listOf("male", "female").forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.replaceFirstChar { it.uppercase() }) },
                                onClick = { sex = s; sexExpanded = false },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = address, onValueChange = { address = capitalizeWords(it) },
                    label = { Text("Address") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors, shape = RoundedCornerShape(12.dp),
                )

                DateField(
                    digits = enrollDateDigits,
                    onDigitsChange = { enrollDateDigits = it },
                    label = "Enrollment Date",
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                )

                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it },
                ) {
                    OutlinedTextField(
                        value = enrollmentStatus.replaceFirstChar { it.uppercase() },
                        onValueChange = {}, readOnly = true,
                        label = { Text("Enrollment Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = fieldColors, shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        listOf("active", "inactive").forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.replaceFirstChar { it.uppercase() }) },
                                onClick = { enrollmentStatus = s; statusExpanded = false },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        when {
                            firstName.isBlank() || lastName.isBlank() ->
                                error = "First and last name are required"
                            dobDigits.length < 8 -> error = "Enter a complete date of birth (MM/DD/YYYY)"
                            else -> {
                                saving = true
                                error  = null
                                scope.launch {
                                    try {
                                        val child = Child(
                                            id = childId,
                                            firstName = firstName.trim(),
                                            lastName = lastName.trim(),
                                            dateOfBirth = digitsToIso(dobDigits),
                                            sex = sex,
                                            address = address.trim().ifBlank { null },
                                            enrollmentDate = digitsToIso(enrollDateDigits),
                                            enrollmentStatus = enrollmentStatus,
                                        )
                                        if (childId != null) RetrofitClient.api.updateChild(childId, child)
                                        else RetrofitClient.api.createChild(child)
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
                    else Text(if (isEdit) "Save Changes" else "Add Child", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                if (isEdit && childId != null) {
                    Spacer(Modifier.height(4.dp))
                    GuardiansSection(childId)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
