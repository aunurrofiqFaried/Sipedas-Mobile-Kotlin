package com.sipedas.app.ui.sholat

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
import com.sipedas.app.data.model.KelasSholatResponse
import com.sipedas.app.ui.absensi.AbsensiEvents
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import com.sipedas.app.ui.AppEvents

sealed interface SholatKelasUiState {
    data object Loading : SholatKelasUiState
    data class Error(val message: String) : SholatKelasUiState
    data class Success(val data: KelasSholatResponse) : SholatKelasUiState
}

class SholatKelasViewModel(private val api: ApiService) : ViewModel() {

    var state by mutableStateOf<SholatKelasUiState>(SholatKelasUiState.Loading)
        private set

    var refreshing by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch { ambil() }
        viewModelScope.launch { AppEvents.perluSegar.collect { ambil() } }

        // Segar otomatis setelah sholat disimpan, atau absensi harian disimpan
        // (absensi harian yang baru membuka kunci tombol sholat)
        viewModelScope.launch { SholatEvents.berubah.drop(1).collect { ambil() } }
        viewModelScope.launch { AbsensiEvents.berubah.drop(1).collect { ambil() } }
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
        state = SholatKelasUiState.Loading
        viewModelScope.launch { ambil() }
    }

    private suspend fun ambil() {
        runCatching { api.kelasSholat() }
            .onSuccess { state = SholatKelasUiState.Success(it) }
            .onFailure { state = SholatKelasUiState.Error(it.toUserMessage()) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { SholatKelasViewModel(ApiClient.service) }
        }
    }
}