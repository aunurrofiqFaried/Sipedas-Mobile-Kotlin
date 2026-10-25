package com.sipedas.app.data.model

import com.google.gson.annotations.SerializedName

data class KelasAbsenDto(
    val id: Int,
    val nama: String,
    @SerializedName("jumlah_siswa") val jumlahSiswa: Int,
    @SerializedName("sudah_diabsen") val sudahDiabsen: Int,
)

data class KelasHariIniResponse(
    val tanggal: String,
    val data: List<KelasAbsenDto>,
)

data class SiswaAbsenDto(
    val id: Int,
    val nis: String,
    val nama: String,
    @SerializedName("jenis_kelamin") val jenisKelamin: String?,
    val status: String?,
    val keterangan: String?,
    @SerializedName("ada_nomor_wali") val adaNomorWali: Boolean,
    @SerializedName("wa_link") val waLink: String?,
)

data class SiswaKelasResponse(
    val tanggal: String,
    val kelas: IdNama,
    val data: List<SiswaAbsenDto>,
)

data class ItemAbsen(
    @SerializedName("siswa_id") val siswaId: Int,
    val status: String,
    val keterangan: String?,
)

data class SimpanAbsensiRequest(
    @SerializedName("kelas_id") val kelasId: Int,
    val items: List<ItemAbsen>,
)

data class SimpanAbsensiResponse(
    val message: String,
    val tanggal: String,
    val jumlah: Int,
)