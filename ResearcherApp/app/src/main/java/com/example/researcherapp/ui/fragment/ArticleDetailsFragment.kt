package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.researcherapp.R
import com.example.researcherapp.data.model.ArxivEntry
import com.example.researcherapp.databinding.FragmentArticleDetailsBinding

class ArticleDetailsFragment : Fragment() {

    private var _binding: FragmentArticleDetailsBinding? = null
    private val binding get() = _binding!!
    private var article: ArxivEntry? = null
    private var pdfLink: String? = null

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
                openPdfViewer(pdfLink!!)
            }

            binding.buttonReadArticle.setOnClickListener {
                openPdfViewer(pdfLink!!)
            }
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
