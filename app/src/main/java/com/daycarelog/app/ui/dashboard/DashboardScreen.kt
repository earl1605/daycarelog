package com.daycarelog.app.ui.dashboard

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daycarelog.app.data.api.RetrofitClient
import com.daycarelog.app.data.model.UserDto
import com.daycarelog.app.data.preferences.TokenDataStore
import com.google.gson.Gson

private val Green900 = Color(0xFF052e16)
private val Green700 = Color(0xFF15803d)
private val Green500 = Color(0xFF16a34a)
private val Green100 = Color(0xFFdcfce7)

@Composable
fun DashboardScreen(onNavigateToSettings: () -> Unit) {
    val ctx = LocalContext.current
    val userJson by TokenDataStore.getUser(ctx).collectAsState(initial = null)
    val user = remember(userJson) { userJson?.let { Gson().fromJson(it, UserDto::class.java) } }
    var totalChildren    by remember { mutableStateOf<Int?>(null) }
    var activeChildren   by remember { mutableStateOf<Int?>(null) }
    var presentToday     by remember { mutableStateOf<Int?>(null) }
    var loading          by remember { mutableStateOf(true) }

    val today = java.time.LocalDate.now().toString()

    val photoBitmap = remember(user?.profilePhoto) {
        user?.profilePhoto?.let {
            try {
                val b64 = it.substringAfter("base64,")
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) { null }
        }
    }

    LaunchedEffect(Unit) {
        try {
            val children = RetrofitClient.api.getChildren()
            totalChildren  = children.size
            activeChildren = children.count { it.enrollmentStatus == "active" }
            val attendance = RetrofitClient.api.getAttendance(today)
            presentToday   = attendance.count { it.status == "present" }
        } catch (_: Exception) { }
        loading = false
    }

    val displayName = listOfNotNull(user?.firstName, user?.lastName)
        .joinToString(" ")
        .ifEmpty { user?.email ?: "User" }
    val initial = (user?.firstName?.firstOrNull() ?: user?.email?.firstOrNull() ?: 'U').uppercaseChar()
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11  -> "Good morning"
        in 12..16 -> "Good afternoon"
        else      -> "Good evening"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFf0fdf4))
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Header ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Green900, Green500)))
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text("$greeting,", color = Color(0xFFbbf7d0), fontSize = 13.sp)
                    Text(displayName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        user?.role?.replaceFirstChar { it.uppercase() } ?: "",
                        color = Color(0xFF86efac),
                        fontSize = 12.sp,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Settings icon
                    IconButton(onClick = onNavigateToSettings) {
                        Text("⚙️", fontSize = 22.sp)
                    }
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF052e16)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (photoBitmap != null) {
                            Image(
                                bitmap = photoBitmap.asImageBitmap(),
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Text(initial.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Stats row ───────────────────────────────────────────────
            Text("Today's Overview", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF374151))

            if (loading) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green500)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        emoji = "👦",
                        label = "Enrolled",
                        value = activeChildren?.toString() ?: "—",
                        accent = Green500,
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        emoji = "✅",
                        label = "Present Today",
                        value = presentToday?.toString() ?: "—",
                        accent = Color(0xFF2563eb),
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        emoji = "📋",
                        label = "Total",
                        value = totalChildren?.toString() ?: "—",
                        accent = Color(0xFF7c3aed),
                    )
                }
            }

            // ── Date badge ──────────────────────────────────────────────
            Surface(
                color = Green100,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("📅  Today", color = Green700, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        java.time.LocalDate.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d yyyy")
                        ),
                        color = Green700,
                        fontSize = 12.sp,
                    )
                }
            }

            // ── Quick tips ──────────────────────────────────────────────
            Text("Quick Tips", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF374151))
            listOf(
                "👶  Use the Children tab to manage enrolled children.",
                "📋  Take attendance daily from the Attendance tab.",
                "❤️  Log weight and height in the Health tab.",
                "📊  View monthly summaries in the Reports tab.",
            ).forEach { tip ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        tip,
                        fontSize = 13.sp,
                        color = Color(0xFF374151),
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    value: String,
    accent: Color,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = accent)
            Text(label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
