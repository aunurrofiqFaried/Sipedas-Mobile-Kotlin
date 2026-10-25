package com.sipedas.app.data.model

import com.google.gson.annotations.SerializedName

data class JenisPelanggaranDto(
    val id: Int,
    val nama: String,
    val kategori: String?,
    val poin: Int,
)

data class JenisPelanggaranResponse(val data: List<JenisPelanggaranDto>)

data class SiswaCariDto(
    val id: Int,
    val nis: String,
    val nama: String,
    val kelas: String?,
)

data class SiswaCariResponse(val data: List<SiswaCariDto>)

data class SiswaRingkas(
    val id: Int,
    val nis: String,
    val nama: String,
)

data class AturanSpDto(
    val id: Int,
    val nama: String,
    val level: Int,
    @SerializedName("batas_poin") val batasPoin: Int,
)

data class SpSiswaDto(
    val id: Int,
    val status: String,
    @SerializedName("total_poin_saat_terbit") val totalPoinSaatTerbit: Int,
    val aturan: AturanSpDto?,
)

data class JenisRingkas(val id: Int, val nama: String)

data class PencatatDto(val id: Int, val name: String)

data class PelanggaranDto(
    val id: Int,
    val poin: Int,
    val tanggal: String,
    val keterangan: String?,
    val jenis: JenisRingkas?,
    val pencatat: PencatatDto?,
)

data class RiwayatPelanggaranResponse(
    val siswa: SiswaRingkas,
    @SerializedName("tahun_ajaran") val tahunAjaran: String,
    @SerializedName("total_poin") val totalPoin: Int,
    val sp: List<SpSiswaDto>,
    val riwayat: List<PelanggaranDto>,
)

data class CatatPelanggaranRequest(
    @SerializedName("siswa_id") val siswaId: Int,
    @SerializedName("jenis_pelanggaran_id") val jenisPelanggaranId: Int,
    val keterangan: String?,
)

data class CatatPelanggaranResponse(
    val message: String,
    @SerializedName("poin_ditambahkan") val poinDitambahkan: Int,
    @SerializedName("total_poin") val totalPoin: Int,
    @SerializedName("sp_baru") val spBaru: SpSiswaDto?,
)