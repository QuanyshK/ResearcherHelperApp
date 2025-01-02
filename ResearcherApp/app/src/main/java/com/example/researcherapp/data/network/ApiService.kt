package com.example.researcherapp.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path
import com.example.researcherapp.data.model.ChatMessage
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part

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

    @Multipart
    @POST("ai/send/")
    fun sendMessage(
        @Part("message") message: RequestBody,
        @Part file: MultipartBody.Part?,
        @Part("file_name") fileName: RequestBody
    ): Call<ChatMessage>

    @GET("ai/")
    fun getChatList(): Call<List<ChatMessage>>

    @GET("ai/{id}/")
    fun getChatDetail(@Path("id") chatId: Int): Call<ChatMessage>

    @DELETE("ai/{id}/delete/")
    fun deleteChat(@Path("id") chatId: Int): Call<Void>

    @POST("researcher_api/generate-link/")
    fun generateScienceLink(@Body params: Map<String, String>): Call<Map<String, String>>


}
