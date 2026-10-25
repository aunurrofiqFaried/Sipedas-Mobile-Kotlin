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