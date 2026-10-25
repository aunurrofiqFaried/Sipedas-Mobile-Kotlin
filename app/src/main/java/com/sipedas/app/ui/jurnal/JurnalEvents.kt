package com.sipedas.app.ui.jurnal

import kotlinx.coroutines.flow.MutableStateFlow

// Angkanya naik setiap kali jurnal berhasil disimpan
object JurnalEvents {
    val berubah = MutableStateFlow(0)
}