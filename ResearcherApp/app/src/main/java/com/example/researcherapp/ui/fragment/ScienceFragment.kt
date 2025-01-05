package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.researcherapp.data.model.Article
import com.example.researcherapp.data.network.ApiClient
import com.example.researcherapp.databinding.FragmentScienceBinding
import com.example.researcherapp.ui.adapter.ScienceAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScienceFragment : Fragment() {

    private var _binding: FragmentScienceBinding? = null
    private val binding get() = _binding!!
    private val apiService = ApiClient.instance
    private lateinit var scienceAdapter: ScienceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScienceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchArticles()

        binding.buttonGenerateLink.setOnClickListener {
            val doi = binding.editTextDoi.text.toString().trim()
            if (doi.isNotEmpty()) {
                generatePdfLink(doi)
            } else {
                Toast.makeText(requireContext(), "Enter DOI", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewRequests.layoutManager = LinearLayoutManager(requireContext())
        scienceAdapter = ScienceAdapter()
        binding.recyclerViewRequests.adapter = scienceAdapter
    }

    private fun fetchArticles() {
        apiService.getArticles().enqueue(object : Callback<List<Article>> {
            override fun onResponse(call: Call<List<Article>>, response: Response<List<Article>>) {
                if (response.isSuccessful && response.body() != null) {
                    scienceAdapter.submitList(response.body())
                }
            }

            override fun onFailure(call: Call<List<Article>>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to load articles", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generatePdfLink(doi: String) {
        val requestBody = mapOf("doi" to doi)
        apiService.generateScienceLink(requestBody).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful && response.body() != null) {
                    val pdfLink = response.body()?.get("pdf_link")
                    pdfLink?.let {
                        Toast.makeText(requireContext(), "PDF Link Generated", Toast.LENGTH_SHORT).show()
                        openInPdfViewer(it)
                    } ?: Toast.makeText(requireContext(), "PDF link not found", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openInPdfViewer(pdfUrl: String) {
        val fragment = PdfViewerFragment().apply {
            arguments = Bundle().apply {
                putString("pdfUrl", pdfUrl)
            }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(com.example.researcherapp.R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
