package com.sipedas.app.data.api

import com.sipedas.app.data.model.LoginRequest
import com.sipedas.app.data.model.LoginResponse
import com.sipedas.app.data.model.MeResponse
import com.sipedas.app.data.model.MessageResponse
import com.sipedas.app.data.model.JadwalHariIniResponse
import com.sipedas.app.data.model.KelasHariIniResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.sipedas.app.data.model.SiswaKelasResponse
import com.sipedas.app.data.model.SimpanAbsensiRequest
import com.sipedas.app.data.model.SimpanAbsensiResponse
import com.sipedas.app.data.model.JurnalPageResponse
import com.sipedas.app.data.model.SimpanJurnalRequest
import com.sipedas.app.data.model.SimpanJurnalResponse
import retrofit2.http.Query
import com.sipedas.app.data.model.JenisPelanggaranResponse
import com.sipedas.app.data.model.SiswaCariResponse
import com.sipedas.app.data.model.CatatPelanggaranRequest
import com.sipedas.app.data.model.CatatPelanggaranResponse
import com.sipedas.app.data.model.RiwayatPelanggaranResponse
import com.sipedas.app.data.model.UbahPasswordRequest
import com.sipedas.app.data.model.KontakWaliResponse
import com.sipedas.app.data.model.CatatKontakRequest
import com.sipedas.app.data.model.KelasSholatResponse
import com.sipedas.app.data.model.SiswaSholatResponse
import com.sipedas.app.data.model.SimpanSholatRequest
import com.sipedas.app.data.model.SimpanSholatResponse
import com.sipedas.app.data.model.JadwalSayaResponse
import com.sipedas.app.data.model.KelasRekapResponse
import com.sipedas.app.data.model.RekapAbsensiResponse
import com.sipedas.app.data.model.JurnalBulanResponse


interface ApiService {

    @POST("login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("me")
    suspend fun me(): MeResponse

    @POST("logout")
    suspend fun logout(): MessageResponse

    @GET("jadwal/hari-ini")
    suspend fun jadwalHariIni(): JadwalHariIniResponse

    @GET("absensi/kelas-hari-ini")
    suspend fun kelasHariIni(): KelasHariIniResponse

    @GET("kelas/{id}/siswa")
    suspend fun siswaKelas(@Path("id") id: Int): SiswaKelasResponse

    @POST("absensi")
    suspend fun simpanAbsensi(@Body body: SimpanAbsensiRequest): SimpanAbsensiResponse

    @GET("jurnal")
    suspend fun riwayatJurnal(): JurnalPageResponse

    @POST("jurnal")
    suspend fun simpanJurnal(@Body body: SimpanJurnalRequest): SimpanJurnalResponse

    @GET("jurnal/bulan")
    suspend fun jurnalBulan(@Query("bulan") bulan: String): JurnalBulanResponse

    @GET("pelanggaran/jenis")
    suspend fun jenisPelanggaran(): JenisPelanggaranResponse

    @GET("siswa/cari")
    suspend fun cariSiswa(@Query("q") q: String): SiswaCariResponse

    @POST("pelanggaran")
    suspend fun catatPelanggaran(@Body body: CatatPelanggaranRequest): CatatPelanggaranResponse

    @GET("siswa/{id}/pelanggaran")
    suspend fun riwayatPelanggaran(@Path("id") id: Int): RiwayatPelanggaranResponse

    @POST("ubah-password")
    suspend fun ubahPassword(@Body body: UbahPasswordRequest): MessageResponse

    @GET("bk/siswa/{id}/kontak-wali")
    suspend fun kontakWali(@Path("id") id: Int): KontakWaliResponse

    @POST("bk/siswa/{id}/kontak-wali")
    suspend fun catatKontakWali(@Path("id") id: Int, @Body body: CatatKontakRequest): MessageResponse

    @GET("sholat/kelas-saya")
    suspend fun kelasSholat(): KelasSholatResponse

    @GET("sholat/{waktu}/kelas/{id}")
    suspend fun siswaSholat(@Path("waktu") waktu: String, @Path("id") id: Int): SiswaSholatResponse

    @POST("sholat")
    suspend fun simpanSholat(@Body body: SimpanSholatRequest): SimpanSholatResponse

    @GET("jadwal/saya")
    suspend fun jadwalSaya(): JadwalSayaResponse

    @GET("rekap/kelas")
    suspend fun kelasRekap(): KelasRekapResponse

    @GET("rekap/absensi")
    suspend fun rekapAbsensi(
        @Query("kelas_id") kelasId: Int,
        @Query("bulan") bulan: String,
    ): RekapAbsensiResponse
}