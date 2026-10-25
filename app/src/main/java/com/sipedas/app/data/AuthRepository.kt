package com.sipedas.app.data

import android.os.Build
import com.sipedas.app.data.api.ApiClient
import com.sipedas.app.data.api.ApiService
import com.sipedas.app.data.local.SessionStore
import com.sipedas.app.data.model.LoginRequest
import com.sipedas.app.data.model.UserDto

class AuthRepository(
    private val api: ApiService,
    private val store: SessionStore,
) {
    suspend fun login(username: String, password: String): Result<UserDto> = runCatching {
        val res = api.login(LoginRequest(username.trim(), password, Build.MODEL))
        ApiClient.token = res.token
        store.save(res.token, res.user)
        res.user
    }

    suspend fun logout() {
        // Tetap keluar di HP walaupun server tidak terjangkau
        runCatching { api.logout() }
        ApiClient.token = null
        store.clear()
    }

    // Hapus sesi di HP saja, tanpa memanggil server (dipakai saat server sudah menolak token)
    suspend fun hapusSesiLokal() {
        ApiClient.token = null
        store.clear()
    }
}