package com.sipedas.app.data.model

import com.google.gson.annotations.SerializedName

data class KelasSholatDto(
    val id: Int,
    val nama: String,
    @SerializedName("jumlah_peserta") val jumlahPeserta: Int,
    @SerializedName("absensi_harian_siap") val absensiHarianSiap: Boolean,
    val dhuhur: Int,
    val ashar: Int,
)

data class KelasSholatResponse(
    val tanggal: String,
    val data: List<KelasSholatDto>,
)

data class SiswaSholatDto(
    val id: Int,
    val nis: String,
    val nama: String,
    @SerializedName("jenis_kelamin") val jenisKelamin: String?,
    @SerializedName("status_harian") val statusHarian: String?,
    val terkunci: Boolean,
    val status: String?,
)

data class SiswaSholatResponse(
    val tanggal: String,
    val waktu: String,
    val kelas: IdNama,
    val data: List<SiswaSholatDto>,
)

data class ItemSholat(
    @SerializedName("siswa_id") val siswaId: Int,
    val status: String,
)

data class SimpanSholatRequest(
    @SerializedName("kelas_id") val kelasId: Int,
    val waktu: String,
    val items: List<ItemSholat>,
)

data class SimpanSholatResponse(
    val message: String,
    val waktu: String,
    val tanggal: String,
    val jumlah: Int,
)