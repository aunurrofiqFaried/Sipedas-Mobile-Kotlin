package com.sipedas.app.ui.poin

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.model.PelanggaranDto
import com.sipedas.app.data.model.RiwayatPelanggaranResponse
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.components.ScreenHeader
import com.sipedas.app.ui.util.formatTanggalPendek
import androidx.compose.material3.OutlinedButton

@Composable
fun PoinSiswaScreen(
    siswaId: Int,
    onBack: () -> Unit,
    onCatat: () -> Unit,
    bolehHubungiWali: Boolean,
    onHubungiWali: () -> Unit,
    viewModel: PoinSiswaViewModel = viewModel(
        key = "poin-siswa-$siswaId",
        factory = PoinSiswaViewModel.factory(siswaId),
    ),
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = viewModel.data?.siswa?.nama ?: "Poin siswa", onBack = onBack)

        val data = viewModel.data

        when {
            viewModel.loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            data == null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(viewModel.error ?: "Data tidak tersedia.", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = viewModel::muat) { Text("Coba lagi") }
            }

            else -> Konten(data, onCatat, bolehHubungiWali, onHubungiWali)
        }
    }
}

@Composable
private fun Konten(data: RiwayatPelanggaranResponse, onCatat: () -> Unit, bolehHubungiWali: Boolean, onHubungiWali: () -> Unit,) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "NIS ${data.siswa.nis}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${data.totalPoin}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        "poin pelanggaran · tahun ajaran ${data.tahunAjaran}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (data.sp.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        data.sp.forEach { sp ->
                            Text(
                                "${sp.aturan?.nama ?: "SP"} · ${labelStatusSp(sp.status)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (sp.status == "ditolak") {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                            )
                        }
                    }
                }
            }

            item {
                Text("Riwayat pelanggaran", style = MaterialTheme.typography.titleMedium)
            }

            if (data.riwayat.isEmpty()) {
                item {
                    Text(
                        "Belum ada pelanggaran tercatat tahun ini.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(data.riwayat, key = { it.id }) { RiwayatItem(it) }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (bolehHubungiWali) {
                OutlinedButton(
                    onClick = onHubungiWali,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) { Text("Hubungi wali", style = MaterialTheme.typography.labelLarge) }
            }

            Button(
                onClick = onCatat,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) { Text("Catat pelanggaran", style = MaterialTheme.typography.labelLarge) }
        }
    }
}

@Composable
private fun RiwayatItem(p: PelanggaranDto) {
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(p.jenis?.nama ?: "-", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${formatTanggalPendek(p.tanggal)} · ${p.pencatat?.name ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!p.keterangan.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(p.keterangan, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text(
                "+${p.poin}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}