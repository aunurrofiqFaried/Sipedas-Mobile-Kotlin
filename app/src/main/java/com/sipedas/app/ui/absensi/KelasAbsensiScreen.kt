package com.sipedas.app.ui.absensi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.model.KelasAbsenDto
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.components.ScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelasAbsensiScreen(
    tampilSholat: Boolean,
    onBukaSholat: () -> Unit,
    onBukaBantu: () -> Unit,
    onBukaRekap: () -> Unit,
    onPilihKelas: (id: Int, nama: String) -> Unit,
    viewModel: KelasAbsensiViewModel = viewModel(factory = KelasAbsensiViewModel.Factory),
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Absensi hari ini")

        PullToRefreshBox(
            isRefreshing = viewModel.refreshing,
            onRefresh = viewModel::muatUlang,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (val state = viewModel.state) {
                KelasUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is KelasUiState.Error -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::cobaLagi) { Text("Coba lagi") }
                }

                is KelasUiState.Success -> {
                    val kelas = state.data.data

                    // Wajib: kelas wali dan kelas jam ke-1/2. Sisanya masuk ke kartu "Bantu".
                    val wajib = kelas.filter { it.alasan == "wali" || it.alasan == "jadwal" }
                    val lainnya = kelas.filter { it.alasan != "wali" && it.alasan != "jadwal" }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        if (tampilSholat) {
                            item(key = "sholat") {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .clickable(onClick = onBukaSholat),
                                ) {
                                    Text("Absensi sholat", style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        "Dhuhur dan Ashar · khusus wali kelas",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }

                        if (wajib.isNotEmpty()) {
                            item(key = "judul-wajib") {
                                Text("Wajib Anda absen", style = MaterialTheme.typography.titleMedium)
                            }

                            items(wajib, key = { "wajib-${it.id}" }) { k ->
                                KelasCard(k, onClick = { onPilihKelas(k.id, k.nama) })
                            }
                        } else {
                            item(key = "kosong-wajib") {
                                Text(
                                    text = if (lainnya.isEmpty()) {
                                        "Tidak ada kelas yang perlu Anda absen sekarang. " +
                                                "Kelas yang belum diabsen akan terbuka untuk semua guru setelah jam ke-2 dimulai."
                                    } else {
                                        "Tidak ada kelas wajib untuk Anda hari ini."
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        if (lainnya.isNotEmpty()) {
                            item(key = "bantu") {
                                BantuCard(lainnya, onClick = onBukaBantu)
                            }
                        }
                        item(key = "rekap") {
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable(onClick = onBukaRekap),
                            ) {
                                Text("Rekap kehadiran kelas", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "Pilih kelas untuk melihat jumlah sakit, izin, dan alpa tiap siswa",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Satu kartu untuk semua kelas di luar kewajiban sendiri
@Composable
private fun BantuCard(lainnya: List<KelasAbsenDto>, onClick: () -> Unit) {
    val belum = lainnya.count { it.sudahDiabsen == 0 }
    val admin = lainnya.any { it.alasan == "admin" }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (admin) "Semua kelas" else "Bantu mengabsen kelas lain",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (admin) {
                        "${lainnya.size} kelas · akses admin"
                    } else {
                        "$belum kelas belum diabsen · terbuka untuk semua guru"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!admin && belum > 0) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "$belum",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

// Dipakai juga oleh KelasBantuScreen, jadi tidak private
@Composable
internal fun KelasCard(k: KelasAbsenDto, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(k.nama, style = MaterialTheme.typography.titleLarge)
                Text(
                    "${k.jumlahSiswa} siswa",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                val keterangan = when (k.alasan) {
                    "wali" -> "Kelas Anda (wali kelas)"
                    "jadwal" -> "Anda mengajar di jam ke-1 atau ke-2"
                    "pengganti" -> "Belum diabsen · bantu mengabsen"
                    "admin" -> "Akses admin"
                    else -> null
                }

                if (keterangan != null) {
                    Text(
                        keterangan,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (k.alasan == "pengganti") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            val (teks, warna) = when {
                k.sudahDiabsen == 0 -> "Belum diabsen" to MaterialTheme.colorScheme.primary
                k.sudahDiabsen < k.jumlahSiswa -> "Sebagian (${k.sudahDiabsen}/${k.jumlahSiswa})" to MaterialTheme.colorScheme.primary
                else -> "✓ Sudah diabsen" to MaterialTheme.colorScheme.secondary
            }

            Text(teks, style = MaterialTheme.typography.labelLarge, color = warna)
        }
    }
}