package com.sipedas.app.ui.kalender

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
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.theme.Green600
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeId: Locale = Locale.forLanguageTag("id-ID")
private val formatBulan = DateTimeFormatter.ofPattern("MMMM yyyy", localeId)
private val formatHariLengkap = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", localeId)
private val formatHariPendek = DateTimeFormatter.ofPattern("EEEE, d MMM", localeId)
private val formatTanggalPendek = DateTimeFormatter.ofPattern("d MMM", localeId)

private val namaHari = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

@Composable
fun KalenderMengajar(
    token: String,
    viewModel: KalenderViewModel = viewModel(
        key = "kalender-$token",
        factory = KalenderViewModel.Factory,
    ),
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text("Kalender mengajar", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        when (val state = viewModel.state) {
            KalenderUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is KalenderUiState.Error -> Column {
                Text(state.message, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = viewModel::muat) { Text("Coba lagi") }
            }

            is KalenderUiState.Success -> IsiKalender(viewModel, state)
        }
    }
}

@Composable
private fun IsiKalender(viewModel: KalenderViewModel, state: KalenderUiState.Success) {
    val bulan = viewModel.bulan

    // Judul bulan dan panah
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

    Text(
        "Tahun ajaran ${state.data.tahunAjaran.nama}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(8.dp))

    // Nama hari
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

    // Kotak tanggal: minggu dimulai hari Senin
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
                    val sesi = viewModel.sesiPada(tanggal)
                    val dalamTahunAjaran = tanggal >= state.mulai && tanggal <= state.selesai
                    val hariKerja = tanggal.dayOfWeek.value <= 5

                    SelTanggal(
                        tanggal = tanggal,
                        jumlahSesi = sesi.size,
                        dipilih = tanggal == viewModel.tanggalDipilih,
                        hariIni = tanggal == hariIni,
                        aktif = dalamTahunAjaran && hariKerja,
                        onClick = { viewModel.pilihTanggal(tanggal) },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // Pilihan tampilan
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChipMode("Per tanggal", viewModel.mode == ModeKalender.TANGGAL, Modifier.weight(1f)) {
            viewModel.ubahMode(ModeKalender.TANGGAL)
        }
        ChipMode("Per minggu", viewModel.mode == ModeKalender.MINGGU, Modifier.weight(1f)) {
            viewModel.ubahMode(ModeKalender.MINGGU)
        }
    }

    Spacer(Modifier.height(12.dp))

    when (viewModel.mode) {
        ModeKalender.TANGGAL -> DetailTanggal(viewModel)
        ModeKalender.MINGGU -> DetailMinggu(viewModel, bulan)
    }
}

@Composable
private fun SelTanggal(
    tanggal: LocalDate,
    jumlahSesi: Int,
    dipilih: Boolean,
    hariIni: Boolean,
    aktif: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primer = MaterialTheme.colorScheme.primary

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
                    !aktif -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        }

        Spacer(Modifier.height(2.dp))

        // Satu titik hijau per sesi mengajar (paling banyak 5 titik)
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.height(6.dp),
        ) {
            repeat(jumlahSesi.coerceAtMost(5)) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Green600),
                )
            }
        }
    }
}

@Composable
private fun ChipMode(label: String, dipilih: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    val warna = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (dipilih) warna else warna.copy(alpha = 0.08f))
            .border(1.dp, warna.copy(alpha = if (dipilih) 1f else 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (dipilih) MaterialTheme.colorScheme.onPrimary else warna,
        )
    }
}

@Composable
private fun DetailTanggal(viewModel: KalenderViewModel) {
    val tanggal = viewModel.tanggalDipilih ?: return
    val sesi = viewModel.sesiPada(tanggal)

    Text(
        tanggal.format(formatHariLengkap),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(8.dp))

    if (sesi.isEmpty()) {
        Text(
            "Tidak ada sesi mengajar pada tanggal ini.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            sesi.forEach { SesiBaris(it) }
        }
    }
}

@Composable
private fun DetailMinggu(viewModel: KalenderViewModel, bulan: YearMonth) {
    val minggu = mingguDalamBulan(bulan)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        minggu.forEachIndexed { index, hari ->
            val rentang = "${hari.first().format(formatTanggalPendek)} - ${hari.last().format(formatTanggalPendek)}"
            val hariBersesi = hari.filter { viewModel.sesiPada(it).isNotEmpty() }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                    .padding(14.dp),
            ) {
                Text(
                    "Minggu ${index + 1} · $rentang",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                if (hariBersesi.isEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tidak ada sesi mengajar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                hariBersesi.forEach { tanggal ->
                    Spacer(Modifier.height(10.dp))
                    Text(
                        tanggal.format(formatHariPendek),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        viewModel.sesiPada(tanggal).forEach { SesiBaris(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SesiBaris(j: JadwalMingguDto) {
    val rentang = if (j.jamKeMulai == j.jamKeSelesai) "${j.jamKeMulai}" else "${j.jamKeMulai}-${j.jamKeSelesai}"

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(width = 4.dp, height = 44.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Green600),
        )

        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(
                "Jam ke-$rentang · ${j.jamMulai} - ${j.jamSelesai}",
                style = MaterialTheme.typography.labelLarge,
                color = Green600,
            )
            Text(
                "${j.kelas?.nama ?: "-"} · ${j.mapel?.nama ?: "-"}",
                style = MaterialTheme.typography.titleSmall,
            )
            j.ruang?.let { ruang ->
                Text(
                    "${if (ruang.tipe == "lab") "Lab" else "Kelas"}: ${ruang.nama}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// Pecah satu bulan menjadi minggu-minggu (Senin sampai Minggu), hanya hari yang masuk bulan itu
private fun mingguDalamBulan(bulan: YearMonth): List<List<LocalDate>> {
    val akhir = bulan.atEndOfMonth()
    val hasil = mutableListOf<List<LocalDate>>()
    var hari = bulan.atDay(1)

    while (hari <= akhir) {
        val minggu = mutableListOf<LocalDate>()
        var x = hari

        do {
            minggu.add(x)
            x = x.plusDays(1)
        } while (x.dayOfWeek != DayOfWeek.MONDAY && x <= akhir)

        hasil.add(minggu)
        hari = x
    }

    return hasil
}