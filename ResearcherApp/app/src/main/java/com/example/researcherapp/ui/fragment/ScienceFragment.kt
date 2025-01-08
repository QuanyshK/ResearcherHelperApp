package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.researcherapp.R
import com.example.researcherapp.data.database.AuthManager
import com.example.researcherapp.databinding.FragmentScienceBinding
import com.example.researcherapp.ui.adapter.ScienceAdapter
import com.example.researcherapp.ui.mvvm.ScienceViewModel
import com.example.researcherapp.ui.mvvm.ScienceViewModelFactory

class ScienceFragment : Fragment() {

    private var _binding: FragmentScienceBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ScienceViewModel
    private lateinit var adapter: ScienceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScienceBinding.inflate(inflater, container, false)
        val factory = ScienceViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory).get(ScienceViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ScienceAdapter()
        binding.recyclerViewRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewRequests.adapter = adapter

        binding.buttonGenerateLink.setOnClickListener {
            val doi = binding.editTextDoi.text.toString()
            if (doi.isNotEmpty()) {
                viewModel.generateLink(doi, parentFragmentManager)
            }
        }
        val authManager = AuthManager(requireContext())

        if (!authManager.isLoggedIn()) {
            parentFragmentManager.popBackStack()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, LoginFragment())
                .commitAllowingStateLoss()
        }

        viewModel.articles.observe(viewLifecycleOwner) { articles ->
            adapter.submitList(articles)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
