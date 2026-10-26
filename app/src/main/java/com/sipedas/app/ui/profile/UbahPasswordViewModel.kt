package com.sipedas.app.ui.profile

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
import com.sipedas.app.data.model.UbahPasswordRequest
import kotlinx.coroutines.launch

class UbahPasswordViewModel(private val api: ApiService) : ViewModel() {

    var lama by mutableStateOf("")
        private set

    var baru by mutableStateOf("")
        private set

    var konfirmasi by mutableStateOf("")
        private set

    var saving by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    // Menjadi true setelah berhasil; layar lalu kembali otomatis
    var selesai by mutableStateOf(false)
        private set

    fun onLama(v: String) { lama = v; error = null }
    fun onBaru(v: String) { baru = v; error = null }
    fun onKonfirmasi(v: String) { konfirmasi = v; error = null }

    // Aturan sama dengan di server, supaya kesalahan ketahuan tanpa perlu menunggu jaringan
    private fun validasi(): String? = when {
        lama.isBlank() -> "Password lama wajib diisi."
        baru.length < 8 -> "Password baru minimal 8 karakter."
        baru.none { it.isLetter() } || baru.none { it.isDigit() } ->
            "Password baru harus mengandung huruf dan angka."
        baru == lama -> "Password baru harus berbeda dari password lama."
        baru != konfirmasi -> "Konfirmasi password tidak sama."
        else -> null
    }

    fun simpan() {
        if (saving) return

        validasi()?.let {
            error = it
            return
        }

        viewModelScope.launch {
            saving = true
            error = null

            runCatching { api.ubahPassword(UbahPasswordRequest(lama, baru, konfirmasi)) }
                .onSuccess {
                    // Kosongkan isian dari memori begitu berhasil
                    lama = ""
                    baru = ""
                    konfirmasi = ""
                    selesai = true
                }
                .onFailure { error = it.toUserMessage() }

            saving = false
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { UbahPasswordViewModel(ApiClient.service) }
        }
    }
}