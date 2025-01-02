package com.example.researcherapp.data.network

import com.example.researcherapp.data.model.ArxivFeed
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ArxivApi {
    @GET("query")
    fun searchArticles(
        @Query("search_query") searchQuery: String,
        @Query("start") start: Int = 0,
        @Query("max_results") maxResults: Int = 10
    ): Call<ArxivFeed>
}