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
import com.sipedas.app.data.model.RiwayatPelanggaranResponse
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class PoinSiswaViewModel(
    private val api: ApiService,
    private val siswaId: Int,
) : ViewModel() {

    var loading by mutableStateOf(true)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var data by mutableStateOf<RiwayatPelanggaranResponse?>(null)
        private set

    init {
        muat()

        // Segarkan otomatis setelah pelanggaran baru dicatat
        viewModelScope.launch {
            PoinEvents.berubah.drop(1).collect { ambil() }
        }
    }

    fun muat() {
        viewModelScope.launch {
            if (data == null) loading = true
            ambil()
            loading = false
        }
    }

    private suspend fun ambil() {
        runCatching { api.riwayatPelanggaran(siswaId) }
            .onSuccess {
                data = it
                error = null
            }
            .onFailure { error = it.toUserMessage() }
    }

    companion object {
        fun factory(siswaId: Int) = viewModelFactory {
            initializer { PoinSiswaViewModel(ApiClient.service, siswaId) }
        }
    }
}