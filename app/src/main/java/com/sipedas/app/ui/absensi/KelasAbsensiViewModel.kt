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
import com.sipedas.app.data.model.KelasHariIniResponse
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.drop
import com.sipedas.app.ui.AppEvents

sealed interface KelasUiState {
    data object Loading : KelasUiState
    data class Error(val message: String) : KelasUiState
    data class Success(val data: KelasHariIniResponse) : KelasUiState
}

class KelasAbsensiViewModel(private val api: ApiService) : ViewModel() {

    var state by mutableStateOf<KelasUiState>(KelasUiState.Loading)
        private set

    var refreshing by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch { ambil() }

        // Muat ulang otomatis kalau absensi disimpan dari layar lain
        viewModelScope.launch {
            AbsensiEvents.berubah.drop(1).collect { ambil() }
        }
        viewModelScope.launch { AppEvents.perluSegar.collect { ambil() } }
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
        state = KelasUiState.Loading
        viewModelScope.launch { ambil() }
    }

    private suspend fun ambil() {
        runCatching { api.kelasHariIni() }
            .onSuccess { state = KelasUiState.Success(it) }
            .onFailure { state = KelasUiState.Error(it.toUserMessage()) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { KelasAbsensiViewModel(ApiClient.service) }
        }
    }
}