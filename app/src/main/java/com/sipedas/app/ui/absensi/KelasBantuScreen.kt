package com.sipedas.app.ui.absensi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.ui.components.ScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelasBantuScreen(
    onBack: () -> Unit,
    onPilihKelas: (id: Int, nama: String) -> Unit,
    viewModel: KelasAbsensiViewModel = viewModel(
        key = "kelas-bantu",
        factory = KelasAbsensiViewModel.Factory,
    ),
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Bantu mengabsen", onBack = onBack)

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
                    val lainnya = state.data.data.filter { it.alasan != "wali" && it.alasan != "jadwal" }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        item(key = "pengantar") {
                            Text(
                                "Kelas di bawah ini belum diabsen dan terbuka untuk semua guru " +
                                        "karena jam ke-2 sudah dimulai. Bantu absen kalau wali kelas " +
                                        "atau guru jam ke-1 belum sempat.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        if (lainnya.isEmpty()) {
                            item(key = "kosong") {
                                Text("Semua kelas sudah diabsen. Terima kasih!")
                            }
                        }

                        items(lainnya, key = { "bantu-${it.id}" }) { k ->
                            KelasCard(k, onClick = { onPilihKelas(k.id, k.nama) })
                        }
                    }
                }
            }
        }
    }
}