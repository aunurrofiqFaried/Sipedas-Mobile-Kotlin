package com.sipedas.app.data.model

import com.google.gson.annotations.SerializedName

data class AcaraDto(
    val id: Int,
    val judul: String,
    val jenis: String, // "libur" atau "peringatan"
    @SerializedName("tanggal_mulai") val tanggalMulai: String,
    @SerializedName("tanggal_selesai") val tanggalSelesai: String,
    val keterangan: String?,
)

data class KalenderAkademikResponse(val data: List<AcaraDto>)