package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.researcherapp.R
import com.example.researcherapp.adapter.ArxivAdapter
import com.example.researcherapp.data.model.ArxivEntry
import com.example.researcherapp.data.model.ArxivFeed
import com.example.researcherapp.data.network.ArxivClient
import com.example.researcherapp.databinding.FragmentResearchListBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResearchListFragment : Fragment() {

    private var _binding: FragmentResearchListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ArxivAdapter

    private var isLoading = false
    private var currentPage = 0
    private val pageSize = 10
    private var isLastPage = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResearchListBinding.inflate(inflater, container, false)
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
                resetPagination()
                fetchArticles(query)
            } else {
                Toast.makeText(context, "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= pageSize
                    ) {
                        fetchArticles(binding.searchEditText.text.toString())
                    }
                }
            }
        })
    }

    private fun fetchArticles(query: String) {
        isLoading = true
        adapter.submitList(adapter.getCurrentList(), isLoading = true)

        ArxivClient.instance.searchArticles(query, currentPage * pageSize, pageSize)
            .enqueue(object : Callback<ArxivFeed> {
                override fun onResponse(call: Call<ArxivFeed>, response: Response<ArxivFeed>) {
                    isLoading = false
                    if (response.isSuccessful) {
                        val entries = response.body()?.entry ?: emptyList()
                        adapter.submitList(adapter.getCurrentList() + entries, false)
                        if (entries.size < pageSize) {
                            isLastPage = true
                        } else {
                            currentPage++
                        }
                    } else {
                        Toast.makeText(context, "Failed to load articles", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<ArxivFeed>, t: Throwable) {
                    isLoading = false
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun resetPagination() {
        currentPage = 0
        isLastPage = false
        adapter.submitList(emptyList(), false)
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
