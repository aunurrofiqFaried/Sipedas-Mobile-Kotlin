package com.sipedas.app.ui.sholat

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
import com.sipedas.app.data.model.ItemSholat
import com.sipedas.app.data.model.SimpanSholatRequest
import com.sipedas.app.ui.absensi.PesanAbsensi
import kotlinx.coroutines.launch

data class SholatItem(
    val id: Int,
    val nis: String,
    val nama: String,
    val jenisKelamin: String?,
    val statusHarian: String?, // status absensi pagi (hadir/sakit/izin/alpa) atau null
    val terkunci: Boolean,     // true = tidak ikut sholat, mengikuti absensi pagi
    val status: String?,       // pilihan saat ini: hadir / tidak_hadir / berhalangan
)

class SholatSiswaViewModel(
    private val api: ApiService,
    val waktu: String,
    private val kelasId: Int,
) : ViewModel() {

    var loading by mutableStateOf(true)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var kelasNama by mutableStateOf("")
        private set

    var siswa by mutableStateOf<List<SholatItem>>(emptyList())
        private set

    var saving by mutableStateOf(false)
        private set

    var pesan by mutableStateOf<PesanAbsensi?>(null)
        private set

    init {
        muat()
    }

    fun muat() {
        viewModelScope.launch {
            if (siswa.isEmpty()) loading = true
            error = null

            runCatching { api.siswaSholat(waktu, kelasId) }
                .onSuccess { res ->
                    kelasNama = res.kelas.nama
                    siswa = res.data.map {
                        SholatItem(
                            id = it.id,
                            nis = it.nis,
                            nama = it.nama,
                            jenisKelamin = it.jenisKelamin,
                            statusHarian = it.statusHarian,
                            terkunci = it.terkunci,
                            status = it.status,
                        )
                    }
                }
                .onFailure { error = it.toUserMessage() }

            loading = false
        }
    }

    fun setStatus(id: Int, status: String) {
        siswa = siswa.map { if (it.id == id && !it.terkunci) it.copy(status = status) else it }
        pesan = null
    }

    fun semuaHadir() {
        siswa = siswa.map { if (it.terkunci) it else it.copy(status = "hadir") }
        pesan = null
    }

    fun simpan() {
        val bisaDitandai = siswa.filter { !it.terkunci }

        if (saving || bisaDitandai.isEmpty() || bisaDitandai.any { it.status == null }) return

        viewModelScope.launch {
            saving = true
            pesan = null

            val request = SimpanSholatRequest(
                kelasId = kelasId,
                waktu = waktu,
                items = bisaDitandai.map { ItemSholat(it.id, it.status!!) },
            )

            runCatching { api.simpanSholat(request) }
                .onSuccess {
                    SholatEvents.berubah.value += 1
                    pesan = PesanAbsensi(
                        "✓ Absensi sholat ${labelWaktu(waktu)} tersimpan. Masih bisa diubah hari ini.",
                        sukses = true,
                    )
                    muat()
                }
                .onFailure { pesan = PesanAbsensi(it.toUserMessage(), sukses = false) }

            saving = false
        }
    }

    companion object {
        fun factory(waktu: String, kelasId: Int) = viewModelFactory {
            initializer { SholatSiswaViewModel(ApiClient.service, waktu, kelasId) }
        }
    }
}