package com.sipedas.app.ui.jurnal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.sipedas.app.data.model.JurnalBulanDto
import com.sipedas.app.ui.AppEvents
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class JurnalKalenderViewModel(private val api: ApiService) : ViewModel() {

    var loading by mutableStateOf(true)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    // Pola jadwal mingguan + rentang tahun ajaran (dipakai menghitung sesi yang terlewat)
    var jadwal by mutableStateOf<JadwalSayaResponse?>(null)
        private set

    var mulai by mutableStateOf<LocalDate?>(null)
        private set

    var bulan by mutableStateOf(YearMonth.now())
        private set

    var tanggalDipilih by mutableStateOf<LocalDate?>(LocalDate.now())
        private set

    var bulanMemuat by mutableStateOf(false)
        private set

    var bulanError by mutableStateOf<String?>(null)
        private set

    // Jurnal per bulan yang sudah dimuat
    private val cache = mutableStateMapOf<YearMonth, List<JurnalBulanDto>>()

    init {
        muat()

        // Segar otomatis setelah jurnal disimpan atau aplikasi dibuka kembali
        viewModelScope.launch { JurnalEvents.berubah.drop(1).collect { muatBulan(bulan, paksa = true) } }
        viewModelScope.launch { AppEvents.perluSegar.collect { muatBulan(bulan, paksa = true) } }
    }

    fun muat() {
        viewModelScope.launch {
            loading = true
            error = null

            runCatching { api.jadwalSaya() }
                .onSuccess { res ->
                    jadwal = res
                    mulai = LocalDate.parse(res.tahunAjaran.tanggalMulai)

                    // Mulai dari bulan ini; kalau tahun ajaran belum mulai, dari bulan awalnya
                    aturBulan(YearMonth.now().coerceAtLeast(YearMonth.from(mulai)))
                }
                .onFailure { error = it.toUserMessage() }

            loading = false
        }
    }

    private fun aturBulan(b: YearMonth) {
        bulan = b
        tanggalDipilih = if (b == YearMonth.now()) LocalDate.now() else b.atDay(1)

        viewModelScope.launch { muatBulan(b) }
    }

    private suspend fun muatBulan(b: YearMonth, paksa: Boolean = false) {
        if (!paksa && cache.containsKey(b)) return

        bulanMemuat = true
        bulanError = null

        runCatching { api.jurnalBulan(b.toString()) }
            .onSuccess { cache[b] = it.data }
            .onFailure {
                if (it is CancellationException) throw it
                bulanError = it.toUserMessage()
            }

        bulanMemuat = false
    }

    fun coba() {
        viewModelScope.launch { muatBulan(bulan, paksa = true) }
    }

    // Tidak ada jurnal di masa depan; mundur sampai awal tahun ajaran
    fun bisaMaju(): Boolean = bulan < YearMonth.now()

    fun bisaMundur(): Boolean {
        val awal = mulai ?: return false
        return bulan > YearMonth.from(awal)
    }

    fun geserBulan(delta: Long) {
        if (delta > 0 && !bisaMaju()) return
        if (delta < 0 && !bisaMundur()) return

        aturBulan(bulan.plusMonths(delta))
    }

    fun pilihTanggal(tanggal: LocalDate) {
        tanggalDipilih = tanggal
    }

    fun jurnalPada(tanggal: LocalDate): List<JurnalBulanDto> =
        cache[YearMonth.from(tanggal)]
            .orEmpty()
            .filter { it.tanggal == tanggal.toString() }
            .sortedBy { it.jamKeMulai ?: 99 }

    // Sesi mengajar di tanggal yang sudah lewat tapi jurnalnya tidak ada
    fun terlewatPada(tanggal: LocalDate): List<JadwalMingguDto> {
        val pola = jadwal ?: return emptyList()
        val awal = mulai ?: return emptyList()
        val akhir = LocalDate.parse(pola.tahunAjaran.tanggalSelesai)

        if (tanggal >= LocalDate.now() || tanggal < awal || tanggal > akhir) return emptyList()

        // Bulan belum dimuat: jangan tampilkan apa pun, supaya tidak tampak semuanya terlewat
        val jurnalBulanIni = cache[YearMonth.from(tanggal)] ?: return emptyList()

        val sudahDiisi = jurnalBulanIni
            .filter { it.tanggal == tanggal.toString() }
            .map { it.jadwalId }
            .toSet()

        return pola.data
            .filter { it.hari == tanggal.dayOfWeek.value && it.id !in sudahDiisi }
            .sortedBy { it.jamKeMulai }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { JurnalKalenderViewModel(ApiClient.service) }
        }
    }
}