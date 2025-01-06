package com.example.researcherapp.ui.mvvm

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.researcherapp.R
import com.example.researcherapp.data.model.ArxivEntry
import com.example.researcherapp.ui.fragment.PdfViewerFragment
import java.text.SimpleDateFormat
import java.util.*

class ArticleDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val _article = MutableLiveData<ArxivEntry>()
    val article: LiveData<ArxivEntry> get() = _article

    private val _pdfLink = MutableLiveData<String?>()
    val pdfLink: LiveData<String?> get() = _pdfLink

    fun setArticle(entry: ArxivEntry) {
        _article.value = entry
        _pdfLink.value = entry.link.find { it.title == "pdf" }?.href
    }

    fun getFormattedDate(date: String): String {
        return try {
            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val targetFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val parsedDate: Date = originalFormat.parse(date) ?: Date()
            targetFormat.format(parsedDate)
        } catch (e: Exception) {
            "Unknown Date"
        }
    }

    fun downloadPdf(pdfUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            showToast("Failed to open browser: ${e.localizedMessage}")
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

    private fun showToast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
    }
}

class ArticleDetailsViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticleDetailsViewModel::class.java)) {
            return ArticleDetailsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
