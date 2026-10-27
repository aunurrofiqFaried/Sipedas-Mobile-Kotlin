package com.sipedas.app.ui.absensi

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.model.KelasAbsenDto
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.components.ScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelasAbsensiScreen(
    onPilihKelas: (id: Int, nama: String) -> Unit,
    viewModel: KelasAbsensiViewModel = viewModel(factory = KelasAbsensiViewModel.Factory),
    tampilSholat: Boolean,
    onBukaSholat: () -> Unit,
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

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        if (tampilSholat) {
                            item {
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
                        if (kelas.isEmpty()) {
                            item {
                                Text(
                                    "Tidak ada kelas yang bisa Anda absen hari ini. " +
                                            "Absensi hanya untuk wali kelas dan guru yang mengajar di jam ke-1 atau ke-2.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        items(kelas, key = { it.id }) { k ->
                            KelasCard(k, onClick = { onPilihKelas(k.id, k.nama) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KelasCard(k: KelasAbsenDto, onClick: () -> Unit) {
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