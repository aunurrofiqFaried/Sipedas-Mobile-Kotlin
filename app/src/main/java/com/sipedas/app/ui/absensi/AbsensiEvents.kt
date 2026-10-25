package com.sipedas.app.ui.absensi

import kotlinx.coroutines.flow.MutableStateFlow

// Penanda sederhana: angkanya naik setiap kali absensi berhasil disimpan
object AbsensiEvents {
    val berubah = MutableStateFlow(0)
}