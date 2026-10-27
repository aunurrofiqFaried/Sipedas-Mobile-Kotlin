package com.sipedas.app.ui.sholat

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.model.KelasSholatDto
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.components.ScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SholatKelasScreen(
    onBack: () -> Unit,
    onPilih: (kelasId: Int, waktu: String) -> Unit,
    viewModel: SholatKelasViewModel = viewModel(factory = SholatKelasViewModel.Factory),
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Absensi sholat", onBack = onBack)

        PullToRefreshBox(
            isRefreshing = viewModel.refreshing,
            onRefresh = viewModel::muatUlang,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (val state = viewModel.state) {
                SholatKelasUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is SholatKelasUiState.Error -> Column(
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

                is SholatKelasUiState.Success -> {
                    val kelas = state.data.data

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        if (kelas.isEmpty()) {
                            item {
                                Text(
                                    "Anda tidak menjadi wali kelas pada tahun ajaran aktif, " +
                                            "jadi tidak ada kelas untuk diabsen sholatnya.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        items(kelas, key = { it.id }) { KelasSholatCard(it, onPilih) }
                    }
                }
            }
        }
    }
}

@Composable
private fun KelasSholatCard(k: KelasSholatDto, onPilih: (Int, String) -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(k.nama, style = MaterialTheme.typography.titleLarge)
        Text(
            "${k.jumlahPeserta} siswa muslim",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (!k.absensiHarianSiap) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Isi absensi harian kelas ini dulu, baru absensi sholat bisa dibuka.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TombolWaktu("Dhuhur", k.dhuhur, k.absensiHarianSiap, Modifier.weight(1f)) {
                onPilih(k.id, "dhuhur")
            }
            TombolWaktu("Ashar", k.ashar, k.absensiHarianSiap, Modifier.weight(1f)) {
                onPilih(k.id, "ashar")
            }
        }
    }
}

// Sudah tersimpan = tombol berbingkai dengan centang (masih bisa dibuka untuk diubah)
@Composable
private fun TombolWaktu(
    label: String,
    tersimpan: Int,
    aktif: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    if (tersimpan > 0) {
        OutlinedButton(onClick = onClick, enabled = aktif, modifier = modifier) { Text("✓ $label") }
    } else {
        Button(onClick = onClick, enabled = aktif, modifier = modifier) { Text(label) }
    }
}