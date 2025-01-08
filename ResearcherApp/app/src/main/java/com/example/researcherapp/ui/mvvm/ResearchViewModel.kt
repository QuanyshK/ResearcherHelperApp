package com.example.researcherapp.ui.mvvm

import android.app.Application
import androidx.lifecycle.*
import com.example.researcherapp.data.model.ArxivEntry
import com.example.researcherapp.data.model.ArxivFeed
import com.example.researcherapp.data.network.ArxivClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class ResearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _articlesState = MutableLiveData<ArticlesState>()
    val articlesState: LiveData<ArticlesState> get() = _articlesState

    private var currentPage = 0
    internal val pageSize = 10
    internal var isLastPage = false
    internal var isLoading = false

    fun searchArticles(query: String) {
        if (query.isEmpty() || isLoading || isLastPage) return
        _articlesState.value = ArticlesState.Loading
        isLoading = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = ArxivClient.instance
                    .searchArticles(query, currentPage * pageSize, pageSize)
                    .execute()

                withContext(Dispatchers.Main) {
                    handleResponse(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val errorMessage = when (e) {
                        is java.net.UnknownHostException -> "No internet connection. Please check your network."
                        is java.net.SocketTimeoutException -> "The request timed out. Please try again."
                        else -> "An unexpected error occurred"
                    }
                    _articlesState.value = ArticlesState.Error(errorMessage)
                    isLoading = false
                }
            }
        }
    }


    fun resetPagination() {
        currentPage = 0
        isLastPage = false
        _articlesState.value = ArticlesState.Reset
    }

    private fun handleResponse(response: Response<ArxivFeed>) {
        if (response.isSuccessful) {
            val entries = response.body()?.entry ?: emptyList()

            if (entries.isEmpty()) {
                _articlesState.value = ArticlesState.Error("No articles found. Try refining your search.")
                isLastPage = true
            } else {
                _articlesState.value = ArticlesState.Success(entries)
                if (entries.size < pageSize) {
                    isLastPage = true
                } else {
                    currentPage++
                }
            }
        } else {
            val errorMessage = when (response.code()) {
                400 -> "Invalid search query. Please check and try again."
                404 -> "No results found. Try a different search term."
                500 -> "Server error. Please try again later."
                else -> "Failed to load articles. Please try again."
            }
            _articlesState.value = ArticlesState.Error(errorMessage)
        }
        isLoading = false
    }

}

sealed class ArticlesState {
    object Loading : ArticlesState()
    object Reset : ArticlesState()
    data class Success(val articles: List<ArxivEntry>) : ArticlesState()
    data class Error(val message: String) : ArticlesState()
}
