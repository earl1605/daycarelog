package com.daycarelog.app.ui.users

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.daycarelog.app.data.api.RetrofitClient
import com.daycarelog.app.data.model.CreateUserRequest
import com.daycarelog.app.data.model.UpdateRoleRequest
import com.daycarelog.app.data.model.UserDto
import com.daycarelog.app.data.preferences.TokenDataStore
import com.daycarelog.app.ui.theme.ScreenPalette
import com.daycarelog.app.ui.theme.rememberScreenPalette
import com.daycarelog.app.util.capitalizeWords
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Green500 = Color(0xFF16a34a)
private val Green100 = Color(0xFFdcfce7)
private val Green900 = Color(0xFF052e16)
private val Green700 = Color(0xFF15803d)
private val Red500   = Color(0xFFef4444)
private val Amber600  = Color(0xFFd97706)
private val Blue600  = Color(0xFF2563eb)

private val roles = listOf("admin", "staff")

@Composable
fun UsersScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val palette = rememberScreenPalette()

    var currentUser by remember { mutableStateOf<UserDto?>(null) }
    var users        by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var loading      by remember { mutableStateOf(true) }
    var loadError    by remember { mutableStateOf<String?>(null) }
    var actionError  by remember { mutableStateOf<String?>(null) }
    var busyId       by remember { mutableStateOf<Long?>(null) }

    var showCreateDialog    by remember { mutableStateOf(false) }
    var confirmDeleteTarget by remember { mutableStateOf<UserDto?>(null) }
    var tempPasswordReveal  by remember { mutableStateOf<Pair<String, String>?>(null) }

    fun refresh() {
        scope.launch {
            loading = true
            try {
                users = RetrofitClient.api.getUsers()
                loadError = null
            } catch (e: Exception) {
                loadError = e.message
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        val json = TokenDataStore.getUser(ctx).first()
        if (json != null) currentUser = Gson().fromJson(json, UserDto::class.java)
        refresh()
    }

    val isAdmin = currentUser?.role == "admin"

    Column(modifier = Modifier.fillMaxSize().background(palette.pageBg)) {
        Box(
            modifier = Modifier.fillMaxWidth().background(Green900).padding(horizontal = 8.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← Back", color = Color.White) }
                Text("Manage Staff", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
            }
        }

        if (!isAdmin) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Admin access required.", color = palette.mutedColor)
            }
            return
        }

        actionError?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp, 16.dp, 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFfee2e2)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(msg, color = Color(0xFFdc2626), fontSize = 13.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { actionError = null }, contentPadding = PaddingValues(0.dp)) { Text("Dismiss", fontSize = 12.sp) }
                }
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green500)
            }
            loadError != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $loadError", color = Color.Red)
            }
            else -> {
                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item { Spacer(Modifier.height(4.dp)) }
                        items(users, key = { it.id }) { u ->
                            UserRow(
                                user = u,
                                isSelf = u.id == currentUser?.id,
                                busy = busyId == u.id,
                                palette = palette,
                                onChangeRole = { newRole ->
                                    scope.launch {
                                        busyId = u.id
                                        try {
                                            val updated = RetrofitClient.api.updateUserRole(u.id, UpdateRoleRequest(newRole))
                                            users = users.map { if (it.id == u.id) updated else it }
                                        } catch (e: Exception) { actionError = e.message }
                                        busyId = null
                                    }
                                },
                                onToggleActive = {
                                    scope.launch {
                                        busyId = u.id
                                        try {
                                            val updated = if (u.isActive) RetrofitClient.api.deactivateUser(u.id)
                                                          else RetrofitClient.api.reactivateUser(u.id)
                                            users = users.map { if (it.id == u.id) updated else it }
                                        } catch (e: Exception) { actionError = e.message }
                                        busyId = null
                                    }
                                },
                                onResetPassword = {
                                    scope.launch {
                                        busyId = u.id
                                        try {
                                            val res = RetrofitClient.api.resetUserPassword(u.id)
                                            tempPasswordReveal = (u.fullName ?: u.email) to res.tempPassword
                                        } catch (e: Exception) { actionError = e.message }
                                        busyId = null
                                    }
                                },
                                onDelete = { confirmDeleteTarget = u },
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }

                    FloatingActionButton(
                        onClick = { showCreateDialog = true },
                        containerColor = Green500,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
                    ) {
                        Text("+", fontSize = 28.sp, fontWeight = FontWeight.Light)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateStaffDialog(
            onDismiss = { showCreateDialog = false },
            onCreated = { name, tempPassword ->
                showCreateDialog = false
                tempPasswordReveal = name to tempPassword
                refresh()
            },
            onError = { actionError = it },
        )
    }

    tempPasswordReveal?.let { (name, password) ->
        TempPasswordDialog(name = name, password = password, onDismiss = { tempPasswordReveal = null })
    }

    confirmDeleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { confirmDeleteTarget = null },
            title = { Text("Delete account?") },
            text  = { Text("${target.fullName ?: target.email} will be permanently removed and cannot be recovered.") },
            confirmButton = {
                TextButton(onClick = {
                    val id = target.id
                    confirmDeleteTarget = null
                    scope.launch {
                        try {
                            RetrofitClient.api.deleteUser(id)
                            users = users.filter { it.id != id }
                        } catch (e: Exception) { actionError = e.message }
                    }
                }) { Text("Delete", color = Red500) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteTarget = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun UserRow(
    user: UserDto,
    isSelf: Boolean,
    busy: Boolean,
    palette: ScreenPalette,
    onChangeRole: (String) -> Unit,
    onToggleActive: () -> Unit,
    onResetPassword: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBg),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(user.fullName?.ifBlank { null } ?: user.email.substringBefore("@"), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = palette.textColor)
                        if (isSelf) Text("  (you)", fontSize = 11.sp, color = Green700)
                    }
                    Text(user.email, fontSize = 12.sp, color = palette.mutedColor)
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (user.isActive) Green100 else Color(0xFFfee2e2),
                ) {
                    Text(
                        if (user.isActive) "Active" else "Inactive",
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = if (user.isActive) Green700 else Color(0xFFdc2626),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                roles.forEach { r ->
                    val selected = user.role == r
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) Green100 else palette.borderColor,
                        modifier = Modifier.weight(1f),
                    ) {
                        TextButton(
                            onClick = { if (!isSelf && !busy && !selected) onChangeRole(r) },
                            enabled = !isSelf && !busy,
                            contentPadding = PaddingValues(vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                r.replaceFirstChar { it.uppercase() },
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = if (selected) Green700 else palette.mutedColor,
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onResetPassword, enabled = !busy, contentPadding = PaddingValues(0.dp)) {
                    Text("🔑 Reset", color = Blue600, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                if (!isSelf) {
                    TextButton(onClick = onToggleActive, enabled = !busy, contentPadding = PaddingValues(0.dp)) {
                        Text(
                            if (user.isActive) "⏸ Deactivate" else "▶ Reactivate",
                            color = if (user.isActive) Amber600 else Green700,
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        )
                    }
                    TextButton(onClick = onDelete, enabled = !busy, contentPadding = PaddingValues(0.dp)) {
                        Text("🗑 Delete", color = Red500, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (busy) {
                    Spacer(Modifier.width(4.dp))
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Green500)
                }
            }
        }
    }
}

@Composable
private fun CreateStaffDialog(
    onDismiss: () -> Unit,
    onCreated: (name: String, tempPassword: String) -> Unit,
    onError: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val palette = rememberScreenPalette()
    var firstName  by remember { mutableStateOf("") }
    var lastName   by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var suffix     by remember { mutableStateOf("") }
    var email      by remember { mutableStateOf("") }
    var role       by remember { mutableStateOf("staff") }
    var creating   by remember { mutableStateOf(false) }
    var formError  by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!creating) onDismiss() },
        title = { Text("Add staff account") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("A temporary password will be generated and shown once.", fontSize = 12.sp, color = palette.mutedColor)
                formError?.let { Text(it, color = Red500, fontSize = 12.sp) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = firstName, onValueChange = { firstName = capitalizeWords(it) }, label = { Text("First name") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = lastName, onValueChange = { lastName = capitalizeWords(it) }, label = { Text("Last name") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = middleName, onValueChange = { middleName = capitalizeWords(it) }, label = { Text("Middle name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = suffix, onValueChange = { suffix = capitalizeWords(it) }, label = { Text("Suffix") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email address") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Text("Role", fontSize = 12.sp, color = palette.textColor, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    roles.forEach { r ->
                        val selected = role == r
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) Green100 else palette.borderColor,
                            modifier = Modifier.weight(1f),
                        ) {
                            TextButton(onClick = { role = r }, contentPadding = PaddingValues(vertical = 6.dp), modifier = Modifier.fillMaxWidth()) {
                                Text(r.replaceFirstChar { it.uppercase() }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Green700 else palette.mutedColor)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !creating,
                onClick = {
                    if (email.isBlank() || firstName.isBlank() || lastName.isBlank()) {
                        formError = "Email, first name, and last name are required"
                        return@TextButton
                    }
                    creating = true
                    formError = null
                    scope.launch {
                        try {
                            val res = RetrofitClient.api.createUser(
                                CreateUserRequest(email.trim(), firstName.trim(), lastName.trim(), middleName.trim(), suffix.trim(), role)
                            )
                            onCreated(res.user.fullName ?: res.user.email, res.tempPassword)
                        } catch (e: Exception) {
                            creating = false
                            onError(e.message ?: "Failed to create account")
                            onDismiss()
                        }
                    }
                },
            ) { Text(if (creating) "Creating…" else "Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !creating) { Text("Cancel") }
        },
    )
}

@Composable
private fun TempPasswordDialog(name: String, password: String, onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val palette = rememberScreenPalette()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Temporary password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("For $name. This is shown only once — copy it now and share it securely.", fontSize = 13.sp, color = palette.mutedColor)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.borderColor,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        password,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { clipboard.setText(AnnotatedString(password)) }) { Text("📋 Copy") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
    )
}
