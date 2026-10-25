package com.sipedas.app.ui.poin

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow

// Angkanya naik setiap kali pelanggaran berhasil dicatat
object PoinEvents {
    val berubah = MutableStateFlow(0)
}

// Warna menurut tingkat kategori pelanggaran
fun warnaKategori(kategori: String?): Color = when (kategori?.lowercase()) {
    "ringan" -> Color(0xFF16A34A)
    "sedang" -> Color(0xFFD97706)
    "berat" -> Color(0xFFDC2626)
    else -> Color(0xFF6B7280)
}

fun labelStatusSp(status: String): String = when (status) {
    "menunggu" -> "Menunggu persetujuan"
    "disetujui" -> "Disetujui"
    "ditolak" -> "Ditolak"
    else -> status
}