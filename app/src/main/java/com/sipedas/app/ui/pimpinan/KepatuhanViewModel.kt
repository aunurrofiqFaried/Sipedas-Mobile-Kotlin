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
import com.sipedas.app.data.model.KepatuhanJurnalResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate

enum class PeriodeKepatuhanPilihan(val label: String) {
    BULAN_INI("Bulan ini"),
    BULAN_LALU("Bulan lalu"),
    TUJUH_HARI("7 hari"),
    TIGA_PULUH_HARI("30 hari");

    // Pasangan (dari, sampai) dalam format YYYY-MM-DD
    fun rentang(): Pair<String, String> {
        val hariIni = LocalDate.now()

        val (dari, sampai) = when (this) {
            BULAN_INI -> hariIni.withDayOfMonth(1) to hariIni
            BULAN_LALU -> {
                val awal = hariIni.minusMonths(1).withDayOfMonth(1)
                awal to awal.withDayOfMonth(awal.lengthOfMonth())
            }
            TUJUH_HARI -> hariIni.minusDays(6) to hariIni
            TIGA_PULUH_HARI -> hariIni.minusDays(29) to hariIni
        }

        return dari.toString() to sampai.toString()
    }
}

sealed interface KepatuhanState {
    data object Loading : KepatuhanState
    data object Unauthorized : KepatuhanState
    data class Error(val message: String) : KepatuhanState
    data class Success(val data: KepatuhanJurnalResponse) : KepatuhanState
}

class KepatuhanViewModel(private val api: ApiService) : ViewModel() {

    var state by mutableStateOf<KepatuhanState>(KepatuhanState.Loading)
        private set

    var periode by mutableStateOf(PeriodeKepatuhanPilihan.BULAN_INI)
        private set

    var refreshing by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch { ambil() }
    }

    fun pilihPeriode(baru: PeriodeKepatuhanPilihan) {
        if (baru == periode) return

        periode = baru
        state = KepatuhanState.Loading
        viewModelScope.launch { ambil() }
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
        state = KepatuhanState.Loading
        viewModelScope.launch { ambil() }
    }

    private suspend fun ambil() {
        val (dari, sampai) = periode.rentang()

        runCatching { api.kepatuhanJurnal(dari, sampai) }
            .onSuccess { state = KepatuhanState.Success(it) }
            .onFailure { e ->
                state = if (e is HttpException && e.code() == 401) {
                    KepatuhanState.Unauthorized
                } else {
                    KepatuhanState.Error(e.toUserMessage())
                }
            }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { KepatuhanViewModel(ApiClient.service) }
        }
    }
}