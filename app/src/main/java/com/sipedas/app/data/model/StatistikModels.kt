package com.sipedas.app.data.model

import com.google.gson.annotations.SerializedName

data class HariTerpadatDto(val hari: Int, val jam: Int)

data class MengajarStatDto(
    @SerializedName("jumlah_kelas") val jumlahKelas: Int,
    @SerializedName("jumlah_mapel") val jumlahMapel: Int,
    @SerializedName("jam_per_minggu") val jamPerMinggu: Int,
    @SerializedName("jumlah_siswa") val jumlahSiswa: Int,
    @SerializedName("hari_terpadat") val hariTerpadat: HariTerpadatDto?,
)

data class JurnalBulanIniDto(
    val terisi: Int,
    val seharusnya: Int,
    val persen: Int?,
)

data class KelasWaliStatDto(
    val id: Int,
    val nama: String,
    @SerializedName("jumlah_siswa") val jumlahSiswa: Int,
    @SerializedName("kehadiran_persen") val kehadiranPersen: Int?,
    val sakit: Int,
    val izin: Int,
    val alpa: Int,
    @SerializedName("hari_tercatat") val hariTercatat: Int,
)

data class StatistikGuruResponse(
    val mengajar: MengajarStatDto,
    @SerializedName("jurnal_bulan_ini") val jurnalBulanIni: JurnalBulanIniDto,
    @SerializedName("kelas_wali") val kelasWali: List<KelasWaliStatDto>,
    @SerializedName("pelanggaran_dicatat") val pelanggaranDicatat: Int,
)