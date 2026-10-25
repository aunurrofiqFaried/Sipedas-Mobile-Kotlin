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
import com.sipedas.app.data.model.SiswaCariDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class CariSiswaViewModel(private val api: ApiService) : ViewModel() {

    var query by mutableStateOf("")
        private set

    var hasil by mutableStateOf<List<SiswaCariDto>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    private val input = MutableStateFlow("")

    init {
        viewModelScope.launch {
            // Pencarian jalan 0,4 detik setelah guru berhenti mengetik
            input.debounce(400).distinctUntilChanged().collectLatest { q ->
                val teks = q.trim()

                if (teks.length < 2) {
                    hasil = emptyList()
                    error = null
                    loading = false
                    return@collectLatest
                }

                loading = true
                error = null

                runCatching { api.cariSiswa(teks) }
                    .onSuccess { hasil = it.data }
                    .onFailure {
                        if (it is CancellationException) throw it
                        error = it.toUserMessage()
                        hasil = emptyList()
                    }

                loading = false
            }
        }
    }

    fun onQueryChange(value: String) {
        query = value
        input.value = value
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { CariSiswaViewModel(ApiClient.service) }
        }
    }
}