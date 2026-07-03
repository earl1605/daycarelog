package com.daycarelog.app.ui.guardians

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycarelog.app.data.api.RetrofitClient
import com.daycarelog.app.data.model.Child
import com.daycarelog.app.data.model.GuardianAccountResponse
import com.daycarelog.app.data.model.GuardianRequest
import com.daycarelog.app.util.capitalizeWords
import kotlinx.coroutines.launch

private val Green500 = Color(0xFF16a34a)
private val Green900 = Color(0xFF052e16)
private val Green100 = Color(0xFFdcfce7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardiansScreen(onOpenDrawer: () -> Unit) {
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    var accounts by remember { mutableStateOf<List<GuardianAccountResponse>>(emptyList()) }
    var children by remember { mutableStateOf<List<Child>>(emptyList()) }
    var loading  by remember { mutableStateOf(true) }
    var search   by remember { mutableStateOf("") }

    var name          by remember { mutableStateOf("") }
    var email         by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var address       by remember { mutableStateOf("") }
    var relationship  by remember { mutableStateOf("") }
    var selectedChild by remember { mutableStateOf<Child?>(null) }
    var childExpanded by remember { mutableStateOf(false) }
    var creating      by remember { mutableStateOf(false) }
    var formError     by remember { mutableStateOf<String?>(null) }
    var tempPassword  by remember { mutableStateOf<Pair<String, String>?>(null) } // name to password
    var confirmRemove by remember { mutableStateOf<GuardianAccountResponse?>(null) }

    fun load() {
        scope.launch {
            loading = true
            try {
                accounts = RetrofitClient.api.getGuardianAccounts()
                children = RetrofitClient.api.getChildren().filter { it.enrollmentStatus == "active" }
            } catch (_: Exception) { }
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    tempPassword?.let { (who, pass) ->
        AlertDialog(
            onDismissRequest = { tempPassword = null },
            title = { Text("Temporary password") },
            text = {
                Column {
                    Text("For $who. Shown only once - copy it now and share it securely.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Surface(color = Color(0xFFf9fafb), shape = RoundedCornerShape(8.dp)) {
                        Text(pass, modifier = Modifier.padding(10.dp), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { clipboard.setText(AnnotatedString(pass)) }) { Text("Copy") }
            },
            dismissButton = { TextButton(onClick = { tempPassword = null }) { Text("Done") } },
        )
    }

    confirmRemove?.let { acct ->
        AlertDialog(
            onDismissRequest = { confirmRemove = null },
            title = { Text("Remove guardian account?") },
            text = { Text("${acct.name} will lose access to all linked children's records. Their login is not deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRemove = null
                    scope.launch {
                        try { RetrofitClient.api.removeGuardianAccount(acct.userId); load() } catch (_: Exception) {}
                    }
                }) { Text("Remove", color = Color(0xFFef4444)) }
            },
            dismissButton = { TextButton(onClick = { confirmRemove = null }) { Text("Cancel") } },
        )
    }

    Column(Modifier.fillMaxSize().background(Color(0xFFf0fdf4))) {
        Box(Modifier.fillMaxWidth().background(Green900).padding(horizontal = 8.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(Icons.Outlined.Menu, contentDescription = "Open navigation", tint = Color.White)
                }
                Text("Guardians", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("+ Create a Guardian Account", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Green900)
                    Text(
                        "Creates a new Parent/Guardian login and generates a temporary password. Public self-registration can never create one.",
                        fontSize = 11.sp, color = Color.Gray,
                    )
                    formError?.let { Text(it, color = Color(0xFFdc2626), fontSize = 12.sp) }

                    OutlinedTextField(
                        value = name, onValueChange = { name = capitalizeWords(it) },
                        label = { Text("Full name") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = contactNumber, onValueChange = { contactNumber = it },
                        label = { Text("Contact number") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = address, onValueChange = { address = capitalizeWords(it) },
                        label = { Text("Address") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = relationship, onValueChange = { relationship = capitalizeWords(it) },
                        label = { Text("Relationship to child") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Mother, Father, Guardian…") },
                    )

                    ExposedDropdownMenuBox(expanded = childExpanded, onExpandedChange = { childExpanded = it }) {
                        OutlinedTextField(
                            value = selectedChild?.let { "${it.firstName} ${it.lastName}" } ?: "Select a child…",
                            onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(childExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
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

                    Button(
                        onClick = {
                            val child = selectedChild
                            when {
                                name.isBlank() -> formError = "Name is required"
                                email.isBlank() -> formError = "Email is required"
                                child?.id == null -> formError = "Please select a child"
                                else -> {
                                    creating = true
                                    formError = null
                                    scope.launch {
                                        try {
                                            val res = RetrofitClient.api.addGuardian(
                                                child.id!!,
                                                GuardianRequest(
                                                    name = name.trim(), relationship = relationship.trim(),
                                                    contactNumber = contactNumber.trim(), address = address.trim(),
                                                    createPortalAccount = true, email = email.trim(),
                                                ),
                                            )
                                            name = ""; email = ""; contactNumber = ""; address = ""; relationship = ""; selectedChild = null
                                            load()
                                            if (!res.tempPassword.isNullOrBlank()) tempPassword = name to res.tempPassword
                                        } catch (e: Exception) {
                                            formError = e.message
                                        }
                                        creating = false
                                    }
                                }
                            }
                        },
                        enabled = !creating,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Green500),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (creating) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Create Guardian Account", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            OutlinedTextField(
                value = search, onValueChange = { search = it },
                placeholder = { Text("Search by name…") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            )

            if (loading) {
                Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green500)
                }
            } else {
                val filtered = accounts.filter { it.name?.contains(search, ignoreCase = true) != false || search.isBlank() }
                if (filtered.isEmpty()) {
                    Text("No guardian accounts found", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    filtered.forEach { acct ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp),
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(acct.name ?: "—", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF111827))
                                Text(acct.email ?: "—", fontSize = 12.sp, color = Color.Gray)
                                if (!acct.contactNumber.isNullOrBlank()) Text(acct.contactNumber, fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    "Children: " + (acct.children.joinToString(", ") { "${it.firstName} ${it.lastName}" }.ifBlank { "—" }),
                                    fontSize = 12.sp, color = Color(0xFF374151),
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    val res = RetrofitClient.api.resetUserPassword(acct.userId)
                                                    tempPassword = (acct.name ?: "Guardian") to res.tempPassword
                                                } catch (_: Exception) { }
                                            }
                                        },
                                        contentPadding = PaddingValues(0.dp),
                                    ) { Text("Reset Password", fontSize = 12.sp, color = Color(0xFF2563eb)) }
                                    TextButton(
                                        onClick = { confirmRemove = acct },
                                        contentPadding = PaddingValues(0.dp),
                                    ) { Text("Remove", fontSize = 12.sp, color = Color(0xFFef4444)) }
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
