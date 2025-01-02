package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResearchListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ArxivAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString()
            if (query.isNotEmpty()) {
                fetchArticles(query)
            } else {
                Toast.makeText(context, "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchArticles(query: String) {
        binding.progressBar.visibility = View.VISIBLE

        ArxivClient.instance.searchArticles(query).enqueue(object : Callback<ArxivFeed> {
            override fun onResponse(call: Call<ArxivFeed>, response: Response<ArxivFeed>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val entries = response.body()?.entry ?: emptyList()
                    adapter.submitList(entries)
                } else {
                    Toast.makeText(context, "Failed to load articles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ArxivFeed>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}