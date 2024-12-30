package com.example.researcherapp.data.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.Interceptor

object ApiClient {
    private const val BASE_URL = "http://192.168.1.108:8000/"
    private const val WEBSOCKET_URL = "ws://192.168.1.108:8000/ws/chat/default_room/"

    private lateinit var authToken: String

    fun setAuthToken(token: String) {
        authToken = token
    }

    private val client by lazy {
        OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                if (::authToken.isInitialized && authToken.isNotEmpty()) {
                    request.addHeader("Authorization", "Token $authToken")
                }
                chain.proceed(request.build())
            })
            .build()
    }

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun connectWebSocket(listener: WebSocketListener): WebSocket {
        val request = Request.Builder()
            .url(WEBSOCKET_URL)
            .build()
        return client.newWebSocket(request, listener)
    }
}
