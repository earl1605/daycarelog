package com.daycarelog.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val roles = listOf(
    Triple("admin",   "🛡️", "Admin"),
    Triple("teacher", "📚", "Teacher"),
    Triple("staff",   "👤", "Staff"),
)

private val suffixes = listOf("", "Jr.", "Sr.", "II", "III", "IV", "V")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by authViewModel.state.collectAsState()

    var firstName  by remember { mutableStateOf("") }
    var lastName   by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var suffix     by remember { mutableStateOf("") }
    var email      by remember { mutableStateOf("") }
    var password   by remember { mutableStateOf("") }
    var confirm    by remember { mutableStateOf("") }
    var role       by remember { mutableStateOf("staff") }
    var showPass   by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var suffixExpanded by remember { mutableStateOf(false) }
    var errorMsg   by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state) {
        when (state) {
            is AuthState.Registered -> { authViewModel.resetState(); onRegisterSuccess() }
            is AuthState.Error      -> { errorMsg = (state as AuthState.Error).message; authViewModel.resetState() }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF14532d), Color(0xFF16a34a))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text("DaycareLog", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("Create your account", fontSize = 13.sp, color = Color(0xFFbbf7d0), modifier = Modifier.padding(bottom = 24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    errorMsg?.let {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFfee2e2)), shape = RoundedCornerShape(12.dp)) {
                            Text(it, color = Color(0xFFdc2626), fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                        }
                    }

                    // Name row 1
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = firstName, onValueChange = { firstName = it },
                            label = { Text("First Name *", fontSize = 12.sp) },
                            singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        )
                        OutlinedTextField(
                            value = lastName, onValueChange = { lastName = it },
                            label = { Text("Last Name *", fontSize = 12.sp) },
                            singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        )
                    }

                    // Name row 2
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = middleName, onValueChange = { middleName = it },
                            label = { Text("Middle Name", fontSize = 12.sp) },
                            singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        )
                        ExposedDropdownMenuBox(
                            expanded = suffixExpanded,
                            onExpandedChange = { suffixExpanded = it },
                            modifier = Modifier.weight(1f),
                        ) {
                            OutlinedTextField(
                                value = suffix.ifEmpty { "None" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Suffix", fontSize = 12.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(suffixExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            )
                            ExposedDropdownMenu(expanded = suffixExpanded, onDismissRequest = { suffixExpanded = false }) {
                                suffixes.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s.ifEmpty { "— None —" }) },
                                        onClick = { suffix = s; suffixExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    // Role picker
                    Text("Role *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        roles.forEach { (value, icon, label) ->
                            val selected = role == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) Color(0xFFdcfce7) else Color(0xFFf9fafb))
                                    .border(2.dp, if (selected) Color(0xFF16a34a) else Color(0xFFe5e7eb), RoundedCornerShape(12.dp))
                                    .clickable { role = value }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(icon, fontSize = 18.sp)
                                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                        color = if (selected) Color(0xFF15803d) else Color(0xFF6b7280))
                                }
                            }
                        }
                    }

                    // Email
                    OutlinedTextField(
                        value = email, onValueChange = { email = it; errorMsg = null },
                        label = { Text("Email address *") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    )

                    // Password
                    OutlinedTextField(
                        value = password, onValueChange = { password = it; errorMsg = null },
                        label = { Text("Password *") }, singleLine = true,
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = { TextButton(onClick = { showPass = !showPass }) { Text(if (showPass) "Hide" else "Show", fontSize = 12.sp) } },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    )

                    // Confirm password
                    OutlinedTextField(
                        value = confirm, onValueChange = { confirm = it; errorMsg = null },
                        label = { Text("Confirm Password *") }, singleLine = true,
                        visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = { TextButton(onClick = { showConfirm = !showConfirm }) { Text(if (showConfirm) "Hide" else "Show", fontSize = 12.sp) } },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    )

                    Button(
                        onClick = {
                            when {
                                firstName.isBlank() || lastName.isBlank() -> errorMsg = "First and last name are required"
                                email.isBlank()     -> errorMsg = "Email is required"
                                password.length < 6 -> errorMsg = "Password must be at least 6 characters"
                                password != confirm -> errorMsg = "Passwords do not match"
                                else -> authViewModel.register(context, email.trim(), password, firstName.trim(), lastName.trim(), middleName.trim(), suffix, role)
                            }
                        },
                        enabled = state !is AuthState.Loading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (state is AuthState.Loading)
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else
                            Text("Create Account", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("Already have an account? ", fontSize = 13.sp, color = Color(0xFF6b7280))
                        TextButton(onClick = onNavigateToLogin, contentPadding = PaddingValues(0.dp)) {
                            Text("Sign in", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
