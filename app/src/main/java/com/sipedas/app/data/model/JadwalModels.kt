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

data class PiketDto(
    @SerializedName("jam_ke_mulai") val jamKeMulai: Int,
    @SerializedName("jam_ke_selesai") val jamKeSelesai: Int,
    @SerializedName("jam_mulai") val jamMulai: String,
    @SerializedName("jam_selesai") val jamSelesai: String,
    val tugas: String?,
)

data class JadwalHariIniResponse(
    val tanggal: String,
    @SerializedName("jam_server") val jamServer: String,
    val data: List<JadwalDto>,
    val piket: List<PiketDto>? = null,
)

data class JadwalMingguDto(
    val id: Int,
    val hari: Int, // 1 = Senin ... 5 = Jumat
    val kelas: IdNama?,
    val mapel: IdNama?,
    val ruang: RuangDto?,
    @SerializedName("jam_ke_mulai") val jamKeMulai: Int,
    @SerializedName("jam_ke_selesai") val jamKeSelesai: Int,
    @SerializedName("jam_mulai") val jamMulai: String,
    @SerializedName("jam_selesai") val jamSelesai: String,
)

data class TahunAjaranDto(
    val nama: String,
    @SerializedName("tanggal_mulai") val tanggalMulai: String,
    @SerializedName("tanggal_selesai") val tanggalSelesai: String,
)

data class JadwalSayaResponse(
    @SerializedName("tahun_ajaran") val tahunAjaran: TahunAjaranDto,
    val data: List<JadwalMingguDto>,
    val piket: List<PiketMingguDto>? = null,
)

data class PiketMingguDto(
    val id: Int,
    val hari: Int, // 1 = Senin ... 5 = Jumat
    @SerializedName("jam_ke_mulai") val jamKeMulai: Int,
    @SerializedName("jam_ke_selesai") val jamKeSelesai: Int,
    @SerializedName("jam_mulai") val jamMulai: String,
    @SerializedName("jam_selesai") val jamSelesai: String,
    val tugas: String?,
)
