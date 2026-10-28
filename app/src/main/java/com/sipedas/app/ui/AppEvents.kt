package com.sipedas.app.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import java.time.LocalDate

object AppEvents {

    // Naik setiap kali aplikasi kembali ke layar depan (dipanggil dari MainActivity.onStart)
    val kembaliAktif = MutableStateFlow(0)

    // Memancarkan tanggal baru ketika hari berganti (dicek setiap menit)
    val tanggalBerganti: Flow<LocalDate> = flow {
        var terakhir = LocalDate.now()

        while (true) {
            delay(60_000)
            val sekarang = LocalDate.now()

            if (sekarang != terakhir) {
                terakhir = sekarang
                emit(sekarang)
            }
        }
    }

    // Saatnya memuat ulang data "hari ini": aplikasi dibuka kembali atau hari berganti
    val perluSegar: Flow<Unit> = merge(
        kembaliAktif.drop(1).map { },
        tanggalBerganti.map { },
    )
}