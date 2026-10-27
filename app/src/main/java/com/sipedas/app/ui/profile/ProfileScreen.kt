package com.sipedas.app.ui.profile

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sipedas.app.data.local.Session
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.components.ScreenHeader
import com.sipedas.app.ui.theme.Green600
import com.sipedas.app.ui.theme.Orange500
import com.sipedas.app.ui.kalender.KalenderMengajar

@Composable
fun ProfileScreen(session: Session, onLogout: () -> Unit, onUbahPassword: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Profil")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))

            // Avatar: huruf depan nama dengan gradasi oranye ke hijau
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Orange500, Green600))),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = inisial(session.name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                session.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "@${session.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Lencana(
                    teks = labelRole(session.role),
                    latar = MaterialTheme.colorScheme.primaryContainer,
                    isi = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (session.isWaliKelas) {
                    Lencana(
                        teks = "Wali kelas",
                        latar = MaterialTheme.colorScheme.secondaryContainer,
                        isi = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            KalenderMengajar(token = session.token)

            Spacer(Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Keamanan akun", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Ganti password awal Anda secara berkala agar akun tetap aman.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onUbahPassword,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Ubah password") }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onLogout,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) { Text("Keluar", style = MaterialTheme.typography.labelLarge) }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Lencana(teks: String, latar: Color, isi: Color) {
    Text(
        text = teks,
        style = MaterialTheme.typography.labelLarge,
        color = isi,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(latar)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

// "Budi Santoso, S.Kom" -> "BS"
private fun inisial(nama: String): String =
    nama.split(" ", ",")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

private fun labelRole(role: String): String = when (role) {
    "admin" -> "Admin"
    "guru" -> "Guru"
    "guru_bk" -> "Guru BK"
    "kesiswaan" -> "Waka Kesiswaan"
    "kepala_sekolah" -> "Kepala Sekolah"
    else -> role
}