package com.sipedas.app.ui.pimpinan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipedas.app.data.model.*

private fun jam(a: Int, b: Int) = if (a == b) "$a" else "$a-$b"

private fun warnaTingkat(t: String): Color = when (t) {
    "danger" -> PimpinanWarna.TeksMerah
    "warn" -> PimpinanWarna.TeksKuning
    "info" -> PimpinanWarna.TeksBiru
    else -> PimpinanWarna.TeksHijau
}

@Composable
fun DashboardPimpinanIsi(
    data: DashboardPimpinan,
    onBukaKepatuhan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        KepalaDashboard(data)
        BagianKehadiran(data.absensi)
        BagianPeringatan(data.peringatan, onBukaKepatuhan)
        BagianKpi(data)

        Bagian("Tren kehadiran 14 hari") {
            GrafikArea(
                nilai = data.trenAbsensi.map { it.persen },
                label = data.trenAbsensi.map { it.label },
                gradasi = PimpinanWarna.Hijau,
            )
        }

        BagianSholat(data.sholat)

        Bagian("Peta panas kehadiran · 10 hari") { PetaPanasView(data.petaPanas) }

        BagianKelas(data)
        BagianJurnal(data, onBukaKepatuhan)
        BagianKesiswaan(data)
        BagianJurusan(data.jurusan)

        // Ruang untuk menu bawah yang mengambang
        Spacer(Modifier.height(96.dp))
    }
}

/* ---------- Bagian-bagian ---------- */

@Composable
private fun KepalaDashboard(d: DashboardPimpinan) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "PUSAT KENDALI SEKOLAH",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = PimpinanWarna.TeksOranye,
            letterSpacing = 3.sp,
        )
        Text("Kondisi Sekolah", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            d.hari.label + if (d.hari.hariIni) "" else " · data hari efektif terakhir",
            fontSize = 13.sp,
            color = PimpinanWarna.Abu500,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            TitikLive()
            Spacer(Modifier.width(6.dp))
            Text("LIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PimpinanWarna.TeksHijau)
            Text(
                " · diperbarui ${d.diperbarui.drop(11).take(5)}",
                fontSize = 11.sp,
                color = PimpinanWarna.Abu500,
            )
        }

        if (d.hari.libur != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Hari ini libur (${d.hari.libur}). Angka di bawah memakai data hari efektif terakhir.",
                fontSize = 12.sp,
                color = PimpinanWarna.TeksKuning,
            )
        }
    }
}

@Composable
private fun BagianKehadiran(a: AbsensiDashboard) {
    Bagian("Kehadiran siswa") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Cincin(a.persen, 128.dp, 11.dp, gradPersen(a.persen)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(teksPersen(a.persen), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("HADIR", fontSize = 9.sp, color = PimpinanWarna.Abu400, letterSpacing = 2.sp)
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    listOf(
                        Triple("Hadir", a.hadir, PimpinanWarna.TeksHijau),
                        Triple("Sakit", a.sakit, PimpinanWarna.TeksKuning),
                        Triple("Izin", a.izin, PimpinanWarna.TeksBiru),
                        Triple("Alpa", a.alpa, PimpinanWarna.TeksMerah),
                    ).forEach { (nama, n, warna) ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$n", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = warna)
                            Text(nama, fontSize = 10.sp, color = PimpinanWarna.Abu500)
                        }
                    }
                }

                Column {
                    Text(
                        "Kelas diabsen ${a.kelasDiabsen}/${a.kelasTotal} · ${a.belum} siswa belum",
                        fontSize = 11.sp,
                        color = PimpinanWarna.Abu500,
                    )
                    Spacer(Modifier.height(4.dp))
                    Batang(a.kelasDiabsen, a.kelasTotal, PimpinanWarna.Hijau, tinggi = 8.dp)
                }
            }
        }
    }
}

@Composable
private fun BagianPeringatan(daftar: List<PeringatanDashboard>, onBukaKepatuhan: () -> Unit) {
    Bagian("Perlu perhatian") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            daftar.forEach { p ->
                val warna = warnaTingkat(p.tingkat)

                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .then(
                            if (p.tautan == "/pimpinan/jurnal") Modifier.clickable { onBukaKepatuhan() } else Modifier,
                        ),
                ) {
                    Box(
                        Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(warna),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(p.judul, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = warna)
                        p.detail?.let { Text(it, fontSize = 12.sp, color = PimpinanWarna.Abu500) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarisDua(kiri: @Composable () -> Unit, kanan: @Composable () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        Box(
            Modifier
                .weight(1f)
                .padding(end = 16.dp),
        ) { kiri() }
        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(PimpinanWarna.Abu200),
        )
        Box(
            Modifier
                .weight(1f)
                .padding(start = 16.dp),
        ) { kanan() }
    }
}

@Composable
private fun BagianKpi(d: DashboardPimpinan) {
    val pel = d.ringkasan.pelanggaranBulan
    val selisih = pel.jumlah - pel.jumlahLalu
    val teksSelisih = when {
        selisih == 0 -> "sama dengan bulan lalu"
        selisih > 0 -> "▲ $selisih dari bulan lalu"
        else -> "▼ ${-selisih} dari bulan lalu"
    }
    val warnaSelisih = if (selisih > 0) PimpinanWarna.TeksMerah else PimpinanWarna.TeksHijau
    val j = d.jurnalHari

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PimpinanWarna.Abu200),
        )

        BarisDua(
            kiri = {
                Kpi("Siswa aktif", "${d.ringkasan.siswa}", "${d.ringkasan.kelas} kelas", PimpinanWarna.Abu500, PimpinanWarna.Hijau)
            },
            kanan = {
                Kpi("Guru mengajar", "${d.ringkasan.guruMengajar}", "pada hari data", PimpinanWarna.Abu500, PimpinanWarna.Biru)
            },
        )

        BarisDua(
            kiri = {
                Kpi(
                    "Jurnal hari ini",
                    "${j.terisi}/${j.total}",
                    if (j.terlewat > 0) "${j.terlewat} terlewat" else "tidak ada yang terlewat",
                    if (j.terlewat > 0) PimpinanWarna.TeksMerah else PimpinanWarna.TeksHijau,
                    PimpinanWarna.Oranye,
                )
            },
            kanan = {
                Kpi("Pelanggaran bulan ini", "${pel.jumlah}", teksSelisih, warnaSelisih, PimpinanWarna.Merah)
            },
        )

        BarisDua(
            kiri = {
                Kpi(
                    "SP menunggu",
                    "${d.ringkasan.spMenunggu}",
                    "${d.ringkasan.mendekatiSp} siswa mendekati SP",
                    PimpinanWarna.Abu500,
                    PimpinanWarna.Merah,
                )
            },
            kanan = {},
        )

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PimpinanWarna.Abu200),
        )
    }
}

@Composable
private fun BagianSholat(s: SholatDashboard) {
    Bagian("Sholat hari ini") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Dhuhur" to s.dhuhur, "Ashar" to s.ashar).forEach { (nama, x) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Cincin(x.persen, 96.dp, 9.dp, gradPersen(x.persen)) {
                        Text(teksPersen(x.persen), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(nama, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("${x.hadir} hadir · ${x.tidakHadir} tidak", fontSize = 11.sp, color = PimpinanWarna.Abu500)
                    Text("${x.kelas}/${s.kelasTotal} kelas", fontSize = 11.sp, color = PimpinanWarna.Abu400)
                }
            }
        }
    }
}

@Composable
private fun BarisPersen(nama: String, persen: Int?) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            nama,
            modifier = Modifier.width(84.dp),
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Batang(persen ?: 0, 100, gradPersen(persen), Modifier.weight(1f))
        Text(
            teksPersen(persen),
            modifier = Modifier.width(44.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun BagianKelas(d: DashboardPimpinan) {
    Column(verticalArrangement = Arrangement.spacedBy(26.dp)) {
        Bagian("Kelas belum diabsen (${d.kelasBelum.size})") {
            if (d.kelasBelum.isEmpty()) {
                Text("Semua kelas sudah diabsen.", fontSize = 13.sp, color = PimpinanWarna.TeksHijau)
            } else {
                Text(
                    d.kelasBelum.joinToString(", ") { it.nama },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PimpinanWarna.TeksMerah,
                )
            }
        }

        Bagian("Kehadiran terendah") {
            if (d.kelasTerendah.isEmpty()) {
                Text("Belum ada absensi tercatat.", fontSize = 13.sp, color = PimpinanWarna.Abu400)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    d.kelasTerendah.forEach { BarisPersen(it.nama, it.persen) }
                }
            }
        }
    }
}

@Composable
private fun BagianJurnal(d: DashboardPimpinan, onBukaKepatuhan: () -> Unit) {
    val kep = d.kepatuhan.ringkasan
    val totalGuru = maxOf(1, kep.baik + kep.cukup + kep.kurang)

    Column(verticalArrangement = Arrangement.spacedBy(26.dp)) {
        Bagian(
            "Kepatuhan jurnal bulan ini",
            aksi = {
                Text(
                    "Detail →",
                    modifier = Modifier.clickable { onBukaKepatuhan() },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PimpinanWarna.TeksOranye,
                )
            },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Cincin(kep.persen, 104.dp, 10.dp, gradPersen(kep.persen)) {
                    Text(teksPersen(kep.persen), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple("Baik", kep.baik, PimpinanWarna.Hijau),
                        Triple("Cukup", kep.cukup, PimpinanWarna.Oranye),
                        Triple("Kurang", kep.kurang, PimpinanWarna.Merah),
                    ).forEach { (nama, n, g) ->
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(nama, fontSize = 12.sp, color = PimpinanWarna.Abu700)
                                Text("$n guru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Batang(n, totalGuru, g, tinggi = 5.dp)
                        }
                    }
                }
            }
        }

        Bagian("Sesi belum dijurnal hari ini (${d.jurnalHari.terlewat})") {
            if (d.jurnalHari.daftarTerlewat.isEmpty()) {
                Text("Tidak ada sesi selesai yang terlewat.", fontSize = 13.sp, color = PimpinanWarna.TeksHijau)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    d.jurnalHari.daftarTerlewat.forEach { s ->
                        Column {
                            Text(s.guru, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PimpinanWarna.TeksMerah)
                            Text("${s.kelas ?: "-"} · ${s.mapel ?: "-"}", fontSize = 13.sp, color = PimpinanWarna.Abu700)
                            Text(
                                "Jam ke-${jam(s.jamKeMulai, s.jamKeSelesai)} · ${s.jamMulai}-${s.jamSelesai}",
                                fontSize = 11.sp,
                                color = PimpinanWarna.Abu400,
                            )
                        }
                    }
                }
            }
        }

        Bagian("Guru perlu perhatian") {
            if (d.kepatuhan.terendah.isEmpty()) {
                Text("Belum ada data kepatuhan bulan ini.", fontSize = 13.sp, color = PimpinanWarna.Abu400)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    d.kepatuhan.terendah.forEach { g ->
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    g.nama,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(teksPersen(g.persen), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Batang(g.persen ?: 0, 100, gradPersen(g.persen), tinggi = 5.dp)
                            Text(
                                "${g.terisi}/${g.seharusnya} sesi · ${g.terlewat} terlewat",
                                fontSize = 11.sp,
                                color = PimpinanWarna.Abu400,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "Lihat kepatuhan semua guru →",
                modifier = Modifier.clickable { onBukaKepatuhan() },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PimpinanWarna.TeksOranye,
            )
        }
    }
}

@Composable
private fun BagianKesiswaan(d: DashboardPimpinan) {
    val maksMinggu = maxOf(1, d.pelanggaran.mingguan.maxOfOrNull { it.jumlah } ?: 0)
    val maksJenis = maxOf(1, d.pelanggaran.jenisTeratas.maxOfOrNull { it.jumlah } ?: 0)

    Column(verticalArrangement = Arrangement.spacedBy(26.dp)) {
        Bagian("Pelanggaran 8 minggu") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                d.pelanggaran.mingguan.forEach { m ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${m.jumlah}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = PimpinanWarna.Abu700)
                        Spacer(Modifier.height(2.dp))
                        Box(
                            Modifier
                                .fillMaxWidth(0.7f)
                                .height(maxOf(4f, 80f * m.jumlah / maksMinggu).dp)
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        listOf(PimpinanWarna.Merah[0], PimpinanWarna.Merah[1].copy(alpha = 0.35f)),
                                    ),
                                    androidx.compose.foundation.shape.RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                                ),
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(m.label, fontSize = 9.sp, color = PimpinanWarna.Abu400, maxLines = 1)
                    }
                }
            }

            if (d.pelanggaran.jenisTeratas.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text("JENIS TERBANYAK BULAN INI", fontSize = 10.sp, color = PimpinanWarna.Abu400, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    d.pelanggaran.jenisTeratas.forEach { j ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                j.nama,
                                modifier = Modifier.width(120.dp),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Batang(j.jumlah, maksJenis, PimpinanWarna.Oranye, Modifier.weight(1f), tinggi = 5.dp)
                            Text(
                                "${j.jumlah}x",
                                modifier = Modifier.width(36.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                }
            }
        }

        Bagian("Mendekati SP") {
            if (d.sp.mendekati.isEmpty()) {
                Text("Tidak ada siswa yang mendekati batas.", fontSize = 13.sp, color = PimpinanWarna.TeksHijau)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    d.sp.mendekati.forEach { s ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(s.nama, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(s.kelas, fontSize = 11.sp, color = PimpinanWarna.Abu400)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${s.poinAktif} poin", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PimpinanWarna.TeksKuning)
                                Text("${s.sisa} lagi → ${s.menuju}", fontSize = 11.sp, color = PimpinanWarna.Abu500)
                            }
                        }
                    }
                }
            }
        }

        Bagian("Poin tertinggi") {
            if (d.sp.poinTertinggi.isEmpty()) {
                Text("Belum ada poin aktif.", fontSize = 13.sp, color = PimpinanWarna.Abu400)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    d.sp.poinTertinggi.forEach { s ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(s.nama, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(s.kelas, fontSize = 11.sp, color = PimpinanWarna.Abu400)
                            }
                            Text("${s.poinAktif}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PimpinanWarna.TeksMerah)
                        }
                    }
                }
            }
        }

        if (d.sp.menunggu.isNotEmpty()) {
            Bagian("Usulan SP menunggu (${d.ringkasan.spMenunggu})") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    d.sp.menunggu.forEach { s ->
                        Column {
                            Text("${s.siswa} · ${s.kelas}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PimpinanWarna.TeksKuning)
                            Text("${s.aturan} · ${s.poin} poin · diusulkan ${s.tanggal}", fontSize = 11.sp, color = PimpinanWarna.Abu500)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BagianJurusan(daftar: List<JurusanRingkas>) {
    val maksPel = maxOf(1, daftar.maxOfOrNull { it.pelanggaranBulan } ?: 0)

    Bagian("Perbandingan jurusan") {
        if (daftar.isEmpty()) {
            Text("Belum ada data jurusan.", fontSize = 13.sp, color = PimpinanWarna.Abu400)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                daftar.forEach { j ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(j.nama, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("${j.kelas} kelas · ${j.siswa} siswa", fontSize = 11.sp, color = PimpinanWarna.Abu400)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Kehadiran", Modifier.width(84.dp), fontSize = 12.sp, color = PimpinanWarna.Abu500)
                            Batang(j.persen ?: 0, 100, gradPersen(j.persen), Modifier.weight(1f), tinggi = 5.dp)
                            Text(teksPersen(j.persen), Modifier.width(44.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Pelanggaran", Modifier.width(84.dp), fontSize = 12.sp, color = PimpinanWarna.Abu500)
                            Batang(j.pelanggaranBulan, maksPel, PimpinanWarna.Merah, Modifier.weight(1f), tinggi = 5.dp)
                            Text("${j.pelanggaranBulan}", Modifier.width(44.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Preview dengan data contoh ---------- */

private fun contohDashboard() = DashboardPimpinan(
    diperbarui = "2026-11-10T19:46:12+07:00",
    hari = HariDashboard("2026-11-10", "Selasa, 10 November 2026", true, null),
    ringkasan = RingkasanDashboard(
        siswa = 1004, kelas = 36, guruMengajar = 52,
        pelanggaranBulan = PelanggaranBulan(18, 95, 24, 130),
        spMenunggu = 2, mendekatiSp = 5,
    ),
    absensi = AbsensiDashboard(
        totalSiswa = 1004, tercatat = 980, hadir = 931, sakit = 21, izin = 11, alpa = 17,
        belum = 24, persen = 95, kelasTotal = 36, kelasDiabsen = 35,
    ),
    trenAbsensi = listOf(93, 95, 94, 96, 92, 95, 97, 94, 93, 95, 96, 91, 94, 95).mapIndexed { i, p ->
        TrenAbsensi("2026-11-${i + 1}", "${i + 1} Nov", p, p * 10, 5, 3, 2, 1000)
    },
    petaPanas = PetaPanas(
        tanggal = listOf("2/11", "3/11", "4/11", "5/11", "6/11", "9/11", "10/11", "11/11", "12/11", "13/11"),
        kelas = listOf(
            PetaKelas(1, "X RPL 1", "RPL", listOf(98, 95, 92, null, 88, 96, 99, 94, 91, 97)),
            PetaKelas(2, "X RPL 2", "RPL", listOf(90, 85, 72, 96, 94, 91, 88, 95, null, 93)),
            PetaKelas(3, "XI TKJ 1", "TKJ", listOf(100, 97, 96, 95, 93, 94, 92, 90, 85, 78)),
        ),
    ),
    kelasBelum = listOf(KelasBelum(5, "XI TKJ 2", "TKJ", 34)),
    kelasTerendah = listOf(
        KelasTerendah(3, "XI TKJ 1", "TKJ", 35, 78, 27, 35),
        KelasTerendah(2, "X RPL 2", "RPL", 34, 85, 29, 34),
    ),
    sholat = SholatDashboard(
        dhuhur = SholatRingkas(780, 20, 15, 97, 30),
        ashar = SholatRingkas(760, 40, 12, 95, 28),
        kelasTotal = 30,
    ),
    jurnalHari = JurnalHari(
        total = 150, terisi = 120, terlewat = 12, berjalan = 18, persen = 91,
        daftarTerlewat = listOf(SesiTerlewatHari("Budi Santoso, S.Pd", "XI RPL 1", "Matematika", 3, 4, "08:30", "10:00")),
    ),
    kepatuhan = KepatuhanDashboard(
        ringkasan = RingkasanKepatuhan(52, 7, 980, 900, 80, 92, 40, 8, 4),
        mingguan = listOf(MingguKepatuhan("3 Nov", 480, 450, 94)),
        terendah = listOf(
            GuruKepatuhan(9, "Siti Aminah, S.Kom", "198501012010012001", 12, 84, 50, 34, 60, "kurang", "2026-10-28"),
            GuruKepatuhan(14, "Ahmad Fauzi, S.Pd", null, 8, 56, 42, 14, 75, "cukup", "2026-11-09"),
        ),
    ),
    pelanggaran = PelanggaranDashboard(
        mingguan = listOf(5, 8, 6, 12, 9, 7, 10, 4).mapIndexed { i, n -> MingguPelanggaran("${i + 1} Sep", n) },
        jenisTeratas = listOf(JenisTeratas("Terlambat", 9, 18), JenisTeratas("Atribut tidak lengkap", 5, 10)),
    ),
    sp = SpDashboard(
        menunggu = listOf(SpMenunggu("Rizki Pratama", "X RPL 2", "SP1", 38, "2026-11-09")),
        mendekati = listOf(SpMendekati("Dewi Lestari", "XI TKJ 1", 63, 7, "SP2")),
        poinTertinggi = listOf(PoinTertinggi("Rizki Pratama", "X RPL 2", 38)),
    ),
    jurusan = listOf(
        JurusanRingkas("RPL", 9, 250, 230, 240, 96, 5),
        JurusanRingkas("TKJ", 9, 255, 220, 245, 90, 8),
    ),
    peringatan = listOf(
        PeringatanDashboard("danger", "1 kelas belum diabsen", "XI TKJ 2", null),
        PeringatanDashboard("warn", "4 guru berkepatuhan jurnal kurang", "Di bawah 75% pada bulan ini.", "/pimpinan/jurnal"),
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 380, heightDp = 3200)
@Composable
private fun PreviewDashboardPimpinan() {
    DashboardPimpinanIsi(data = contohDashboard(), onBukaKepatuhan = {})
}