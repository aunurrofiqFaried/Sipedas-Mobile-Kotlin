package com.sipedas.app.data.model

import com.google.gson.annotations.SerializedName

data class PengumumanDto(
    val id: Int,
    val judul: String,
    val isi: String,
    val tingkat: String, // "biasa" atau "penting"
    val tanggal: String, // "2026-11-02 08:15:00"
    @SerializedName("berlaku_sampai") val berlakuSampai: String?,
    val pembuat: String?,
)

data class PengumumanResponse(
    @SerializedName("belum_dibaca") val belumDibaca: Int,
    @SerializedName("terakhir_dibaca_id") val terakhirDibacaId: Int,
    val data: List<PengumumanDto>,
)