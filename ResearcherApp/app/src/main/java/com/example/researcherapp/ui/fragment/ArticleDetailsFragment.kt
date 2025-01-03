package com.example.researcherapp.ui.fragment

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.researcherapp.R
import com.example.researcherapp.data.model.ArxivEntry
import com.example.researcherapp.databinding.FragmentArticleDetailsBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.OutputStream

class ArticleDetailsFragment : Fragment() {

    private var _binding: FragmentArticleDetailsBinding? = null
    private val binding get() = _binding!!
    private var article: ArxivEntry? = null
    private var pdfLink: String? = null

    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        article = arguments?.getParcelable("article")

        article?.let {
            binding.textViewTitle.text = it.title
            binding.textViewSummary.text = it.summary
            binding.textViewPublished.text = "Published: ${it.published}"
            binding.textViewUpdated.text = "Updated: ${it.updated}"

            pdfLink = it.link.find { link -> link.title == "pdf" }?.href

            binding.buttonDownload.setOnClickListener {
                pdfLink?.let { link ->
                    downloadPdf(link)
                } ?: Toast.makeText(requireContext(), "PDF link not found", Toast.LENGTH_SHORT).show()
            }

            binding.buttonReadArticle.setOnClickListener {
                pdfLink?.let { link ->
                    openPdfViewer(link)
                } ?: Toast.makeText(requireContext(), "PDF link not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadPdf(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            Toast.makeText(requireContext(), "Opening browser to download PDF", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to open browser: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openPdfViewer(pdfUrl: String) {
        val fragment = PdfViewerFragment().apply {
            arguments = Bundle().apply {
                putString("pdfUrl", pdfUrl)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
