package com.example.researcherapp.ui.mvvm

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.researcherapp.R
import com.example.researcherapp.data.model.Article
import com.example.researcherapp.data.network.ApiClient
import com.example.researcherapp.ui.fragment.PdfViewerFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScienceViewModel(application: Application) : AndroidViewModel(application) {

    private val _articleListState = MutableLiveData<ArticleListState>()
    val articleListState: LiveData<ArticleListState> get() = _articleListState

    private val _pdfLinkState = MutableLiveData<PdfLinkState>()
    val pdfLinkState: LiveData<PdfLinkState> get() = _pdfLinkState

    var articles = mutableListOf<Article>()

    fun fetchArticles() {
        _articleListState.value = ArticleListState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.instance.getArticles().execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        articles.clear()
                        articles.addAll(response.body() ?: emptyList())
                        _articleListState.value = ArticleListState.Success(articles)
                    } else {
                        _articleListState.value = ArticleListState.Error("Failed to load articles")
                    }
                }
            } catch (e: Exception) {
                _articleListState.postValue(ArticleListState.Error("Error: ${e.localizedMessage}"))
            }
        }
    }

    fun generatePdfLink(doi: String) {
        _pdfLinkState.value = PdfLinkState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val requestBody = mapOf("doi" to doi)
                val response = ApiClient.instance.generateScienceLink(requestBody).execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val pdfLink = response.body()?.get("pdf_link")
                        if (pdfLink != null) {
                            _pdfLinkState.value = PdfLinkState.Success(pdfLink)
                        } else {
                            _pdfLinkState.value = PdfLinkState.Error("PDF link not found")
                        }
                    } else {
                        _pdfLinkState.value = PdfLinkState.Error("Failed to generate PDF link")
                    }
                }
            } catch (e: Exception) {
                _pdfLinkState.postValue(PdfLinkState.Error("Error: ${e.localizedMessage}"))
            }
        }
    }

    fun openPdfViewer(fragmentManager: FragmentManager, pdfUrl: String) {
        val fragment = PdfViewerFragment().apply {
            arguments = Bundle().apply {
                putString("pdfUrl", pdfUrl)
            }
        }
        fragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }
    }

sealed class ArticleListState {
    object Loading : ArticleListState()
    data class Success(val items: List<Article>) : ArticleListState()
    data class Error(val message: String) : ArticleListState()
}

sealed class PdfLinkState {
    object Loading : PdfLinkState()
    data class Success(val pdfLink: String) : PdfLinkState()
    data class Error(val message: String) : PdfLinkState()
}
