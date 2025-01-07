package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.researcherapp.R
import com.example.researcherapp.adapter.ArxivAdapter
import com.example.researcherapp.data.database.AuthManager
import com.example.researcherapp.data.model.ArxivEntry
import com.example.researcherapp.databinding.FragmentResearchListBinding
import com.example.researcherapp.ui.mvvm.ArticlesState
import com.example.researcherapp.ui.mvvm.ResearchViewModel

class ResearchListFragment : Fragment() {

    private var _binding: FragmentResearchListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ResearchViewModel
    private lateinit var adapter: ArxivAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResearchListBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ResearchViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ArxivAdapter { entry ->
            openDetailsFragment(entry)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                viewModel.resetPagination()
                viewModel.searchArticles(query)
            } else {
                Toast.makeText(context, "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }
        val authManager = AuthManager(requireContext())

        if (!authManager.isLoggedIn()) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, LoginFragment())
                .commit()
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!viewModel.isLoading && !viewModel.isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= viewModel.pageSize
                    ) {
                        viewModel.searchArticles(binding.searchEditText.text.toString())
                    }
                }
            }
        })

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.articlesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ArticlesState.Loading -> adapter.setLoading(true)
                is ArticlesState.Success -> {
                    adapter.setLoading(false)
                    adapter.submitList(adapter.getCurrentList() + state.articles)
                }
                is ArticlesState.Error -> {
                    adapter.setLoading(false)
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                is ArticlesState.Reset -> {
                    adapter.submitList(emptyList())
                }
            }
        }
    }

    private fun openDetailsFragment(entry: ArxivEntry) {
        val fragment = ArticleDetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable("article", entry)
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
