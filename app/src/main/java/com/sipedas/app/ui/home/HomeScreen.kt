package com.sipedas.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sipedas.app.data.local.Session

// Sementara: nanti diganti daftar jadwal hari ini
@Composable
fun HomeScreen(session: Session, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Halo, ${session.name}", style = MaterialTheme.typography.headlineSmall)
        Text("Role: ${session.role}")
        if (session.isWaliKelas) {
            Text("Anda wali kelas")
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onLogout) { Text("Keluar") }
    }
}