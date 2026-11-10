package com.sipedas.app.data.model

import com.google.gson.annotations.SerializedName

/* ---------- GET /api/pimpinan/dashboard ---------- */

data class DashboardPimpinan(
    val diperbarui: String,
    val hari: HariDashboard,
    val ringkasan: RingkasanDashboard,
    val absensi: AbsensiDashboard,
    @SerializedName("tren_absensi") val trenAbsensi: List<TrenAbsensi>,
    @SerializedName("peta_panas") val petaPanas: PetaPanas,
    @SerializedName("kelas_belum") val kelasBelum: List<KelasBelum>,
    @SerializedName("kelas_terendah") val kelasTerendah: List<KelasTerendah>,
    val sholat: SholatDashboard,
    @SerializedName("jurnal_hari") val jurnalHari: JurnalHari,
    val kepatuhan: KepatuhanDashboard,
    val pelanggaran: PelanggaranDashboard,
    val sp: SpDashboard,
    val jurusan: List<JurusanRingkas>,
    val peringatan: List<PeringatanDashboard>,
)

data class HariDashboard(
    val tanggal: String,
    val label: String,
    @SerializedName("hari_ini") val hariIni: Boolean,
    val libur: String?,
)

data class RingkasanDashboard(
    val siswa: Int,
    val kelas: Int,
    @SerializedName("guru_mengajar") val guruMengajar: Int,
    @SerializedName("pelanggaran_bulan") val pelanggaranBulan: PelanggaranBulan,
    @SerializedName("sp_menunggu") val spMenunggu: Int,
    @SerializedName("mendekati_sp") val mendekatiSp: Int,
)

data class PelanggaranBulan(
    val jumlah: Int,
    val poin: Int,
    @SerializedName("jumlah_lalu") val jumlahLalu: Int,
    @SerializedName("poin_lalu") val poinLalu: Int,
)

data class AbsensiDashboard(
    @SerializedName("total_siswa") val totalSiswa: Int,
    val tercatat: Int,
    val hadir: Int,
    val sakit: Int,
    val izin: Int,
    val alpa: Int,
    val belum: Int,
    val persen: Int?,
    @SerializedName("kelas_total") val kelasTotal: Int,
    @SerializedName("kelas_diabsen") val kelasDiabsen: Int,
)

data class TrenAbsensi(
    val tanggal: String,
    val label: String,
    val persen: Int?,
    val hadir: Int,
    val sakit: Int,
    val izin: Int,
    val alpa: Int,
    val total: Int,
)

data class PetaPanas(
    val tanggal: List<String>,
    val kelas: List<PetaKelas>,
)

data class PetaKelas(
    val id: Int,
    val nama: String,
    val jurusan: String,
    val nilai: List<Int?>,
)

data class KelasBelum(
    val id: Int,
    val nama: String,
    val jurusan: String,
    val siswa: Int,
)

data class KelasTerendah(
    val id: Int,
    val nama: String,
    val jurusan: String,
    val siswa: Int,
    val persen: Int?,
    val hadir: Int,
    val tercatat: Int,
)

data class SholatRingkas(
    val hadir: Int,
    @SerializedName("tidak_hadir") val tidakHadir: Int,
    val berhalangan: Int,
    val persen: Int?,
    val kelas: Int,
)

data class SholatDashboard(
    val dhuhur: SholatRingkas,
    val ashar: SholatRingkas,
    @SerializedName("kelas_total") val kelasTotal: Int,
)

data class JurnalHari(
    val total: Int,
    val terisi: Int,
    val terlewat: Int,
    val berjalan: Int,
    val persen: Int?,
    @SerializedName("daftar_terlewat") val daftarTerlewat: List<SesiTerlewatHari>,
)

data class SesiTerlewatHari(
    val guru: String,
    val kelas: String?,
    val mapel: String?,
    @SerializedName("jam_ke_mulai") val jamKeMulai: Int,
    @SerializedName("jam_ke_selesai") val jamKeSelesai: Int,
    @SerializedName("jam_mulai") val jamMulai: String,
    @SerializedName("jam_selesai") val jamSelesai: String,
)

data class KepatuhanDashboard(
    val ringkasan: RingkasanKepatuhan,
    val mingguan: List<MingguKepatuhan>,
    val terendah: List<GuruKepatuhan>,
)

data class PelanggaranDashboard(
    val mingguan: List<MingguPelanggaran>,
    @SerializedName("jenis_teratas") val jenisTeratas: List<JenisTeratas>,
)

data class MingguPelanggaran(val label: String, val jumlah: Int)

data class JenisTeratas(val nama: String, val jumlah: Int, val poin: Int)

data class SpDashboard(
    val menunggu: List<SpMenunggu>,
    val mendekati: List<SpMendekati>,
    @SerializedName("poin_tertinggi") val poinTertinggi: List<PoinTertinggi>,
)

data class SpMenunggu(
    val siswa: String,
    val kelas: String,
    val aturan: String,
    val poin: Int,
    val tanggal: String,
)

data class SpMendekati(
    val nama: String,
    val kelas: String,
    @SerializedName("poin_aktif") val poinAktif: Int,
    val sisa: Int,
    val menuju: String,
)

data class PoinTertinggi(
    val nama: String,
    val kelas: String,
    @SerializedName("poin_aktif") val poinAktif: Int,
)

data class JurusanRingkas(
    val nama: String,
    val kelas: Int,
    val siswa: Int,
    val hadir: Int,
    val tercatat: Int,
    val persen: Int?,
    @SerializedName("pelanggaran_bulan") val pelanggaranBulan: Int,
)

// tingkat: "danger", "warn", "info", atau "ok"
data class PeringatanDashboard(
    val tingkat: String,
    val judul: String,
    val detail: String?,
    val tautan: String?,
)

/* ---------- GET /api/pimpinan/kepatuhan-jurnal dan /{guru} ---------- */

data class KepatuhanJurnalResponse(
    val periode: PeriodeKepatuhan,
    val ringkasan: RingkasanKepatuhan,
    val mingguan: List<MingguKepatuhan>,
    val guru: List<GuruKepatuhan>,
)

data class PeriodeKepatuhan(
    val dari: String,
    val sampai: String,
    @SerializedName("dihitung_sampai") val dihitungSampai: String,
    val label: String,
)

data class RingkasanKepatuhan(
    val guru: Int,
    @SerializedName("hari_efektif") val hariEfektif: Int,
    val seharusnya: Int,
    val terisi: Int,
    val terlewat: Int,
    val persen: Int?,
    val baik: Int,
    val cukup: Int,
    val kurang: Int,
)

data class MingguKepatuhan(
    val label: String,
    val seharusnya: Int,
    val terisi: Int,
    val persen: Int?,
)

// status: "baik", "cukup", "kurang", atau null kalau belum ada sesi
data class GuruKepatuhan(
    val id: Int,
    val nama: String,
    val nip: String?,
    @SerializedName("sesi_per_minggu") val sesiPerMinggu: Int,
    val seharusnya: Int,
    val terisi: Int,
    val terlewat: Int,
    val persen: Int?,
    val status: String?,
    @SerializedName("terakhir_isi") val terakhirIsi: String?,
)

data class DetailKepatuhan(
    val guru: GuruRingkas,
    val periode: PeriodeKepatuhan,
    val ringkasan: RingkasanGuru,
    val terlewat: List<SesiTerlewat>,
)

data class GuruRingkas(val id: Int, val nama: String, val nip: String?)

data class RingkasanGuru(
    @SerializedName("sesi_per_minggu") val sesiPerMinggu: Int,
    val seharusnya: Int,
    val terisi: Int,
    val terlewat: Int,
    val persen: Int?,
    val status: String?,
    @SerializedName("terakhir_isi") val terakhirIsi: String?,
)

data class SesiTerlewat(
    val tanggal: String,
    val hari: String,
    @SerializedName("tanggal_label") val tanggalLabel: String,
    val kelas: String?,
    val mapel: String?,
    @SerializedName("jam_ke_mulai") val jamKeMulai: Int,
    @SerializedName("jam_ke_selesai") val jamKeSelesai: Int,
    @SerializedName("jam_mulai") val jamMulai: String,
    @SerializedName("jam_selesai") val jamSelesai: String,
)