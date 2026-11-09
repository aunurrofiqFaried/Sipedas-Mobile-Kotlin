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
import com.sipedas.app.data.model.BeriRemisiRequest
import com.sipedas.app.data.model.JenisRemisiDto
import com.sipedas.app.ui.notifikasi.Notifikasi
import kotlinx.coroutines.launch

class BeriRemisiViewModel(
    private val api: ApiService,
    private val siswaId: Int,
) : ViewModel() {

    var loading by mutableStateOf(true)
        private set

    var loadError by mutableStateOf<String?>(null)
        private set

    var jenis by mutableStateOf<List<JenisRemisiDto>>(emptyList())
        private set

    var terpilih by mutableStateOf<Int?>(null)
        private set

    var keterangan by mutableStateOf("")
        private set

    var saving by mutableStateOf(false)
        private set

    var saveError by mutableStateOf<String?>(null)
        private set

    // Menjadi true setelah berhasil; layar lalu kembali otomatis
    var selesai by mutableStateOf(false)
        private set

    val jenisTerpilih: JenisRemisiDto?
        get() = jenis.firstOrNull { it.id == terpilih }

    init {
        muat()
    }

    fun muat() {
        viewModelScope.launch {
            loading = true
            loadError = null

            runCatching { api.jenisRemisi() }
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
                api.beriRemisi(
                    BeriRemisiRequest(
                        siswaId = siswaId,
                        jenisRemisiId = id,
                        keterangan = keterangan.trim().ifEmpty { null },
                    )
                )
            }
                .onSuccess {
                    PoinEvents.berubah.value += 1
                    Notifikasi.sukses(
                        "Remisi diberikan",
                        "−${it.poinDikurangi} poin · poin aktif sekarang ${it.totalPoinSekarang}",
                    )
                    selesai = true
                }
                .onFailure { saveError = it.toUserMessage() }

            saving = false
        }
    }

    companion object {
        fun factory(siswaId: Int) = viewModelFactory {
            initializer { BeriRemisiViewModel(ApiClient.service, siswaId) }
        }
    }
}