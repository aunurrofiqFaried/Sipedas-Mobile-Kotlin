package com.sipedas.app.ui.pimpinan

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.model.DetailKepatuhan
import com.sipedas.app.data.model.SesiTerlewat
import com.sipedas.app.ui.components.ScreenHeader

@Composable
fun DetailKepatuhanScreen(
    guruId: Int,
    dari: String?,
    sampai: String?,
    onBack: () -> Unit,
    viewModel: DetailKepatuhanViewModel = viewModel(
        key = "kepatuhan-$guruId-$dari-$sampai",
        factory = DetailKepatuhanViewModel.factory(guruId, dari, sampai),
    ),
) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Detail Kepatuhan", onBack = onBack)

        PullToRefreshBox(
            isRefreshing = viewModel.refreshing,
            onRefresh = viewModel::muatUlang,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (val s = viewModel.state) {
                DetailKepatuhanState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is DetailKepatuhanState.Error -> Column(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(s.message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                    Button(
                        onClick = viewModel::cobaLagi,
                        modifier = Modifier.padding(top = 16.dp),
                    ) { Text("Coba lagi") }
                }

                // Sesi berakhir ditangani terpusat oleh ApiClient.onUnauthorized
                DetailKepatuhanState.Unauthorized -> Unit

                is DetailKepatuhanState.Success -> IsiDetail(s.data)
            }
        }
    }
}

@Composable
private fun IsiDetail(data: DetailKepatuhan) {
    val r = data.ringkasan
    val perTanggal = data.terlewat.groupBy { it.tanggal }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column {
                Text(data.guru.nama, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                if (!data.guru.nip.isNullOrBlank()) {
                    Text("NIP ${data.guru.nip}", fontSize = 12.sp, color = PimpinanWarna.Abu500)
                }
                Text(data.periode.label, fontSize = 12.sp, color = PimpinanWarna.Abu500)
                Text(
                    "Dihitung sampai ${data.periode.dihitungSampai}",
                    fontSize = 11.sp,
                    color = PimpinanWarna.Abu400,
                )

                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Cincin(
                        nilai = r.persen,
                        ukuran = 110.dp,
                        tebal = 12.dp,
                        gradasi = gradPersen(r.persen),
                    ) {
                        Text(teksPersen(r.persen), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.width(20.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Kpi("Terisi", "${r.terisi}", "dari ${r.seharusnya} sesi", PimpinanWarna.Abu500, PimpinanWarna.Hijau)
                            Kpi("Terlewat", "${r.terlewat}", null, PimpinanWarna.Abu500, PimpinanWarna.Merah)
                        }
                        Text(
                            "${r.sesiPerMinggu} sesi per minggu",
                            fontSize = 12.sp,
                            color = PimpinanWarna.Abu500,
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    if (r.terakhirIsi == null) "Belum pernah mengisi jurnal"
                    else "Terakhir mengisi jurnal ${r.terakhirIsi}",
                    fontSize = 12.sp,
                    color = PimpinanWarna.Abu500,
                )
            }
        }

        item {
            Bagian("Sesi terlewat (${data.terlewat.size})") {
                if (data.terlewat.isEmpty()) {
                    Text(
                        "Tidak ada sesi yang terlewat di periode ini.",
                        fontSize = 13.sp,
                        color = PimpinanWarna.TeksHijau,
                    )
                }
            }
        }

        perTanggal.forEach { (_, sesi) ->
            item(key = sesi.first().tanggal) {
                GrupTanggal(sesi)
            }
        }
    }
}

@Composable
private fun GrupTanggal(sesi: List<SesiTerlewat>) {
    val pertama = sesi.first()

    Column {
        Text(
            "${pertama.hari}, ${pertama.tanggalLabel}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PimpinanWarna.Abu700,
        )
        Spacer(Modifier.height(6.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            sesi.forEach { s ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            listOfNotNull(s.kelas, s.mapel).joinToString("  ·  ").ifBlank { "-" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            "${s.jamMulai} – ${s.jamSelesai}",
                            fontSize = 12.sp,
                            color = PimpinanWarna.Abu500,
                        )
                    }
                    Text(
                        if (s.jamKeMulai == s.jamKeSelesai) "Jam ke-${s.jamKeMulai}"
                        else "Jam ke-${s.jamKeMulai}–${s.jamKeSelesai}",
                        fontSize = 12.sp,
                        color = PimpinanWarna.TeksMerah,
                    )
                }
            }
        }
    }
}