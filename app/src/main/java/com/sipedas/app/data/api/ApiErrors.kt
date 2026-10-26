package com.sipedas.app.data.api

import com.google.gson.Gson
import com.sipedas.app.data.model.ApiError
import retrofit2.HttpException
import java.io.IOException

// Mengubah error teknis menjadi kalimat yang bisa dibaca guru
fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> {
        if (code() == 429) {
            return "Terlalu banyak percobaan. Tunggu sebentar lalu coba lagi."
        }
        val body = response()?.errorBody()?.string()
        val parsed = try {
            Gson().fromJson(body, ApiError::class.java)
        } catch (e: Exception) {
            null
        }
        val pesanField = parsed?.errors?.values?.firstOrNull()?.firstOrNull()
        pesanField ?: parsed?.message ?: "Terjadi kesalahan (kode ${code()})"
    }
    is IOException -> "Tidak dapat terhubung ke server. Periksa koneksi internet."
    else -> message ?: "Terjadi kesalahan."
}