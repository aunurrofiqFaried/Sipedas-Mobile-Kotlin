package com.sipedas.app.data.model

import com.google.gson.annotations.SerializedName

data class JenisRemisiDto(
    val id: Int,
    val nama: String,
    val poin: Int,
    val keterangan: String?,
)

data class JenisRemisiResponse(val data: List<JenisRemisiDto>)

data class RemisiDto(
    val id: Int,
    val poin: Int,
    val tanggal: String,
    val keterangan: String?,
    val jenis: JenisRingkas?,
    val pencatat: PencatatDto?,
)

data class BeriRemisiRequest(
    @SerializedName("siswa_id") val siswaId: Int,
    @SerializedName("jenis_remisi_id") val jenisRemisiId: Int,
    val keterangan: String?,
)

data class BeriRemisiResponse(
    val message: String,
    @SerializedName("poin_dikurangi") val poinDikurangi: Int,
    @SerializedName("total_poin_sebelum") val totalPoinSebelum: Int,
    @SerializedName("total_poin_sekarang") val totalPoinSekarang: Int,
)