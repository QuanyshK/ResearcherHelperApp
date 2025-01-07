package com.example.researcherapp.data.network

import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://192.168.1.108:8000/"

    private lateinit var authToken: String

    fun setAuthToken(token: String) {
        authToken = token
    }

    private val client by lazy {
        OkHttpClient.Builder()
            .callTimeout(75, java.util.concurrent.TimeUnit.SECONDS)
            .connectTimeout(75, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(75, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(75, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                if (::authToken.isInitialized && authToken.isNotEmpty()) {
                    request.addHeader("Authorization", "Token $authToken")
                }
                chain.proceed(request.build())
            }
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
}
