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
import com.sipedas.app.data.model.CatatKontakRequest
import com.sipedas.app.data.model.KontakWaliResponse
import kotlinx.coroutines.launch

class KontakWaliViewModel(
    private val api: ApiService,
    private val siswaId: Int,
) : ViewModel() {

    var loading by mutableStateOf(true)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var data by mutableStateOf<KontakWaliResponse?>(null)
        private set

    var tujuan by mutableStateOf("konseling")
        private set

    var catatan by mutableStateOf("")
        private set

    var sending by mutableStateOf(false)
        private set

    var pesan by mutableStateOf<String?>(null)
        private set

    init {
        muat()
    }

    fun muat() {
        viewModelScope.launch {
            if (data == null) loading = true
            ambil()
            loading = false
        }
    }

    private suspend fun ambil() {
        runCatching { api.kontakWali(siswaId) }
            .onSuccess {
                data = it
                error = null
            }
            .onFailure { error = it.toUserMessage() }
    }

    fun pilihTujuan(value: String) {
        tujuan = value
    }

    fun onCatatan(value: String) {
        if (value.length <= 1000) catatan = value
    }

    // Catat riwayat dulu, lalu buka WhatsApp. WhatsApp tetap dibuka walau pencatatan gagal.
    fun hubungi(onBuka: (String) -> Unit) {
        val link = data?.waLink ?: return
        if (sending) return

        viewModelScope.launch {
            sending = true
            pesan = null

            runCatching {
                api.catatKontakWali(
                    siswaId,
                    CatatKontakRequest(tujuan, catatan.trim().ifEmpty { null }),
                )
            }
                .onSuccess {
                    catatan = ""
                    ambil()
                }
                .onFailure { pesan = "Riwayat kontak gagal tercatat: ${it.toUserMessage()}" }

            sending = false
            onBuka(link)
        }
    }

    companion object {
        fun factory(siswaId: Int) = viewModelFactory {
            initializer { KontakWaliViewModel(ApiClient.service, siswaId) }
        }
    }
}