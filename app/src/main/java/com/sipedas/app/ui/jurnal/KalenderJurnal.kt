package com.sipedas.app.ui.jurnal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.model.JadwalMingguDto
import com.sipedas.app.data.model.JurnalBulanDto
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.theme.Green600
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val warnaTerlewat = Color(0xFFDC2626)

private val localeId: Locale = Locale.forLanguageTag("id-ID")
private val formatBulan = DateTimeFormatter.ofPattern("MMMM yyyy", localeId)
private val formatHariLengkap = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", localeId)
private val namaHari = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

@Composable
fun KalenderJurnal(
    viewModel: JurnalKalenderViewModel = viewModel(factory = JurnalKalenderViewModel.Factory),
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text("Riwayat jurnal", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        when {
            viewModel.loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            viewModel.error != null -> Column {
                Text(viewModel.error ?: "", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = viewModel::muat) { Text("Coba lagi") }
            }

            else -> Isi(viewModel)
        }
    }
}

@Composable
private fun Isi(viewModel: JurnalKalenderViewModel) {
    val bulan = viewModel.bulan

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { viewModel.geserBulan(-1) }, enabled = viewModel.bisaMundur()) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Bulan sebelumnya")
        }
        Text(
            text = bulan.format(formatBulan),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { viewModel.geserBulan(1) }, enabled = viewModel.bisaMaju()) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Bulan berikutnya")
        }
    }

    Spacer(Modifier.height(4.dp))

    Row(modifier = Modifier.fillMaxWidth()) {
        namaHari.forEach { nama ->
            Text(
                nama,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }

    Spacer(Modifier.height(4.dp))

    val awal = bulan.atDay(1)
    val offset = awal.dayOfWeek.value - 1
    val jumlahHari = bulan.lengthOfMonth()
    val jumlahBaris = (offset + jumlahHari + 6) / 7
    val hariIni = LocalDate.now()

    for (baris in 0 until jumlahBaris) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (kolom in 0 until 7) {
                val tanggalKe = baris * 7 + kolom - offset + 1

                if (tanggalKe in 1..jumlahHari) {
                    val tanggal = bulan.atDay(tanggalKe)

                    SelJurnal(
                        tanggal = tanggal,
                        terisi = viewModel.jurnalPada(tanggal).size,
                        terlewat = viewModel.terlewatPada(tanggal).size,
                        dipilih = tanggal == viewModel.tanggalDipilih,
                        hariIni = tanggal == hariIni,
                        akhirPekan = tanggal.dayOfWeek.value > 5,
                        onClick = { viewModel.pilihTanggal(tanggal) },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }

    // Keterangan warna
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        TitikKeterangan(Green600, "Jurnal terisi")
        TitikKeterangan(warnaTerlewat, "Belum diisi")
    }

    if (viewModel.bulanMemuat) {
        Spacer(Modifier.height(6.dp))
        Text(
            "Memuat data bulan ini…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    viewModel.bulanError?.let {
        Spacer(Modifier.height(6.dp))
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(6.dp))
        Button(onClick = viewModel::coba) { Text("Coba lagi") }
    }

    Spacer(Modifier.height(14.dp))

    Detail(viewModel)
}

@Composable
private fun SelJurnal(
    tanggal: LocalDate,
    terisi: Int,
    terlewat: Int,
    dipilih: Boolean,
    hariIni: Boolean,
    akhirPekan: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primer = MaterialTheme.colorScheme.primary

    // Paling banyak 5 titik; kalau ada yang terlewat, satu titik merah selalu disisakan
    val hijau = if (terlewat > 0) terisi.coerceAtMost(4) else terisi.coerceAtMost(5)
    val merah = terlewat.coerceAtMost(5 - hijau)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (dipilih) primer else Color.Transparent)
                .then(if (hariIni && !dipilih) Modifier.border(1.5.dp, primer, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${tanggal.dayOfMonth}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (dipilih || hariIni) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    dipilih -> MaterialTheme.colorScheme.onPrimary
                    akhirPekan -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        }

        Spacer(Modifier.height(2.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.height(6.dp)) {
            repeat(hijau) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(Green600))
            }
            repeat(merah) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(warnaTerlewat))
            }
        }
    }
}

@Composable
private fun Detail(viewModel: JurnalKalenderViewModel) {
    val tanggal = viewModel.tanggalDipilih ?: return
    val jurnal = viewModel.jurnalPada(tanggal)
    val terlewat = viewModel.terlewatPada(tanggal)

    Text(
        tanggal.format(formatHariLengkap),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(8.dp))

    if (jurnal.isEmpty() && terlewat.isEmpty()) {
        Text(
            "Tidak ada jurnal pada tanggal ini.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        jurnal.forEach { JurnalBaris(it) }
        terlewat.forEach { TerlewatBaris(it) }
    }
}

@Composable
private fun JurnalBaris(j: JurnalBulanDto) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(width = 4.dp, height = 48.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Green600),
        )

        Column(modifier = Modifier.padding(start = 10.dp)) {
            if (j.jamKeMulai != null) {
                val rentang = if (j.jamKeMulai == j.jamKeSelesai) "${j.jamKeMulai}" else "${j.jamKeMulai}-${j.jamKeSelesai}"
                Text(
                    "Jam ke-$rentang · ${j.jamMulai ?: ""} - ${j.jamSelesai ?: ""}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Green600,
                )
            }

            Text(
                "${j.kelas?.nama ?: "-"} · ${j.mapel?.nama ?: "-"}",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(2.dp))
            Text(j.materi, style = MaterialTheme.typography.bodyMedium)

            if (!j.catatan.isNullOrBlank()) {
                Text(
                    "Catatan: ${j.catatan}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TerlewatBaris(s: JadwalMingguDto) {
    val rentang = if (s.jamKeMulai == s.jamKeSelesai) "${s.jamKeMulai}" else "${s.jamKeMulai}-${s.jamKeSelesai}"

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(width = 4.dp, height = 48.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(warnaTerlewat),
        )

        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(
                "Jam ke-$rentang · ${s.jamMulai} - ${s.jamSelesai}",
                style = MaterialTheme.typography.labelLarge,
                color = warnaTerlewat,
            )
            Text(
                "${s.kelas?.nama ?: "-"} · ${s.mapel?.nama ?: "-"}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                "Jurnal belum diisi",
                style = MaterialTheme.typography.bodySmall,
                color = warnaTerlewat,
            )
        }
    }
}

@Composable
private fun TitikKeterangan(warna: Color, teks: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(warna))
        Spacer(Modifier.width(6.dp))
        Text(
            teks,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}