package com.example.researcherapp.data.network

import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

object ArxivClient {
    private const val BASE_URL = "https://export.arxiv.org/api/"

    val instance: ArxivApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
            .create(ArxivApi::class.java)
    }
}