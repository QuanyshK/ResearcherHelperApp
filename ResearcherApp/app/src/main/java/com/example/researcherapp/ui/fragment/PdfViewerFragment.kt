package com.example.researcherapp.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.researcherapp.R
import com.example.researcherapp.databinding.FragmentPdfViewerBinding

class PdfViewerFragment : Fragment() {

    private var _binding: FragmentPdfViewerBinding? = null
    private val binding get() = _binding!!
    private var pdfUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pdfUrl = arguments?.getString("pdfUrl")

        setupWebView()
        setupButtons()
    }

    private fun setupWebView() {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            setBackgroundColor(Color.TRANSPARENT)
            webViewClient = WebViewClient()

            pdfUrl?.let {
                loadUrl("https://docs.google.com/gview?embedded=true&url=$it")
            }
        }
    }

    private fun setupButtons() {
        binding.buttonReload.setOnClickListener {
            pdfUrl?.let { binding.webView.loadUrl("https://docs.google.com/gview?embedded=true&url=$it") }
        }

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
