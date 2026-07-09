package com.daycarelog.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daycarelog.app.data.model.UserDto
import kotlinx.coroutines.delay

private val Green900Text = Color(0xFF14532d)
private val Green500 = Color(0xFF16a34a)
private val Green100 = Color(0xFFdcfce7)

private const val RESEND_COOLDOWN_SECONDS = 60

private fun maskEmail(email: String): String {
    val at = email.indexOf('@')
    if (at <= 0) return email
    val name = email.substring(0, at)
    val domain = email.substring(at)
    val visible = name.take(2)
    val stars = "*".repeat(maxOf(name.length - 2, 3))
    return "$visible$stars$domain"
}

@Composable
fun VerifyEmailScreen(
    email: String,
    onVerified: (UserDto) -> Unit,
    onSignOut: () -> Unit,
    vm: VerifyEmailViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by vm.state.collectAsState()

    var code by remember { mutableStateOf(List(6) { "" }) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var infoMsg  by remember { mutableStateOf<String?>(null) }
    var cooldown by remember { mutableStateOf(0) }

    LaunchedEffect(cooldown) {
        if (cooldown > 0) {
            delay(1000)
            cooldown -= 1
        }
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is VerifyEmailState.Verified -> { vm.resetState(); onVerified(s.user) }
            is VerifyEmailState.Error    -> { errorMsg = s.message; infoMsg = null; vm.resetState() }
            is VerifyEmailState.Resent   -> { infoMsg = "A new code and link are on the way."; errorMsg = null; cooldown = RESEND_COOLDOWN_SECONDS; vm.resetState() }
            is VerifyEmailState.NotYetVerified -> { infoMsg = "Still not verified yet - check your email."; errorMsg = null; vm.resetState() }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF052e16), Green500)))
            .imePadding(),
    ) {
        Box(modifier = Modifier.weight(0.6f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.18f), modifier = Modifier.size(64.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text("✉️", fontSize = 28.sp) }
                }
                Spacer(Modifier.height(16.dp))
                Text("Check your email", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "We sent a code and link to ${maskEmail(email)}",
                    color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, start = 24.dp, end = 24.dp),
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White,
            shadowElevation = 24.dp,
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 28.dp).padding(top = 28.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Enter verification code", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Green900Text)
                Spacer(Modifier.height(14.dp))

                CodeInputBoxes(
                    values = code,
                    onValuesChange = { code = it },
                    onComplete = { full ->
                        errorMsg = null; infoMsg = null
                        vm.verifyByCode(context, email, full)
                    },
                )

                if (!errorMsg.isNullOrBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Surface(color = Color(0xFFfee2e2), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(errorMsg!!, color = Color(0xFF991b1b), fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                    }
                }
                if (!infoMsg.isNullOrBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Surface(color = Green100, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(infoMsg!!, color = Color(0xFF166534), fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                    }
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        val full = code.joinToString("")
                        if (full.length != 6) { errorMsg = "Enter all 6 digits."; return@Button }
                        errorMsg = null; infoMsg = null
                        vm.verifyByCode(context, email, full)
                    },
                    enabled = state !is VerifyEmailState.Loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green500),
                ) {
                    if (state is VerifyEmailState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Verify email", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = { errorMsg = null; infoMsg = null; vm.resend(email) },
                    enabled = cooldown <= 0 && state !is VerifyEmailState.Loading,
                ) {
                    Text(
                        if (cooldown > 0) "Resend in ${cooldown}s" else "Resend code",
                        color = if (cooldown > 0) Color.Gray else Green500,
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    )
                }

                OutlinedButton(
                    onClick = { errorMsg = null; infoMsg = null; vm.checkStatus(context) },
                    enabled = state !is VerifyEmailState.Loading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("I verified in my browser", fontSize = 13.sp)
                }

                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onSignOut) {
                    Text("Not you? Sign out", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CodeInputBoxes(
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    onComplete: (String) -> Unit,
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val focusManager = LocalFocusManager.current

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 0 until 6) {
            OutlinedTextField(
                value = values[i],
                onValueChange = { raw ->
                    val digit = raw.filter { it.isDigit() }.take(1)
                    val updated = values.toMutableList().also { it[i] = digit }
                    onValuesChange(updated)
                    if (digit.isNotEmpty()) {
                        if (i < 5) {
                            focusRequesters[i + 1].requestFocus()
                        } else {
                            focusManager.clearFocus()
                            val full = updated.joinToString("")
                            if (full.length == 6) onComplete(full)
                        }
                    }
                },
                modifier = Modifier
                    .width(44.dp)
                    .focusRequester(focusRequesters[i]),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Green500),
            )
        }
    }

    LaunchedEffect(Unit) { focusRequesters[0].requestFocus() }
}
