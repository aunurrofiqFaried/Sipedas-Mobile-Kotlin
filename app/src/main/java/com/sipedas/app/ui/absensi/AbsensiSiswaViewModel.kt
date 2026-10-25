package com.sipedas.app.ui.absensi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sipedas.app.data.api.ApiClient
import com.sipedas.app.data.api.ApiService
import com.sipedas.app.data.api.toUserMessage
import com.sipedas.app.data.model.ItemAbsen
import com.sipedas.app.data.model.SimpanAbsensiRequest
import kotlinx.coroutines.launch

data class SiswaItem(
    val id: Int,
    val nis: String,
    val nama: String,
    val status: String?,        // pilihan guru saat ini (belum tentu tersimpan)
    val statusServer: String?,  // status yang sudah tersimpan di server
    val keterangan: String?,
    val waLink: String?,
    val adaNomorWali: Boolean,
)

data class PesanAbsensi(val teks: String, val sukses: Boolean)

class AbsensiSiswaViewModel(
    private val api: ApiService,
    private val kelasId: Int,
) : ViewModel() {

    var loading by mutableStateOf(true)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var kelasNama by mutableStateOf("")
        private set

    var siswa by mutableStateOf<List<SiswaItem>>(emptyList())
        private set

    var saving by mutableStateOf(false)
        private set

    var pesan by mutableStateOf<PesanAbsensi?>(null)
        private set

    init {
        muat()
    }

    fun muat() {
        viewModelScope.launch {
            if (siswa.isEmpty()) loading = true
            error = null

            runCatching { api.siswaKelas(kelasId) }
                .onSuccess { res ->
                    kelasNama = res.kelas.nama
                    siswa = res.data.map {
                        SiswaItem(
                            id = it.id,
                            nis = it.nis,
                            nama = it.nama,
                            status = it.status,
                            statusServer = it.status,
                            keterangan = it.keterangan,
                            waLink = it.waLink,
                            adaNomorWali = it.adaNomorWali,
                        )
                    }
                }
                .onFailure { error = it.toUserMessage() }

            loading = false
        }
    }

    fun setStatus(id: Int, status: String) {
        siswa = siswa.map { if (it.id == id) it.copy(status = status) else it }
        pesan = null
    }

    fun semuaHadir() {
        siswa = siswa.map { it.copy(status = "hadir") }
        pesan = null
    }

    fun simpan() {
        if (saving || siswa.isEmpty() || siswa.any { it.status == null }) return

        viewModelScope.launch {
            saving = true
            pesan = null

            val request = SimpanAbsensiRequest(
                kelasId = kelasId,
                items = siswa.map { ItemAbsen(it.id, it.status!!, it.keterangan) },
            )

            runCatching { api.simpanAbsensi(request) }
                .onSuccess {
                    AbsensiEvents.berubah.value += 1
                    pesan = PesanAbsensi("✓ Absensi tersimpan.", sukses = true)
                    muat() // ambil ulang agar tautan WhatsApp untuk yang tidak hadir terisi
                }
                .onFailure { pesan = PesanAbsensi(it.toUserMessage(), sukses = false) }

            saving = false
        }
    }

    companion object {
        fun factory(kelasId: Int) = viewModelFactory {
            initializer { AbsensiSiswaViewModel(ApiClient.service, kelasId) }
        }
    }
}