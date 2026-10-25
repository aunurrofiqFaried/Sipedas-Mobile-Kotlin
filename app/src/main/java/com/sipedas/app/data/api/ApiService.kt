package com.sipedas.app.data.api

import com.sipedas.app.data.model.LoginRequest
import com.sipedas.app.data.model.LoginResponse
import com.sipedas.app.data.model.MeResponse
import com.sipedas.app.data.model.MessageResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("me")
    suspend fun me(): MeResponse

    @POST("logout")
    suspend fun logout(): MessageResponse
}