package com.example.researcherapp.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class RegisterRequest(val username: String, val email: String, val password: String)
data class LoginRequest(val username: String, val password: String)
data class TokenResponse(val token: String, val username: String)
data class ProfileResponse(val username: String, val email: String)
data class ChatRequest(val message: String)
data class ChatResponse(val sender: String, val message: String, val timestamp: String)

interface ApiService {
    @POST("users/register/")
    fun register(@Body request: RegisterRequest): Call<TokenResponse>

    @POST("users/login/")
    fun login(@Body request: LoginRequest): Call<TokenResponse>

    @GET("users/profile/")
    fun getProfile(): Call<ProfileResponse>

    @POST("ai/summarize/")
    fun sendMessage(@Body request: ChatRequest): Call<ChatResponse>
}
