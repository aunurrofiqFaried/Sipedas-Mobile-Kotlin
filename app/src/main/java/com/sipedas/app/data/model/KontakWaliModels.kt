package com.sipedas.app.data.model

import com.google.gson.annotations.SerializedName

data class WaliDto(
    val nama: String?,
    val hubungan: String?,
    @SerializedName("no_hp") val noHp: String?,
)

data class SiswaKontakDto(
    val id: Int,
    val nis: String,
    val nama: String,
    val kelas: String?,
)

data class RiwayatKontakDto(
    val id: Int,
    val tujuan: String,
    val catatan: String?,
    val oleh: String?,
    val waktu: String,
)

data class KontakWaliResponse(
    val siswa: SiswaKontakDto,
    val wali: WaliDto,
    @SerializedName("ada_nomor_wali") val adaNomorWali: Boolean,
    @SerializedName("wa_link") val waLink: String?,
    val riwayat: List<RiwayatKontakDto>,
)

data class CatatKontakRequest(
    val tujuan: String,
    val catatan: String?,
)