package com.daycarelog.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daycarelog.app.util.capitalizeWords
import com.daycarelog.app.util.capitalizedNameFieldValue

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

    var firstName  by remember { mutableStateOf(TextFieldValue("")) }
    var lastName   by remember { mutableStateOf(TextFieldValue("")) }
    var middleName by remember { mutableStateOf(TextFieldValue("")) }
    var suffix     by remember { mutableStateOf("") }
    var email      by remember { mutableStateOf("") }
    var password   by remember { mutableStateOf("") }
    var confirm    by remember { mutableStateOf("") }
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
                            value = firstName, onValueChange = { firstName = capitalizedNameFieldValue(it) },
                            label = { Text("First Name *", fontSize = 12.sp) },
                            singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        )
                        OutlinedTextField(
                            value = lastName, onValueChange = { lastName = capitalizedNameFieldValue(it) },
                            label = { Text("Last Name *", fontSize = 12.sp) },
                            singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        )
                    }

                    // Name row 2
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = middleName, onValueChange = { middleName = capitalizedNameFieldValue(it) },
                            label = { Text("Middle Name", fontSize = 12.sp) },
                            singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
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
                                firstName.text.isBlank() || lastName.text.isBlank() -> errorMsg = "First and last name are required"
                                email.isBlank()     -> errorMsg = "Email is required"
                                password.length < 6 -> errorMsg = "Password must be at least 6 characters"
                                password != confirm -> errorMsg = "Passwords do not match"
                                else -> authViewModel.register(
                                    context, email.trim(), password,
                                    capitalizeWords(firstName.text.trim()),
                                    capitalizeWords(lastName.text.trim()),
                                    capitalizeWords(middleName.text.trim()),
                                    suffix,
                                )
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
