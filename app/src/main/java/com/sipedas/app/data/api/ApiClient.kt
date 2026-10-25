package com.sipedas.app.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Token login. Diisi saat login berhasil atau saat aplikasi dibuka kembali.
    @Volatile
    var token: String? = null

    // Dipanggil saat server menolak token (akun dinonaktifkan atau token dicabut)
    @Volatile
    var onUnauthorized: (() -> Unit)? = null

    private val okHttp: OkHttpClient by lazy {
        // Level BASIC hanya mencatat alamat dan kode status.
        // Jangan pakai BODY di versi rilis: password dan token akan tercatat di Logcat.
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                    .header("Accept", "application/json")
                token?.let { builder.header("Authorization", "Bearer $it") }
                chain.proceed(builder.build())
            }
            .addInterceptor { chain ->
                val request = chain.request()
                val builder = request.newBuilder().header("Accept", "application/json")
                token?.let { builder.header("Authorization", "Bearer $it") }

                val response = chain.proceed(builder.build())

                // 401 pada request selain login = sesi tidak berlaku lagi
                if (response.code == 401 && token != null && !request.url.encodedPath.endsWith("/login")) {
                    onUnauthorized?.invoke()
                }

                response
            }
//            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}