package com.sipedas.app.ui.pimpinan

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
import com.sipedas.app.data.model.DashboardPimpinan
import com.sipedas.app.ui.AppEvents
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface PimpinanState {
    data object Loading : PimpinanState
    data object Unauthorized : PimpinanState
    data class Error(val message: String) : PimpinanState
    data class Success(val data: DashboardPimpinan) : PimpinanState
}

class PimpinanViewModel(private val api: ApiService) : ViewModel() {

    var state by mutableStateOf<PimpinanState>(PimpinanState.Loading)
        private set

    var refreshing by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch { ambil() }
        viewModelScope.launch { AppEvents.perluSegar.collect { ambil() } }

        // Segarkan otomatis tiap 60 detik. Kegagalan di sini diam saja
        // supaya data yang sudah tampil tidak diganti layar error.
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                ambil(diam = true)
            }
        }
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
        state = PimpinanState.Loading
        viewModelScope.launch { ambil() }
    }

    private suspend fun ambil(diam: Boolean = false) {
        runCatching { api.dashboardPimpinan() }
            .onSuccess { state = PimpinanState.Success(it) }
            .onFailure { e ->
                if (e is HttpException && e.code() == 401) {
                    state = PimpinanState.Unauthorized
                } else if (!diam || state !is PimpinanState.Success) {
                    state = PimpinanState.Error(e.toUserMessage())
                }
            }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { PimpinanViewModel(ApiClient.service) }
        }
    }
}