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
import com.sipedas.app.data.model.JurnalDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

data class JurnalUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val tanggal: String? = null,
    val hariIni: List<JadwalDto> = emptyList(),
    val riwayat: List<JurnalDto> = emptyList(),
)

class JurnalViewModel(private val api: ApiService) : ViewModel() {

    var state by mutableStateOf(JurnalUiState())
        private set

    var refreshing by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch { ambil() }
        viewModelScope.launch { JurnalEvents.berubah.drop(1).collect { ambil() } }
    }

    fun muatUlang() {
        if (refreshing) return

        viewModelScope.launch {
            refreshing = true
            ambil()
            refreshing = false
        }
    }

    fun cobaLagi() {
        state = state.copy(loading = true, error = null)
        viewModelScope.launch { ambil() }
    }

    private suspend fun ambil() {
        // Dua permintaan berjalan bersamaan
        val (jadwal, riwayat) = coroutineScope {
            val a = async { runCatching { api.jadwalHariIni() } }
            val b = async { runCatching { api.riwayatJurnal() } }
            a.await() to b.await()
        }

        state = state.copy(
            loading = false,
            error = (jadwal.exceptionOrNull() ?: riwayat.exceptionOrNull())?.toUserMessage(),
            tanggal = jadwal.getOrNull()?.tanggal ?: state.tanggal,
            hariIni = jadwal.getOrNull()?.data ?: state.hariIni,
            riwayat = riwayat.getOrNull()?.data ?: state.riwayat,
        )
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { JurnalViewModel(ApiClient.service) }
        }
    }
}