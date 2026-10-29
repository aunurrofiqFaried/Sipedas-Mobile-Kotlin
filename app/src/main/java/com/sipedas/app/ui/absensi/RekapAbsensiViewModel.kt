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
import com.sipedas.app.data.model.KelasRekapDto
import com.sipedas.app.data.model.RekapAbsensiResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.YearMonth

class RekapAbsensiViewModel(private val api: ApiService) : ViewModel() {

    var kelasLoading by mutableStateOf(true)
        private set

    var kelasError by mutableStateOf<String?>(null)
        private set

    var daftarKelas by mutableStateOf<List<KelasRekapDto>>(emptyList())
        private set

    var kelasId by mutableStateOf<Int?>(null)
        private set

    var bulan by mutableStateOf(YearMonth.now())
        private set

    var semuaTahun by mutableStateOf(false)
        private set

    var hasil by mutableStateOf<RekapAbsensiResponse?>(null)
        private set

    var memuat by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    private var job: Job? = null

    val kelasNama: String?
        get() = daftarKelas.firstOrNull { it.id == kelasId }?.nama

    init {
        muatKelas()
    }

    fun muatKelas() {
        viewModelScope.launch {
            kelasLoading = true
            kelasError = null

            runCatching { api.kelasRekap() }
                .onSuccess { daftarKelas = it.data }
                .onFailure { kelasError = it.toUserMessage() }

            kelasLoading = false
        }
    }

    fun pilihKelas(id: Int) {
        kelasId = id
        muatRekap()
    }

    // Tidak ada data masa depan, dan mundur dibatasi 12 bulan
    fun bisaMaju(): Boolean = bulan < YearMonth.now()
    fun bisaMundur(): Boolean = bulan > YearMonth.now().minusMonths(12)

    fun geserBulan(delta: Long) {
        if (delta > 0 && !bisaMaju()) return
        if (delta < 0 && !bisaMundur()) return

        bulan = bulan.plusMonths(delta)
        muatRekap()
    }

    fun ubahSemuaTahun(nilai: Boolean) {
        semuaTahun = nilai
        muatRekap()
    }

    fun muatRekap() {
        val id = kelasId ?: return

        // Membatalkan permintaan sebelumnya supaya hasil yang tampil selalu yang terakhir dipilih
        job?.cancel()

        job = viewModelScope.launch {
            memuat = true
            error = null

            val parameter = if (semuaTahun) "semua" else bulan.toString() // contoh: "2026-10"

            runCatching { api.rekapAbsensi(id, parameter) }
                .onSuccess { hasil = it }
                .onFailure {
                    if (it is CancellationException) throw it
                    error = it.toUserMessage()
                    hasil = null
                }

            memuat = false
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { RekapAbsensiViewModel(ApiClient.service) }
        }
    }
}