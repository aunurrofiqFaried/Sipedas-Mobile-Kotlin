package com.sipedas.app.ui.util

import java.text.SimpleDateFormat
import java.util.Locale

// "2026-10-26T00:00:00.000000Z" atau "2026-10-26" -> "Sen, 26 Okt 2026"
fun formatTanggalPendek(iso: String): String = try {
    val masuk = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso.take(10))
    SimpleDateFormat("EEE, d MMM yyyy", Locale.forLanguageTag("id-ID")).format(masuk!!)
} catch (e: Exception) {
    iso.take(10)
}