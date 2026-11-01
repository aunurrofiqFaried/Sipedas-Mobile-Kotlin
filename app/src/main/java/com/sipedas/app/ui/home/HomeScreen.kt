package com.sipedas.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipedas.app.data.local.Session
import com.sipedas.app.data.model.JadwalDto
import com.sipedas.app.data.model.JurnalBulanIniDto
import com.sipedas.app.data.model.KelasWaliStatDto
import com.sipedas.app.data.model.LiburDto
import com.sipedas.app.data.model.MengajarStatDto
import com.sipedas.app.data.model.PeringatanDto
import com.sipedas.app.data.model.PiketDto
import com.sipedas.app.data.model.StatistikGuruResponse
import com.sipedas.app.ui.components.BannerLibur
import com.sipedas.app.ui.components.BannerPeringatan
import com.sipedas.app.ui.components.GlassCard
import com.sipedas.app.ui.pengumuman.LoncengPengumuman
import com.sipedas.app.ui.theme.Green100
import com.sipedas.app.ui.theme.Green600
import com.sipedas.app.ui.theme.Green700
import com.sipedas.app.ui.theme.Orange500
import com.sipedas.app.ui.theme.Orange600
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Locale

private val warnaMerah = Color(0xFFDC2626)
private val warnaBiru = Color(0xFF0284C7)
private val warnaAmber = Color(0xFFD97706)

private val namaHari = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    session: Session,
    belumDibaca: Int,
    onBukaPengumuman: () -> Unit,
    onKeJurnal: () -> Unit,
    // key = token: tiap login baru mendapat ViewModel baru, jadi data guru sebelumnya tidak terbawa
    viewModel: HomeViewModel = viewModel(key = session.token, factory = HomeViewModel.Factory),
) {
    PullToRefreshBox(
        isRefreshing = viewModel.refreshing,
        onRefresh = viewModel::muatUlang,
        modifier = Modifier.fillMaxSize(),
    ) {
        when (val state = viewModel.state) {
            HomeState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is HomeState.Error -> Column(
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

            // Sesi berakhir ditangani terpusat oleh ApiClient.onUnauthorized
            HomeState.Unauthorized -> Unit

            is HomeState.Success -> HomeContent(
                session = session,
                tanggal = state.data.tanggal,
                jadwal = state.data.data,
                piket = state.data.piket.orEmpty(),
                libur = state.data.libur,
                peringatan = state.data.peringatan.orEmpty(),
                statistik = viewModel.statistik,
                belumDibaca = belumDibaca,
                onBukaPengumuman = onBukaPengumuman,
                onKeJurnal = onKeJurnal,
            )
        }
    }
}

@Composable
private fun HomeContent(
    session: Session,
    tanggal: String,
    jadwal: List<JadwalDto>,
    piket: List<PiketDto>,
    libur: LiburDto?,
    peringatan: List<PeringatanDto>,
    statistik: StatistikGuruResponse?,
    belumDibaca: Int,
    onBukaPengumuman: () -> Unit,
    onKeJurnal: () -> Unit,
) {
    // Sesi berikutnya hari ini, dihitung dari jam di HP
    val sekarang = LocalTime.now()
    val berikutnya = jadwal.firstOrNull {
        runCatching { LocalTime.parse(it.jamMulai.take(5)) > sekarang }.getOrDefault(false)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeroCard(
                nama = session.name.substringBefore(' ').substringBefore(','),
                tanggal = formatTanggal(tanggal),
                sesi = jadwal.size,
                jurnalTerisi = jadwal.count { it.sudahJurnal },
                belumDibaca = belumDibaca,
                onBukaPengumuman = onBukaPengumuman,
            )
        }

        if (piket.isNotEmpty()) {
            item { PiketCard(piket) }
        }

        if (libur != null) {
            item { BannerLibur(libur.judul, libur.keterangan) }
        }

        peringatan.forEach { p ->
            item { BannerPeringatan(p.judul, p.keterangan) }
        }

        // Pengganti tiga tombol lama: statistik mengajar
        if (statistik != null) {
            item { StatistikMengajar(statistik.mengajar) }
        }

        item {
            Text("Jadwal mengajar hari ini", style = MaterialTheme.typography.titleMedium)
        }

        if (jadwal.isEmpty()) {
            item {
                Text(
                    "Tidak ada jadwal mengajar hari ini.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (berikutnya != null) {
            item { SesiBerikutnyaCard(berikutnya) }
        }

        items(jadwal, key = { it.id }) { JadwalCard(it, onIsiJurnal = onKeJurnal) }

        // Ringkasan bulan ini di bagian bawah
        if (statistik != null) {
            item {
                Text("Bulan ini", style = MaterialTheme.typography.titleMedium)
            }

            item { JurnalBulanCard(statistik.jurnalBulanIni) }

            items(statistik.kelasWali, key = { "wali-${it.id}" }) { KelasWaliCard(it) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        ikon = Icons.Filled.Warning,
                        nilai = "${statistik.pelanggaranDicatat}",
                        label = "Pelanggaran dicatat",
                        warna = warnaAmber,
                        modifier = Modifier.weight(1f),
                    )

                    val terpadat = statistik.mengajar.hariTerpadat
                    StatTile(
                        ikon = Icons.Filled.Star,
                        nilai = terpadat?.let { namaHari.getOrElse(it.hari - 1) { "-" } } ?: "-",
                        label = terpadat?.let { "Hari terpadat · ${it.jam} jam" } ?: "Hari terpadat",
                        warna = Orange600,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    nama: String,
    tanggal: String,
    sesi: Int,
    jurnalTerisi: Int,
    belumDibaca: Int,
    onBukaPengumuman: () -> Unit,
) {
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(Orange500, Orange600)))
            .drawBehind {
                // dua lingkaran putih transparan sebagai hiasan
                drawCircle(
                    color = Color.White.copy(alpha = 0.14f),
                    radius = size.height * 0.9f,
                    center = Offset(size.width * 0.95f, -size.height * 0.1f),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = size.height * 0.5f,
                    center = Offset(size.width * 0.72f, size.height * 1.05f),
                )
            }
            .padding(22.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Halo, $nama",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        tanggal,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f),
                    )
                }

                // Lonceng pengumuman di pojok kanan kartu
                LoncengPengumuman(
                    belumDibaca = belumDibaca,
                    onClick = onBukaPengumuman,
                    tint = Color.White,
                    modifier = Modifier.offset(x = 10.dp, y = (-10).dp),
                )
            }

            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatPill("$sesi", "Sesi mengajar", Modifier.weight(1f))
                StatPill("$jurnalTerisi/$sesi", "Jurnal terisi", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatPill(nilai: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            nilai,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.92f))
    }
}

@Composable
private fun StatistikMengajar(m: MengajarStatDto) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Statistik mengajar", style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(Icons.Filled.AccountBox, "${m.jumlahKelas}", "Kelas diampu", Orange600, Modifier.weight(1f))
            StatTile(Icons.Filled.Create, "${m.jumlahMapel}", "Mata pelajaran", Green600, Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(Icons.Filled.DateRange, "${m.jamPerMinggu}", "Jam per minggu", warnaBiru, Modifier.weight(1f))
            StatTile(Icons.Filled.Face, "${m.jumlahSiswa}", "Siswa diajar", warnaAmber, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(
    ikon: ImageVector,
    nilai: String,
    label: String,
    warna: Color,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier, contentPadding = 14.dp) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(warna.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(ikon, contentDescription = null, tint = warna, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.height(10.dp))

        Text(
            nilai,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun JurnalBulanCard(j: JurnalBulanIniDto) {
    val persen = j.persen
    val warna = when {
        persen == null -> MaterialTheme.colorScheme.onSurfaceVariant
        persen >= 90 -> Green600
        persen >= 70 -> Orange600
        else -> warnaMerah
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Kepatuhan jurnal",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Text(
                persen?.let { "$it%" } ?: "-",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = warna,
            )
        }

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { (persen ?: 0) / 100f },
            color = warna,
            trackColor = warna.copy(alpha = 0.15f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = if (j.seharusnya == 0) {
                "Belum ada sesi bulan ini yang perlu dijurnal."
            } else {
                "${j.terisi} dari ${j.seharusnya} sesi mengajar sudah ada jurnalnya (sampai kemarin)"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun KelasWaliCard(k: KelasWaliStatDto) {
    val persen = k.kehadiranPersen
    val warna = when {
        persen == null -> MaterialTheme.colorScheme.onSurfaceVariant
        persen >= 90 -> Green600
        persen >= 75 -> Orange600
        else -> warnaMerah
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Kelas wali · ${k.nama}", style = MaterialTheme.typography.titleSmall)
                Text(
                    "${k.jumlahSiswa} siswa · absensi tercatat ${k.hariTercatat} hari",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    persen?.let { "$it%" } ?: "-",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = warna,
                )
                Text(
                    "kehadiran",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LencanaKehadiran("Sakit", k.sakit, warnaAmber, Modifier.weight(1f))
            LencanaKehadiran("Izin", k.izin, warnaBiru, Modifier.weight(1f))
            LencanaKehadiran("Alpa", k.alpa, warnaMerah, Modifier.weight(1f))
        }
    }
}

@Composable
private fun LencanaKehadiran(label: String, nilai: Int, warna: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(warna.copy(alpha = 0.12f))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "$nilai",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = warna,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = warna)
    }
}

@Composable
private fun SesiBerikutnyaCard(j: JadwalDto) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(warnaBiru.copy(alpha = 0.10f))
            .border(1.dp, warnaBiru.copy(alpha = 0.35f), shape)
            .padding(14.dp),
    ) {
        Text(
            "Sesi berikutnya · ${j.jamMulai.take(5)}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = warnaBiru,
        )
        Text(
            "${j.kelas?.nama ?: "-"} · ${j.mapel?.nama ?: "-"}",
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
private fun PiketCard(piket: List<PiketDto>) {
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Green100)
            .border(1.dp, Green600.copy(alpha = 0.4f), shape)
            .padding(16.dp),
    ) {
        Text(
            "Anda piket hari ini",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Green700,
        )

        piket.forEach { p ->
            val rentang = if (p.jamKeMulai == p.jamKeSelesai) {
                "Jam ke-${p.jamKeMulai}"
            } else {
                "Jam ke-${p.jamKeMulai}-${p.jamKeSelesai}"
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "$rentang · ${p.jamMulai} - ${p.jamSelesai}",
                style = MaterialTheme.typography.titleSmall,
            )
            if (!p.tugas.isNullOrBlank()) {
                Text(p.tugas, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun JadwalCard(j: JadwalDto, onIsiJurnal: () -> Unit) {
    val rentang = if (j.jamKeMulai == j.jamKeSelesai) "${j.jamKeMulai}" else "${j.jamKeMulai}-${j.jamKeSelesai}"

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Jam ke-$rentang",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${j.jamMulai.take(5)} - ${j.jamSelesai.take(5)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(j.kelas?.nama ?: "-", style = MaterialTheme.typography.titleLarge)
        Text(
            j.mapel?.nama ?: "-",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        j.ruang?.let { ruang ->
            Spacer(Modifier.height(4.dp))
            Text(
                "${if (ruang.tipe == "lab") "Lab" else "Kelas"}: ${ruang.nama}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(12.dp))

        when {
            j.sudahJurnal -> Text(
                "✓ Jurnal sudah diisi",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )

            j.bisaIsiJurnal -> Button(
                onClick = onIsiJurnal,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Isi jurnal") }

            else -> Text(
                "Jurnal hanya bisa diisi pada hari jadwalnya",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// "2026-10-26" -> "Senin, 26 Oktober 2026"
private fun formatTanggal(iso: String): String = try {
    val masuk = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso)
    SimpleDateFormat("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID")).format(masuk!!)
} catch (e: Exception) {
    iso
}