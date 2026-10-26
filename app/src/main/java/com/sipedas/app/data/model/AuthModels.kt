package com.sipedas.app.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String,
    @SerializedName("device_name") val deviceName: String? = null,
)

data class UserDto(
    val id: Int,
    val name: String,
    val username: String,
    val nip: String?,
    val role: String,
    @SerializedName("is_wali_kelas") val isWaliKelas: Boolean,
)

data class LoginResponse(
    val token: String,
    val user: UserDto,
)

data class MeResponse(
    val user: UserDto,
)

data class MessageResponse(
    val message: String,
)

// Bentuk error dari Laravel: pesan umum + pesan per field
data class ApiError(
    val message: String?,
    val errors: Map<String, List<String>>?,
)

data class UbahPasswordRequest(
    @SerializedName("password_lama") val passwordLama: String,
    @SerializedName("password_baru") val passwordBaru: String,
    @SerializedName("password_baru_confirmation") val konfirmasi: String,
)