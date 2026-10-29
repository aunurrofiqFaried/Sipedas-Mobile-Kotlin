package com.sipedas.app.data.model

import com.google.gson.annotations.SerializedName

data class JadwalRingkas(
    val id: Int,
    val mapel: IdNama?,
)

data class JurnalDto(
    val id: Int,
    @SerializedName("jadwal_id") val jadwalId: Int,
    val tanggal: String,
    val materi: String,
    val catatan: String?,
    val kelas: IdNama?,
    val jadwal: JadwalRingkas?,
)

data class JurnalPageResponse(
    val data: List<JurnalDto>,
)

data class SimpanJurnalRequest(
    @SerializedName("jadwal_id") val jadwalId: Int,
    val materi: String,
    val catatan: String?,
)

data class SimpanJurnalResponse(
    val message: String,
)

data class JurnalBulanDto(
    val id: Int,
    @SerializedName("jadwal_id") val jadwalId: Int,
    val tanggal: String, // "2026-10-26"
    val materi: String,
    val catatan: String?,
    val kelas: IdNama?,
    val mapel: IdNama?,
    @SerializedName("jam_ke_mulai") val jamKeMulai: Int?,
    @SerializedName("jam_ke_selesai") val jamKeSelesai: Int?,
    @SerializedName("jam_mulai") val jamMulai: String?,
    @SerializedName("jam_selesai") val jamSelesai: String?,
)

data class JurnalBulanResponse(
    val bulan: String,
    val data: List<JurnalBulanDto>,
)