package com.sipedas.app.ui.notifikasi

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class JenisNotif { SUKSES, PERINGATAN, GALAT, INFO }

data class NotifEvent(
    val jenis: JenisNotif,
    val judul: String,
    val pesan: String?,
)

// Dipanggil dari mana saja (termasuk ViewModel): Notifikasi.sukses("Judul", "Pesan")
object Notifikasi {
    private val _events = MutableSharedFlow<NotifEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<NotifEvent> = _events.asSharedFlow()

    fun sukses(judul: String, pesan: String? = null) {
        _events.tryEmit(NotifEvent(JenisNotif.SUKSES, judul, pesan))
    }

    fun peringatan(judul: String, pesan: String? = null) {
        _events.tryEmit(NotifEvent(JenisNotif.PERINGATAN, judul, pesan))
    }

    fun galat(judul: String, pesan: String? = null) {
        _events.tryEmit(NotifEvent(JenisNotif.GALAT, judul, pesan))
    }

    fun info(judul: String, pesan: String? = null) {
        _events.tryEmit(NotifEvent(JenisNotif.INFO, judul, pesan))
    }
}