package com.sipedas.app.ui.sholat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.components.ScreenHeader
import com.sipedas.app.ui.theme.Green600

private data class OpsiSholat(val kode: String, val label: String, val warna: Color)

private val opsiHadir = OpsiSholat("hadir", "Hadir", Green600)
private val opsiTidakHadir = OpsiSholat("tidak_hadir", "Tidak hadir", Color(0xFFDC2626))
private val opsiBerhalangan = OpsiSholat("berhalangan", "Berhalangan", Color(0xFFD97706))

private val semuaOpsi = listOf(opsiHadir, opsiTidakHadir, opsiBerhalangan)

private fun alasanTerkunci(statusHarian: String?): String = when (statusHarian) {
    "sakit" -> "Sakit (absensi pagi)"
    "izin" -> "Izin (absensi pagi)"
    "alpa" -> "Alpa (absensi pagi)"
    null -> "Belum diabsen pagi"
    else -> "Tidak ikut"
}

@Composable
fun SholatSiswaScreen(
    waktu: String,
    kelasId: Int,
    onBack: () -> Unit,
    viewModel: SholatSiswaViewModel = viewModel(
        key = "sholat-$waktu-$kelasId",
        factory = SholatSiswaViewModel.factory(waktu, kelasId),
    ),
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "${labelWaktu(waktu)} · ${viewModel.kelasNama}".trimEnd(' ', '·'),
            onBack = onBack,
        )

        when {
            viewModel.loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            viewModel.error != null && viewModel.siswa.isEmpty() -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(viewModel.error ?: "", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = viewModel::muat) { Text("Coba lagi") }
            }

            else -> Konten(viewModel)
        }
    }
}

@Composable
private fun Konten(viewModel: SholatSiswaViewModel) {
    val semua = viewModel.siswa
    val bisaDitandai = semua.filter { !it.terkunci }
    val terkunci = semua.size - bisaDitandai.size
    val belum = bisaDitandai.count { it.status == null }

    Column(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                semuaOpsi.forEach { opsi ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(opsi.warna.copy(alpha = 0.12f))
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "${bisaDitandai.count { it.status == opsi.kode }}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = opsi.warna,
                        )
                        Text(opsi.label, style = MaterialTheme.typography.labelMedium, color = opsi.warna)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = buildString {
                        append(if (belum == 0) "Semua siswa sudah ditandai" else "$belum siswa belum ditandai")
                        if (terkunci > 0) append(" · $terkunci tidak ikut (absen pagi)")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = viewModel::semuaHadir) { Text("Tandai semua hadir") }
            }
        }

        viewModel.pesan?.let { p ->
            Text(
                text = p.teks,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (p.sukses) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (semua.isEmpty()) {
                item {
                    Text(
                        "Tidak ada siswa muslim di kelas ini.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(semua, key = { it.id }) { s ->
                SiswaSholatCard(s, onStatus = { kode -> viewModel.setStatus(s.id, kode) })
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
            Button(
                onClick = viewModel::simpan,
                enabled = bisaDitandai.isNotEmpty() && belum == 0 && !viewModel.saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (viewModel.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        text = when {
                            bisaDitandai.isEmpty() -> "Tidak ada siswa yang perlu ditandai"
                            belum > 0 -> "Tandai $belum siswa lagi"
                            else -> "Simpan absensi sholat"
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun SiswaSholatCard(s: SholatItem, onStatus: (String) -> Unit) {
    val warna = semuaOpsi.firstOrNull { it.kode == s.status }?.warna ?: MaterialTheme.colorScheme.onSurfaceVariant

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (s.terkunci) 0.6f else 1f),
        contentPadding = 14.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(warna.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = s.nama.trim().firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = warna,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    s.nama,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    s.nis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        if (s.terkunci) {
            Text(
                "Tidak ikut sholat · ${alasanTerkunci(s.statusHarian)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            // "Berhalangan" hanya tersedia untuk siswi
            val daftar = if (s.jenisKelamin == "P") semuaOpsi else listOf(opsiHadir, opsiTidakHadir)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                daftar.forEach { opsi ->
                    PilihanChip(
                        opsi = opsi,
                        dipilih = s.status == opsi.kode,
                        onClick = { onStatus(opsi.kode) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PilihanChip(
    opsi: OpsiSholat,
    dipilih: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (dipilih) opsi.warna else opsi.warna.copy(alpha = 0.08f))
            .border(1.dp, opsi.warna.copy(alpha = if (dipilih) 1f else 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            opsi.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (dipilih) Color.White else opsi.warna,
        )
    }
}