package com.sipedas.app.data.model

import com.google.gson.annotations.SerializedName

data class IdNama(
    val id: Int,
    val nama: String,
)

data class RuangDto(
    val id: Int,
    val nama: String,
    val tipe: String,
)

data class JadwalDto(
    val id: Int,
    val kelas: IdNama?,
    val mapel: IdNama?,
    val ruang: RuangDto?,
    @SerializedName("jam_ke_mulai") val jamKeMulai: Int,
    @SerializedName("jam_ke_selesai") val jamKeSelesai: Int,
    @SerializedName("jam_mulai") val jamMulai: String,
    @SerializedName("jam_selesai") val jamSelesai: String,
    @SerializedName("sudah_jurnal") val sudahJurnal: Boolean,
    @SerializedName("bisa_isi_jurnal") val bisaIsiJurnal: Boolean,
)

data class JadwalHariIniResponse(
    val tanggal: String,
    @SerializedName("jam_server") val jamServer: String,
    val data: List<JadwalDto>,
)