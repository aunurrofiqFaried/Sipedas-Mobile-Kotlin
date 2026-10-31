package com.sipedas.app.ui.kalender

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
import com.sipedas.app.data.model.JadwalMingguDto
import com.sipedas.app.data.model.JadwalSayaResponse
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import com.sipedas.app.data.model.PiketMingguDto
import com.sipedas.app.ui.AppEvents
import kotlinx.coroutines.coroutineScope
import com.sipedas.app.data.model.AcaraDto
import kotlinx.coroutines.async

enum class ModeKalender { TANGGAL, MINGGU }

sealed interface KalenderUiState {
    data object Loading : KalenderUiState
    data class Error(val message: String) : KalenderUiState
    data class Success(
        val data: JadwalSayaResponse,
        val mulai: LocalDate,
        val selesai: LocalDate,
    ) : KalenderUiState
}

data class Acara(
    val dto: AcaraDto,
    val mulai: LocalDate,
    val selesai: LocalDate,
)
class KalenderViewModel(private val api: ApiService) : ViewModel() {

    var acara by mutableStateOf<List<Acara>>(emptyList())
        private set

    var state by mutableStateOf<KalenderUiState>(KalenderUiState.Loading)
        private set

    var bulan by mutableStateOf(YearMonth.now())
        private set

    var tanggalDipilih by mutableStateOf<LocalDate?>(LocalDate.now())
        private set

    var mode by mutableStateOf(ModeKalender.TANGGAL)
        private set

    init {
        muat()
        // Hari berganti: pindah ke tanggal hari ini (kalau masih di dalam tahun ajaran)
        viewModelScope.launch {
            AppEvents.tanggalBerganti.collect { hariIni ->
                val s = state as? KalenderUiState.Success ?: return@collect

                if (hariIni >= s.mulai && hariIni <= s.selesai) {
                    aturBulan(YearMonth.from(hariIni))
                }
            }
        }
    }

    fun muat() {
        viewModelScope.launch {
            state = KalenderUiState.Loading

            runCatching {
                coroutineScope {
                    val jadwal = async { api.jadwalSaya() }
                    // Kegagalan memuat kalender akademik tidak boleh menggagalkan seluruh kalender
                    val kalender = async { runCatching { api.kalenderAkademik().data }.getOrDefault(emptyList()) }
                    jadwal.await() to kalender.await()
                }
            }
                .onSuccess { (res, daftarAcara) ->
                    val mulai = LocalDate.parse(res.tahunAjaran.tanggalMulai)
                    val selesai = LocalDate.parse(res.tahunAjaran.tanggalSelesai)

                    acara = daftarAcara.map {
                        Acara(it, LocalDate.parse(it.tanggalMulai), LocalDate.parse(it.tanggalSelesai))
                    }

                    state = KalenderUiState.Success(res, mulai, selesai)
                    aturBulan(YearMonth.now().coerceIn(YearMonth.from(mulai), YearMonth.from(selesai)))
                }
                .onFailure { state = KalenderUiState.Error(it.toUserMessage()) }
        }
    }

    private fun aturBulan(b: YearMonth) {
        bulan = b
        tanggalDipilih = if (b == YearMonth.now()) LocalDate.now() else b.atDay(1)
    }

    fun bisaMundur(): Boolean {
        val s = state as? KalenderUiState.Success ?: return false
        return bulan > YearMonth.from(s.mulai)
    }

    fun bisaMaju(): Boolean {
        val s = state as? KalenderUiState.Success ?: return false
        return bulan < YearMonth.from(s.selesai)
    }

    fun geserBulan(delta: Long) {
        if (delta < 0 && !bisaMundur()) return
        if (delta > 0 && !bisaMaju()) return

        aturBulan(bulan.plusMonths(delta))
    }

    fun pilihTanggal(tanggal: LocalDate) {
        tanggalDipilih = tanggal
    }

    fun ubahMode(baru: ModeKalender) {
        mode = baru
    }

    // Sesi mengajar pada tanggal tertentu: diturunkan dari pola jadwal mingguan
    fun sesiPada(tanggal: LocalDate): List<JadwalMingguDto> {
        if (liburPada(tanggal) != null) return emptyList()
        val s = state as? KalenderUiState.Success ?: return emptyList()

        if (tanggal < s.mulai || tanggal > s.selesai) return emptyList()

        return s.data.data
            .filter { it.hari == tanggal.dayOfWeek.value }
            .sortedBy { it.jamKeMulai }
    }

    // Piket pada tanggal tertentu: diturunkan dari pola piket mingguan
    fun piketPada(tanggal: LocalDate): List<PiketMingguDto> {
        if (liburPada(tanggal) != null) return emptyList()
        val s = state as? KalenderUiState.Success ?: return emptyList()

        if (tanggal < s.mulai || tanggal > s.selesai) return emptyList()

        return s.data.piket.orEmpty()
            .filter { it.hari == tanggal.dayOfWeek.value }
            .sortedBy { it.jamKeMulai }
    }

    fun acaraPada(tanggal: LocalDate): List<Acara> =
        acara.filter { tanggal >= it.mulai && tanggal <= it.selesai }

    fun liburPada(tanggal: LocalDate): Acara? =
        acaraPada(tanggal).firstOrNull { it.dto.jenis == "libur" }

    fun peringatanPada(tanggal: LocalDate): List<Acara> =
        acaraPada(tanggal).filter { it.dto.jenis == "peringatan" }

    companion object {
        val Factory = viewModelFactory {
            initializer { KalenderViewModel(ApiClient.service) }
        }
    }
}