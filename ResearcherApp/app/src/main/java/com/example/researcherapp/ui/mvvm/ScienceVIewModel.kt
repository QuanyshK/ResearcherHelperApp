package com.example.researcherapp.ui.mvvm

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.*
import androidx.fragment.app.FragmentManager
import com.example.researcherapp.data.model.Article
import com.example.researcherapp.data.network.ApiClient
import com.example.researcherapp.ui.fragment.PdfViewerFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScienceViewModel(application: Application) : AndroidViewModel(application) {

    private val _articles = MutableLiveData<List<Article>>()
    val articles: LiveData<List<Article>> get() = _articles

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    init {
        fetchArticles()
    }

    private fun fetchArticles() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.instance.getArticles().execute()
                if (response.isSuccessful) {
                    _articles.postValue(response.body())
                } else {
                    _error.postValue("Failed to fetch articles")
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "An error occurred")
            }
        }
    }

    fun generateLink(doi: String, fragmentManager: FragmentManager) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val params = mapOf("doi" to doi)
                val response = ApiClient.instance.generateScienceLink(params).execute()
                if (response.isSuccessful) {
                    val pdfUrl = response.body()?.get("pdf_link")
                    if (!pdfUrl.isNullOrEmpty()) {
                        openPdfViewer(fragmentManager, pdfUrl)
                    } else {
                        _error.postValue("PDF link not found")
                    }
                    fetchArticles()
                } else {
                    _error.postValue("Failed to generate link")
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage ?: "An error occurred")
            }
        }
    }

    private fun openPdfViewer(fragmentManager: FragmentManager, pdfUrl: String) {
        val fragment = PdfViewerFragment().apply {
            arguments = Bundle().apply {
                putString("pdfUrl", pdfUrl)
            }
        }
        fragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }
}

class ScienceViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScienceViewModel::class.java)) {
            return ScienceViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
