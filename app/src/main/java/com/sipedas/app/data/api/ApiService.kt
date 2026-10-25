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
}