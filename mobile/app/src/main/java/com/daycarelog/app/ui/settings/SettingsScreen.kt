package com.daycarelog.app.ui.settings

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycarelog.app.data.api.RetrofitClient
import com.daycarelog.app.data.api.TokenProvider
import com.daycarelog.app.data.model.UpdateProfileRequest
import com.daycarelog.app.data.model.UserDto
import com.daycarelog.app.data.preferences.ThemeState
import com.daycarelog.app.data.preferences.TokenDataStore
import com.daycarelog.app.util.capitalizeWords
import com.daycarelog.app.util.capitalizedNameFieldValue
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

private val Green500 = Color(0xFF16a34a)
private val Green100 = Color(0xFFdcfce7)
private val Green900 = Color(0xFF052e16)
private val Green700 = Color(0xFF15803d)

@Suppress("DEPRECATION")
private fun uriToBase64(ctx: android.content.Context, uri: android.net.Uri): String {
    val src = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(ctx.contentResolver, uri)) { dec, _, _ ->
            dec.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        MediaStore.Images.Media.getBitmap(ctx.contentResolver, uri)
    }
    val min = minOf(src.width, src.height)
    val cropped = Bitmap.createBitmap(src, (src.width - min) / 2, (src.height - min) / 2, min, min)
    val scaled  = Bitmap.createScaledBitmap(cropped, 256, 256, true)
    val out = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
    val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    return "data:image/jpeg;base64,$b64"
}

private fun decodeBase64Bitmap(dataUrl: String): Bitmap? = try {
    val b64 = dataUrl.substringAfter("base64,")
    val bytes = Base64.decode(b64, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
} catch (_: Exception) { null }

@Composable
fun SettingsScreen(onSignOut: () -> Unit, onBack: () -> Unit, onManageStaff: () -> Unit) {
    val ctx   = LocalContext.current
    val scope = rememberCoroutineScope()
    var user  by remember { mutableStateOf<UserDto?>(null) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var photoUploading    by remember { mutableStateOf(false) }
    var photoError        by remember { mutableStateOf<String?>(null) }

    var editFirstName  by remember { mutableStateOf<TextFieldValue?>(null) }
    var editLastName   by remember { mutableStateOf<TextFieldValue?>(null) }
    var editMiddleName by remember { mutableStateOf<TextFieldValue?>(null) }
    var nameSaving      by remember { mutableStateOf(false) }
    var nameSaveError   by remember { mutableStateOf<String?>(null) }
    var nameSaved        by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val json = TokenDataStore.getUser(ctx).first()
        if (json != null) {
            val loaded = Gson().fromJson(json, UserDto::class.java)
            user = loaded
            editFirstName  = TextFieldValue(loaded.firstName ?: "")
            editLastName   = TextFieldValue(loaded.lastName ?: "")
            editMiddleName = TextFieldValue(loaded.middleName ?: "")
        }
    }

    fun saveName() {
        val u = user ?: return
        scope.launch {
            nameSaving = true
            nameSaveError = null
            nameSaved = false
            try {
                val updated = RetrofitClient.api.updateProfile(
                    u.id,
                    UpdateProfileRequest(
                        firstName  = capitalizeWords((editFirstName?.text ?: "").trim()),
                        lastName   = capitalizeWords((editLastName?.text ?: "").trim()),
                        middleName = capitalizeWords((editMiddleName?.text ?: "").trim()),
                    ),
                )
                TokenDataStore.saveUser(ctx, Gson().toJson(updated))
                user = updated
                editFirstName  = TextFieldValue(updated.firstName ?: "")
                editLastName   = TextFieldValue(updated.lastName ?: "")
                editMiddleName = TextFieldValue(updated.middleName ?: "")
                nameSaved = true
            } catch (e: Exception) {
                nameSaveError = "Save failed: ${e.message}"
            }
            nameSaving = false
        }
    }

    val photoBitmap = remember(user?.profilePhoto) {
        user?.profilePhoto?.let { decodeBase64Bitmap(it) }
    }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            photoUploading = true
            photoError = null
            try {
                val base64  = uriToBase64(ctx, uri)
                val updated = RetrofitClient.api.updateProfile(user!!.id, UpdateProfileRequest(base64))
                TokenDataStore.saveUser(ctx, Gson().toJson(updated))
                user = updated
            } catch (e: Exception) {
                photoError = "Upload failed: ${e.message}"
            }
            photoUploading = false
        }
    }

    val displayName = listOfNotNull(
        user?.firstName,
        user?.middleName?.firstOrNull()?.toString()?.let { "$it." },
        user?.lastName,
        user?.suffix?.ifBlank { null },
    ).joinToString(" ").ifEmpty { user?.email ?: "User" }

    val initial = (user?.firstName?.firstOrNull() ?: user?.email?.firstOrNull() ?: 'U').uppercaseChar()

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text  = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        scope.launch {
                            TokenDataStore.clear(ctx)
                            TokenProvider.token = null
                            onSignOut()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFef4444)),
                ) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Cancel") }
            },
        )
    }

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
                Text("Settings", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Profile card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp),
            ) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Avatar with camera badge
                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.BottomEnd,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Green100)
                                .clickable(enabled = !photoUploading) {
                                    photoLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (photoBitmap != null) {
                                Image(
                                    bitmap = photoBitmap.asImageBitmap(),
                                    contentDescription = "Profile photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Text(
                                    initial.toString(),
                                    color = Green700,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 36.sp,
                                )
                            }
                            if (photoUploading) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.45f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp,
                                    )
                                }
                            }
                        }
                        // Camera badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Green500, CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("📷", fontSize = 13.sp)
                        }
                    }

                    if (!photoError.isNullOrBlank()) {
                        Text(
                            photoError!!,
                            color = Color(0xFFef4444),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    } else {
                        Text(
                            "Tap to change photo",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(displayName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF111827))
                    Text(user?.email ?: "", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                    Surface(
                        color = Green100,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Text(
                            user?.role?.replaceFirstChar { it.uppercase() } ?: "",
                            color = Green700,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            // Account details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Account Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Green900)
                    SettingRow("Email", user?.email ?: "—")
                    SettingRow("Role",  user?.role?.replaceFirstChar { it.uppercase() } ?: "—")

                    Spacer(Modifier.height(2.dp))

                    if (nameSaveError != null) {
                        Surface(color = Color(0xFFfee2e2), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Text(nameSaveError!!, color = Color(0xFF991b1b), fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                        }
                    }
                    if (nameSaved) {
                        Surface(color = Green100, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Text("✓  Profile updated", color = Green700, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                        }
                    }

                    OutlinedTextField(
                        value = editFirstName ?: TextFieldValue(""),
                        onValueChange = { editFirstName = capitalizedNameFieldValue(it); nameSaved = false },
                        label = { Text("First Name", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    OutlinedTextField(
                        value = editLastName ?: TextFieldValue(""),
                        onValueChange = { editLastName = capitalizedNameFieldValue(it); nameSaved = false },
                        label = { Text("Last Name", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    OutlinedTextField(
                        value = editMiddleName ?: TextFieldValue(""),
                        onValueChange = { editMiddleName = capitalizedNameFieldValue(it); nameSaved = false },
                        label = { Text("Middle Name", fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )

                    Button(
                        onClick = { saveName() },
                        enabled = !nameSaving && !(editFirstName?.text ?: "").isBlank() && !(editLastName?.text ?: "").isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Green500),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (nameSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Save Changes", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            if (user?.role == "admin") {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onManageStaff),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("👥", fontSize = 18.sp, modifier = Modifier.padding(end = 10.dp))
                        Text("Manage Staff", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Green900, modifier = Modifier.weight(1f))
                        Text("›", fontSize = 18.sp, color = Color.Gray)
                    }
                }
            }

            // Appearance
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(if (ThemeState.isDarkMode) "🌙" else "☀️", fontSize = 18.sp, modifier = Modifier.padding(end = 10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Dark Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Green900)
                        Text(
                            if (ThemeState.isDarkMode) "On" else "Off",
                            fontSize = 12.sp, color = Color.Gray,
                        )
                    }
                    Switch(
                        checked = ThemeState.isDarkMode,
                        onCheckedChange = { scope.launch { ThemeState.toggle(ctx) } },
                        colors = SwitchDefaults.colors(checkedTrackColor = Green500),
                    )
                }
            }

            // About
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("About", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Green900)
                    Text("DaycareLog Mobile",                fontSize = 13.sp, color = Color(0xFF374151))
                    Text("Version 1.0.0",                    fontSize = 12.sp, color = Color.Gray)
                    Text("Barangay Childcare Management System", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showSignOutDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFef4444)),
            ) {
                Text("Sign Out", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
    }
}
