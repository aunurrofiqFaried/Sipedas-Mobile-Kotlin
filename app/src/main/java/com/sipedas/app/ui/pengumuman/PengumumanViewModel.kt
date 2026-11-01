package com.sipedas.app.ui.pengumuman

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
import com.sipedas.app.data.model.PengumumanDto
import com.sipedas.app.ui.AppEvents
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PengumumanViewModel(private val api: ApiService) : ViewModel() {

    var daftar by mutableStateOf<List<PengumumanDto>>(emptyList())
        private set

    var belumDibaca by mutableStateOf(0)
        private set

    // Id terakhir yang sudah dibaca SAAT panel dibuka; yang lebih besar ditandai "Baru"
    var batasBaru by mutableStateOf(0)
        private set

    var panelBuka by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    private var terakhirDibaca = 0

    init {
        viewModelScope.launch { muat() }
        viewModelScope.launch { AppEvents.perluSegar.collect { muat() } }

        // Pengganti notifikasi push: cek pengumuman baru setiap menit selama aplikasi hidup
        viewModelScope.launch {
            while (true) {
                delay(INTERVAL_CEK)
                muat()
            }
        }
    }

    private suspend fun muat() {
        runCatching { api.pengumuman() }
            .onSuccess { res ->
                daftar = res.data
                belumDibaca = res.belumDibaca
                terakhirDibaca = res.terakhirDibacaId
                error = null

                // Panel sedang terbuka: yang baru masuk langsung dianggap terbaca
                if (panelBuka && res.belumDibaca > 0) tandaiDibaca()
            }
            .onFailure { error = it.toUserMessage() }
    }

    private suspend fun tandaiDibaca() {
        runCatching { api.pengumumanDibaca() }
            .onSuccess {
                belumDibaca = 0
                terakhirDibaca = daftar.maxOfOrNull { it.id } ?: terakhirDibaca
            }
    }

    fun bukaPanel() {
        batasBaru = terakhirDibaca
        panelBuka = true

        viewModelScope.launch {
            muat() // ambil yang terbaru; kalau ada yang belum dibaca, ditandai di dalam muat()

            if (belumDibaca > 0) tandaiDibaca()
        }
    }

    fun tutupPanel() {
        panelBuka = false
    }

    companion object {
        private const val INTERVAL_CEK = 60_000L // 1 menit

        val Factory = viewModelFactory {
            initializer { PengumumanViewModel(ApiClient.service) }
        }
    }
}