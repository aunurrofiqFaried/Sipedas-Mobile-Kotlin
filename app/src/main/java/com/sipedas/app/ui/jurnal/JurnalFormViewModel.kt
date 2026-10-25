package com.sipedas.app.ui.jurnal

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
import com.sipedas.app.data.model.JadwalDto
import com.sipedas.app.data.model.SimpanJurnalRequest
import kotlinx.coroutines.launch

class JurnalFormViewModel(
    private val api: ApiService,
    private val jadwalId: Int,
) : ViewModel() {

    var loading by mutableStateOf(true)
        private set

    var loadError by mutableStateOf<String?>(null)
        private set

    var jadwal by mutableStateOf<JadwalDto?>(null)
        private set

    var materi by mutableStateOf("")
        private set

    var catatan by mutableStateOf("")
        private set

    var saving by mutableStateOf(false)
        private set

    var saveError by mutableStateOf<String?>(null)
        private set

    // Menjadi true setelah berhasil menyimpan; layar lalu kembali otomatis
    var selesai by mutableStateOf(false)
        private set

    init {
        muat()
    }

    fun muat() {
        viewModelScope.launch {
            loading = true
            loadError = null

            runCatching {
                val hariIni = api.jadwalHariIni()
                val j = hariIni.data.firstOrNull { it.id == jadwalId }
                    ?: error("Jadwal tidak ditemukan untuk hari ini.")

                // Kalau jurnal hari ini sudah ada, isi formulir dengan isinya (untuk diubah)
                val sudah = if (j.sudahJurnal) {
                    api.riwayatJurnal().data.firstOrNull {
                        it.jadwalId == jadwalId && it.tanggal.startsWith(hariIni.tanggal)
                    }
                } else {
                    null
                }

                Triple(j, sudah?.materi.orEmpty(), sudah?.catatan.orEmpty())
            }
                .onSuccess { (j, m, c) ->
                    jadwal = j
                    materi = m
                    catatan = c
                }
                .onFailure { loadError = it.toUserMessage() }

            loading = false
        }
    }

    fun onMateriChange(value: String) {
        if (value.length <= BATAS) materi = value
        saveError = null
    }

    fun onCatatanChange(value: String) {
        if (value.length <= BATAS) catatan = value
        saveError = null
    }

    fun simpan() {
        if (saving) return

        if (materi.trim().length < 5) {
            saveError = "Materi pembelajaran wajib diisi (minimal 5 karakter)."
            return
        }

        viewModelScope.launch {
            saving = true
            saveError = null

            runCatching {
                api.simpanJurnal(
                    SimpanJurnalRequest(
                        jadwalId = jadwalId,
                        materi = materi.trim(),
                        catatan = catatan.trim().ifEmpty { null },
                    )
                )
            }
                .onSuccess {
                    JurnalEvents.berubah.value += 1
                    selesai = true
                }
                .onFailure { saveError = it.toUserMessage() }

            saving = false
        }
    }

    companion object {
        const val BATAS = 2000

        fun factory(jadwalId: Int) = viewModelFactory {
            initializer { JurnalFormViewModel(ApiClient.service, jadwalId) }
        }
    }
}