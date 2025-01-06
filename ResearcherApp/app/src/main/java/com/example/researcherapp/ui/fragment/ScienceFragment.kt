package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.researcherapp.R
import com.example.researcherapp.databinding.FragmentScienceBinding
import com.example.researcherapp.ui.adapter.ScienceAdapter
import com.example.researcherapp.ui.mvvm.ArticleListState
import com.example.researcherapp.ui.mvvm.PdfLinkState
import com.example.researcherapp.ui.mvvm.ScienceViewModel

class ScienceFragment : Fragment() {

    private var _binding: FragmentScienceBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ScienceViewModel
    private lateinit var scienceAdapter: ScienceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScienceBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(ScienceViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.buttonGenerateLink.setOnClickListener {
            val doi = binding.editTextDoi.text.toString().trim()
            if (doi.isNotEmpty()) {
                viewModel.generatePdfLink(doi)
            } else {
                Toast.makeText(requireContext(), "Enter DOI", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.fetchArticles()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewRequests.layoutManager = LinearLayoutManager(requireContext())
        scienceAdapter = ScienceAdapter()
        binding.recyclerViewRequests.adapter = scienceAdapter
    }

    private fun observeViewModel() {
        viewModel.articleListState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ArticleListState.Loading -> {
                }
                is ArticleListState.Success -> {
                    scienceAdapter.submitList(state.items)
                }
                is ArticleListState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.pdfLinkState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PdfLinkState.Loading -> {
                    Toast.makeText(requireContext(), "Generating PDF link...", Toast.LENGTH_SHORT).show()
                }
                is PdfLinkState.Success -> {
                    state.pdfLink.let {
                        viewModel.openPdfViewer(requireActivity().supportFragmentManager, it)
                    }
                }
                is PdfLinkState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.popBackStack()
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
