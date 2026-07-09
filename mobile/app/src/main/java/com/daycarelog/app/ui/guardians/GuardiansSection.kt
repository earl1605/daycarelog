package com.daycarelog.app.ui.guardians

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycarelog.app.data.api.RetrofitClient
import com.daycarelog.app.data.model.Guardian
import com.daycarelog.app.data.model.GuardianRequest
import com.daycarelog.app.ui.theme.rememberScreenPalette
import com.daycarelog.app.util.capitalizeWords
import kotlinx.coroutines.launch

private val Green500 = Color(0xFF16a34a)
private val Green900 = Color(0xFF052e16)

@Composable
fun GuardiansSection(childId: Long) {
    val scope = rememberCoroutineScope()
    val palette = rememberScreenPalette()
    val clipboard = LocalClipboardManager.current

    var guardians by remember { mutableStateOf<List<Guardian>>(emptyList()) }
    var loading   by remember { mutableStateOf(true) }
    var showForm  by remember { mutableStateOf(false) }

    var name                by remember { mutableStateOf("") }
    var relationship        by remember { mutableStateOf("") }
    var contactNumber       by remember { mutableStateOf("") }
    var createPortalAccount by remember { mutableStateOf(false) }
    var email               by remember { mutableStateOf("") }
    var saving              by remember { mutableStateOf(false) }
    var formError           by remember { mutableStateOf<String?>(null) }
    var tempPassword        by remember { mutableStateOf<Pair<String, String>?>(null) }

    fun load() {
        scope.launch {
            loading = true
            try { guardians = RetrofitClient.api.getGuardians(childId) } catch (_: Exception) { }
            loading = false
        }
    }
    LaunchedEffect(childId) { load() }

    tempPassword?.let { (who, pass) ->
        AlertDialog(
            onDismissRequest = { tempPassword = null },
            title = { Text("Temporary password") },
            text = {
                Column {
                    Text("Parent portal account for $who. Shown only once.", fontSize = 12.sp, color = palette.mutedColor)
                    Spacer(Modifier.height(8.dp))
                    Surface(color = palette.borderColor, shape = RoundedCornerShape(8.dp)) {
                        Text(pass, modifier = Modifier.padding(10.dp), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { clipboard.setText(AnnotatedString(pass)) }) { Text("Copy") } },
            dismissButton = { TextButton(onClick = { tempPassword = null }) { Text("Done") } },
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBg),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Guardians", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = palette.textColor, modifier = Modifier.weight(1f))
                TextButton(onClick = { showForm = !showForm }) { Text(if (showForm) "Cancel" else "+ Add Guardian", fontSize = 12.sp) }
            }

            if (loading) {
                Box(Modifier.fillMaxWidth().height(40.dp)) { CircularProgressIndicator(modifier = Modifier.height(20.dp), color = Green500) }
            } else if (guardians.isEmpty() && !showForm) {
                Text("No guardians added yet.", fontSize = 12.sp, color = palette.mutedColor)
            } else {
                guardians.forEach { g ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                g.name + if (g.isPrimary) "  •  Primary" else "",
                                fontSize = 13.sp, fontWeight = FontWeight.Medium, color = palette.textColor,
                            )
                            val sub = listOfNotNull(g.relationship, g.contactNumber).joinToString(" · ")
                                .ifBlank { "—" } + if (g.userId != null) "  •  Portal access" else ""
                            Text(sub, fontSize = 11.sp, color = palette.mutedColor)
                        }
                        IconButton(onClick = {
                            scope.launch {
                                val id = g.id ?: return@launch
                                try { RetrofitClient.api.deleteGuardian(childId, id); load() } catch (_: Exception) { }
                            }
                        }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = Color(0xFFef4444), modifier = Modifier.height(18.dp))
                        }
                    }
                }
            }

            if (showForm) {
                formError?.let { Text(it, color = Color(0xFFdc2626), fontSize = 12.sp) }
                OutlinedTextField(value = name, onValueChange = { name = capitalizeWords(it) }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = relationship, onValueChange = { relationship = capitalizeWords(it) }, label = { Text("Relationship") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contactNumber, onValueChange = { contactNumber = it }, label = { Text("Contact number") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = createPortalAccount, onCheckedChange = { createPortalAccount = it })
                    Text("Create a parent portal account", fontSize = 13.sp)
                }
                if (createPortalAccount) {
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text(
                        "If this email already has a parent account, this child is linked to it instead of creating a new one.",
                        fontSize = 10.sp, color = palette.mutedColor,
                    )
                }

                Button(
                    onClick = {
                        when {
                            name.isBlank() -> formError = "Guardian name is required"
                            createPortalAccount && email.isBlank() -> formError = "Email is required to create a portal account"
                            else -> {
                                saving = true
                                formError = null
                                scope.launch {
                                    try {
                                        val res = RetrofitClient.api.addGuardian(
                                            childId,
                                            GuardianRequest(
                                                name = name.trim(), relationship = relationship.trim().ifBlank { null },
                                                contactNumber = contactNumber.trim().ifBlank { null },
                                                createPortalAccount = createPortalAccount,
                                                email = email.trim().ifBlank { null },
                                            ),
                                        )
                                        val savedName = name
                                        name = ""; relationship = ""; contactNumber = ""; email = ""; createPortalAccount = false
                                        showForm = false
                                        load()
                                        if (!res.tempPassword.isNullOrBlank()) tempPassword = savedName to res.tempPassword
                                    } catch (e: Exception) {
                                        formError = e.message
                                    }
                                    saving = false
                                }
                            }
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Green500),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (saving) CircularProgressIndicator(modifier = Modifier.height(18.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Add Guardian", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}
