package com.sipedas.app.ui.jurnal

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.model.JadwalDto
import com.sipedas.app.data.model.JurnalDto
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.components.ScreenHeader
import com.sipedas.app.ui.util.formatTanggalPendek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JurnalScreen(
    onIsi: (jadwalId: Int) -> Unit,
    viewModel: JurnalViewModel = viewModel(factory = JurnalViewModel.Factory),
) {
    val s = viewModel.state

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Jurnal mengajar")

        PullToRefreshBox(
            isRefreshing = viewModel.refreshing,
            onRefresh = viewModel::muatUlang,
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                s.loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                s.error != null && s.hariIni.isEmpty() && s.riwayat.isEmpty() -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(s.error, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::cobaLagi) { Text("Coba lagi") }
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (s.error != null) {
                        item {
                            Text(s.error, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    item {
                        Text("Hari ini", style = MaterialTheme.typography.titleMedium)
                    }

                    if (s.hariIni.isEmpty()) {
                        item {
                            Text(
                                "Tidak ada jadwal mengajar hari ini.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    items(s.hariIni, key = { "jadwal-${it.id}" }) { j ->
                        JadwalJurnalCard(j, onIsi = { onIsi(j.id) })
                    }

                    item(key = "kalender") {
                        Spacer(Modifier.height(8.dp))
                        KalenderJurnal()
                    }
                }
            }
        }
    }
}

@Composable
private fun JadwalJurnalCard(j: JadwalDto, onIsi: () -> Unit) {
    val rentang = if (j.jamKeMulai == j.jamKeSelesai) "${j.jamKeMulai}" else "${j.jamKeMulai}-${j.jamKeSelesai}"

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Jam ke-$rentang",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${j.jamMulai.take(5)} - ${j.jamSelesai.take(5)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(j.kelas?.nama ?: "-", style = MaterialTheme.typography.titleLarge)
        Text(
            j.mapel?.nama ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(12.dp))

        when {
            j.sudahJurnal && j.bisaIsiJurnal -> OutlinedButton(
                onClick = onIsi,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("✓ Sudah diisi · Ubah jurnal") }

            j.sudahJurnal -> Text(
                "✓ Jurnal sudah diisi",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )

            j.bisaIsiJurnal -> Button(
                onClick = onIsi,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Isi jurnal") }

            else -> Text(
                "Jurnal hanya bisa diisi pada hari jadwalnya",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RiwayatCard(j: JurnalDto) {
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
        Text(
            formatTanggalPendek(j.tanggal),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "${j.kelas?.nama ?: "-"} · ${j.jadwal?.mapel?.nama ?: "-"}",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            j.materi,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}