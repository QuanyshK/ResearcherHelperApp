package com.example.researcherapp.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(val username: String, val email: String, val password: String)
data class LoginRequest(val username: String, val password: String)
data class TokenResponse(val token: String, val username: String)

interface ApiService {
    @POST("register/")
    fun register(@Body request: RegisterRequest): Call<TokenResponse>

    @POST("login/")
    fun login(@Body request: LoginRequest): Call<TokenResponse>
}
