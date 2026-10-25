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
            .addInterceptor(logging)
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