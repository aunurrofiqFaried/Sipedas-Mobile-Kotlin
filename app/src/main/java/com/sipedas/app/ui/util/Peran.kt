package com.sipedas.app.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector
import com.sipedas.app.data.local.Session

// Peran yang melihat data seluruh sekolah
val PERAN_PIMPINAN = setOf("kepala_sekolah", "waka_kurikulum", "kesiswaan", "waka_sdm", "admin")

// Peran yang punya jadwal mengajar sendiri (beranda guru)
private val PERAN_MENGAJAR = setOf("guru", "guru_bk")

// Peran utama ditambah peran tambahan
val Session.semuaPeran: List<String>
    get() = roles.ifEmpty { listOf(role) }

fun Session.punyaPeran(vararg peran: String): Boolean = semuaPeran.any { it in peran }

val Session.adalahPimpinan: Boolean
    get() = semuaPeran.any { it in PERAN_PIMPINAN }

val Session.adalahKaprodi: Boolean
    get() = kaprodiKode != null

enum class Beranda(
    val labelTab: String,
    val judul: String,
    val keterangan: String,
    val ikon: ImageVector,
) {
    GURU("Beranda", "Beranda guru", "Jadwal, absensi, dan jurnal mengajar", Icons.Filled.Home),
    PIMPINAN("Pimpinan", "Beranda pimpinan", "Pantauan seluruh sekolah", Icons.Filled.Dashboard),
    KAPRODI("Kaprodi", "Beranda kaprodi", "Pantauan jurusan yang dipimpin", Icons.Filled.School),
}

// Beranda yang boleh dipakai pengguna ini
fun Session.daftarBeranda(): List<Beranda> = buildList {
    if (adalahPimpinan) {
        // Pimpinan murni hanya punya beranda pimpinan; yang juga mengajar punya keduanya
        if (semuaPeran.any { it in PERAN_MENGAJAR }) add(Beranda.GURU)
        add(Beranda.PIMPINAN)
    } else {
        add(Beranda.GURU)
        if (adalahKaprodi) add(Beranda.KAPRODI)
    }
}

// Beranda yang terbuka pertama kali: sesuai peran utama
fun Session.berandaAwal(): Beranda {
    val daftar = daftarBeranda()
    val diinginkan = if (role in PERAN_PIMPINAN) Beranda.PIMPINAN else Beranda.GURU

    return if (diinginkan in daftar) diinginkan else daftar.first()
}