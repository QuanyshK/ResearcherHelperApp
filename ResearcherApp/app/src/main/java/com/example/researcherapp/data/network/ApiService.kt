package com.example.researcherapp.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path
import com.example.researcherapp.data.model.ChatMessage

data class RegisterRequest(val username: String, val email: String, val password: String)
data class LoginRequest(val username: String, val password: String)
data class TokenResponse(val token: String, val username: String)
data class ProfileResponse(val username: String, val email: String)
data class ChatRequest(val message: String)
data class ChatResponse(val id: Int, val user_message: String, val bot_response: String, val created_at: String)

interface ApiService {
    @POST("users/register/")
    fun register(@Body request: RegisterRequest): Call<TokenResponse>

    @POST("users/login/")
    fun login(@Body request: LoginRequest): Call<TokenResponse>

    @GET("users/profile/")
    fun getProfile(): Call<ProfileResponse>

    @POST("ai/send/")
    fun sendMessage(@Body request: ChatRequest): Call<ChatMessage>

    @GET("ai/")
    fun getChatList(): Call<List<ChatMessage>>

    @GET("ai/{id}/")
    fun getChatDetail(@Path("id") chatId: Int): Call<ChatMessage>

    @DELETE("ai/{id}/delete/")
    fun deleteChat(@Path("id") chatId: Int): Call<Void>
}
