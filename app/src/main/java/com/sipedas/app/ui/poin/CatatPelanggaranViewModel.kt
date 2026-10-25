package com.sipedas.app.ui.poin

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
import com.sipedas.app.data.model.CatatPelanggaranRequest
import com.sipedas.app.data.model.CatatPelanggaranResponse
import com.sipedas.app.data.model.JenisPelanggaranDto
import kotlinx.coroutines.launch

class CatatPelanggaranViewModel(
    private val api: ApiService,
    private val siswaId: Int,
) : ViewModel() {

    var loading by mutableStateOf(true)
        private set

    var loadError by mutableStateOf<String?>(null)
        private set

    var jenis by mutableStateOf<List<JenisPelanggaranDto>>(emptyList())
        private set

    var terpilih by mutableStateOf<Int?>(null)
        private set

    var keterangan by mutableStateOf("")
        private set

    var saving by mutableStateOf(false)
        private set

    var saveError by mutableStateOf<String?>(null)
        private set

    // Terisi setelah berhasil menyimpan
    var hasil by mutableStateOf<CatatPelanggaranResponse?>(null)
        private set

    val jenisTerpilih: JenisPelanggaranDto?
        get() = jenis.firstOrNull { it.id == terpilih }

    init {
        muat()
    }

    fun muat() {
        viewModelScope.launch {
            loading = true
            loadError = null

            runCatching { api.jenisPelanggaran() }
                .onSuccess { jenis = it.data }
                .onFailure { loadError = it.toUserMessage() }

            loading = false
        }
    }

    fun pilih(id: Int) {
        terpilih = id
        saveError = null
    }

    fun onKeterangan(value: String) {
        if (value.length <= 1000) keterangan = value
    }

    fun simpan() {
        val id = terpilih ?: return
        if (saving) return

        viewModelScope.launch {
            saving = true
            saveError = null

            runCatching {
                api.catatPelanggaran(
                    CatatPelanggaranRequest(
                        siswaId = siswaId,
                        jenisPelanggaranId = id,
                        keterangan = keterangan.trim().ifEmpty { null },
                    )
                )
            }
                .onSuccess {
                    hasil = it
                    PoinEvents.berubah.value += 1
                }
                .onFailure { saveError = it.toUserMessage() }

            saving = false
        }
    }

    companion object {
        fun factory(siswaId: Int) = viewModelFactory {
            initializer { CatatPelanggaranViewModel(ApiClient.service, siswaId) }
        }
    }
}