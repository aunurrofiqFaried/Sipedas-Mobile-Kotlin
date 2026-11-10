package com.sipedas.app.ui.pimpinan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.model.GuruKepatuhan
import com.sipedas.app.data.model.KepatuhanJurnalResponse
import com.sipedas.app.ui.components.ScreenHeader

@Composable
fun KepatuhanScreen(
    onBack: () -> Unit,
    onBukaGuru: (id: Int, dari: String, sampai: String) -> Unit,
    viewModel: KepatuhanViewModel = viewModel(factory = KepatuhanViewModel.Factory),
) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Kepatuhan Jurnal", onBack = onBack)

        PilihanPeriode(
            terpilih = viewModel.periode,
            onPilih = viewModel::pilihPeriode,
        )

        PullToRefreshBox(
            isRefreshing = viewModel.refreshing,
            onRefresh = viewModel::muatUlang,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (val s = viewModel.state) {
                KepatuhanState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                is KepatuhanState.Error -> Column(
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
                KepatuhanState.Unauthorized -> Unit

                is KepatuhanState.Success -> IsiKepatuhan(s.data, onBukaGuru)
            }
        }
    }
}

/* ---------- Pilihan periode ---------- */

@Composable
private fun PilihanPeriode(
    terpilih: PeriodeKepatuhanPilihan,
    onPilih: (PeriodeKepatuhanPilihan) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PeriodeKepatuhanPilihan.entries.forEach { p ->
            Pilihan(p.label, aktif = p == terpilih) { onPilih(p) }
        }
    }
}

@Composable
private fun Pilihan(teks: String, aktif: Boolean, onClick: () -> Unit) {
    val bentuk = RoundedCornerShape(50)

    Box(
        Modifier
            .clip(bentuk)
            .background(if (aktif) MaterialTheme.colorScheme.primary else Color(0x0F000000))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            teks,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (aktif) Color.White else PimpinanWarna.Abu700,
        )
    }
}

/* ---------- Isi ---------- */

@Composable
private fun IsiKepatuhan(
    data: KepatuhanJurnalResponse,
    onBukaGuru: (Int, String, String) -> Unit,
) {
    var cari by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf("semua") }

    val tampil = data.guru.filter { g ->
        (status == "semua" || g.status == status) &&
                (cari.isBlank() || g.nama.contains(cari.trim(), ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item { Ringkasan(data) }

        if (data.mingguan.isNotEmpty()) {
            item {
                Bagian("Per minggu") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        data.mingguan.forEach { m ->
                            Column {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(m.label, fontSize = 12.sp, color = PimpinanWarna.Abu500)
                                    Text(
                                        "${m.terisi}/${m.seharusnya}  ·  ${teksPersen(m.persen)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = gradPersen(m.persen)[0],
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Batang(m.persen ?: 0, 100, gradPersen(m.persen))
                            }
                        }
                    }
                }
            }
        }

        item {
            Bagian("Guru (${tampil.size})") {
                OutlinedTextField(
                    value = cari,
                    onValueChange = { cari = it },
                    placeholder = { Text("Cari nama guru") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        "semua" to "Semua",
                        "kurang" to "Kurang (${data.ringkasan.kurang})",
                        "cukup" to "Cukup (${data.ringkasan.cukup})",
                        "baik" to "Baik (${data.ringkasan.baik})",
                    ).forEach { (kode, label) ->
                        Pilihan(label, aktif = status == kode) { status = kode }
                    }
                }
            }
        }

        if (tampil.isEmpty()) {
            item {
                Text(
                    "Tidak ada guru yang cocok.",
                    fontSize = 13.sp,
                    color = PimpinanWarna.Abu400,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }

        items(tampil, key = { it.id }) { g ->
            BarisGuru(g) { onBukaGuru(g.id, data.periode.dari, data.periode.sampai) }
        }
    }
}

@Composable
private fun Ringkasan(data: KepatuhanJurnalResponse) {
    val r = data.ringkasan

    Column {
        Text(
            data.periode.label,
            fontSize = 12.sp,
            color = PimpinanWarna.Abu500,
        )
        Text(
            "Dihitung sampai ${data.periode.dihitungSampai}",
            fontSize = 11.sp,
            color = PimpinanWarna.Abu400,
        )
        Spacer(Modifier.height(12.dp))

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
                    Kpi("Terlewat", "${r.terlewat}", "${r.hariEfektif} hari efektif", PimpinanWarna.Abu500, PimpinanWarna.Merah)
                }
                Text(
                    "${r.guru} guru  ·  ${r.baik} baik  ·  ${r.cukup} cukup  ·  ${r.kurang} kurang",
                    fontSize = 12.sp,
                    color = PimpinanWarna.Abu500,
                )
            }
        }
    }
}

@Composable
private fun BarisGuru(g: GuruKepatuhan, onClick: () -> Unit) {
    val warna = gradPersen(g.persen)

    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    g.nama,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (g.seharusnya == 0) "Belum ada sesi di periode ini"
                    else "${g.terisi}/${g.seharusnya} sesi  ·  ${g.terlewat} terlewat",
                    fontSize = 12.sp,
                    color = PimpinanWarna.Abu500,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                teksPersen(g.persen),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = warna[0],
            )
        }

        Spacer(Modifier.height(6.dp))
        Batang(g.persen ?: 0, 100, warna)
        Spacer(Modifier.height(4.dp))

        Text(
            if (g.terakhirIsi == null) "Belum pernah mengisi jurnal" else "Terakhir mengisi ${g.terakhirIsi}",
            fontSize = 11.sp,
            color = PimpinanWarna.Abu400,
        )
    }
}