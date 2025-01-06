package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.researcherapp.data.model.ArxivEntry
import com.example.researcherapp.databinding.FragmentArticleDetailsBinding
import com.example.researcherapp.ui.mvvm.ArticleDetailsViewModel
import com.example.researcherapp.ui.mvvm.ArticleDetailsViewModelFactory

class ArticleDetailsFragment : Fragment() {

    private var _binding: FragmentArticleDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ArticleDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleDetailsBinding.inflate(inflater, container, false)

        val application = requireActivity().application
        val factory = ArticleDetailsViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory).get(ArticleDetailsViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val article = arguments?.let { BundleCompat.getParcelable(it, "article", ArxivEntry::class.java) }
        article?.let {
            viewModel.setArticle(it)
        }

        viewModel.article.observe(viewLifecycleOwner) { article ->
            binding.textViewTitle.text = article.title
            binding.textViewSummary.text = article.summary
            binding.textViewPublished.text = "Published: ${viewModel.getFormattedDate(article.published)}"
            binding.textViewUpdated.text = "Updated: ${viewModel.getFormattedDate(article.updated)}"
        }

        viewModel.pdfLink.observe(viewLifecycleOwner) { pdfLink ->
            binding.buttonDownload.setOnClickListener {
                pdfLink?.let {
                    viewModel.downloadPdf(it)
                }
            }
            binding.buttonReadArticle.setOnClickListener {
                pdfLink?.let {
                    viewModel.openPdfViewer(parentFragmentManager, it)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
