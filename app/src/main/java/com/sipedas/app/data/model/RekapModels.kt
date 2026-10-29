package com.sipedas.app.data.model

import com.google.gson.annotations.SerializedName

data class KelasRekapDto(
    val id: Int,
    val nama: String,
    @SerializedName("jumlah_siswa") val jumlahSiswa: Int,
)

data class KelasRekapResponse(val data: List<KelasRekapDto>)

data class PeriodeDto(
    val label: String,
    val dari: String,
    val sampai: String,
)

data class RekapTotalDto(
    val hadir: Int,
    val sakit: Int,
    val izin: Int,
    val alpa: Int,
)

data class RekapSiswaDto(
    val id: Int,
    val nis: String,
    val nama: String,
    val hadir: Int,
    val sakit: Int,
    val izin: Int,
    val alpa: Int,
)

data class RekapAbsensiResponse(
    val kelas: IdNama,
    val periode: PeriodeDto,
    @SerializedName("hari_tercatat") val hariTercatat: Int,
    val total: RekapTotalDto,
    val siswa: List<RekapSiswaDto>,
)