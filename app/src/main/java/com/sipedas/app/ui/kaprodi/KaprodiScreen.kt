package com.sipedas.app.ui.kaprodi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sipedas.app.data.local.Session
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.pengumuman.LoncengPengumuman

@Composable
fun KaprodiScreen(
    session: Session,
    belumDibaca: Int,
    onBukaPengumuman: () -> Unit,
    onBukaKepatuhan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth().padding(end = 8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Kepala program keahlian",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        session.kaprodiNama ?: "Jurusan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Data di sini hanya mencakup kelas dan guru di jurusan ${session.kaprodiKode ?: ""}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                LoncengPengumuman(belumDibaca = belumDibaca, onClick = onBukaPengumuman)
            }
        }

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp)
                .clickable(onClick = onBukaKepatuhan),
        ) {
            Text(
                "Kepatuhan jurnal guru",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Pantau jurnal mengajar guru di jurusan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}