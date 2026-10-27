package com.sipedas.app.ui.sholat

import kotlinx.coroutines.flow.MutableStateFlow

// Angkanya naik setiap kali absensi sholat berhasil disimpan
object SholatEvents {
    val berubah = MutableStateFlow(0)
}

fun labelWaktu(waktu: String): String = when (waktu) {
    "dhuhur" -> "Dhuhur"
    "ashar" -> "Ashar"
    else -> waktu
}