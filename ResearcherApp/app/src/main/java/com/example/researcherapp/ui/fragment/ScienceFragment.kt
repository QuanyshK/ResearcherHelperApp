package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.researcherapp.R
import com.example.researcherapp.data.network.ApiClient
import com.example.researcherapp.databinding.FragmentScienceBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScienceFragment : Fragment() {

    private var _binding: FragmentScienceBinding? = null
    private val binding get() = _binding!!
    private val apiService = ApiClient.instance

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

        binding.buttonGenerateLink.setOnClickListener {
            val doi = binding.editTextDoi.text.toString().trim()
            if (doi.isNotEmpty()) {
                generatePdfLink(doi)
            } else {
                Toast.makeText(requireContext(), "Enter DOI or URL", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generatePdfLink(doi: String) {
        val requestBody = mapOf("doi" to doi)
        val call = apiService.generateScienceLink(requestBody)

        call.enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: Response<Map<String, String>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()
                    val pdfLink =data?.get("pdf_link")

                    if (pdfLink != null) {
                        Toast.makeText(requireContext(), "PDF Link Generated", Toast.LENGTH_SHORT).show()
                        openInPdfViewer(pdfLink)
                    } else {
                        Toast.makeText(requireContext(), "PDF link not found", Toast.LENGTH_SHORT).show()
                    }
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
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
