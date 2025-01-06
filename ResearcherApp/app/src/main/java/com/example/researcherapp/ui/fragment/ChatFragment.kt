package com.example.researcherapp.ui.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.researcherapp.R
import com.example.researcherapp.adapter.ChatAdapter
import com.example.researcherapp.data.database.AuthManager
import com.example.researcherapp.databinding.FragmentChatBinding
import com.example.researcherapp.ui.mvvm.ChatListState
import com.example.researcherapp.ui.mvvm.ChatViewModel
import com.example.researcherapp.ui.mvvm.SendMessageState

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatAdapter
    private val viewModel by lazy { ViewModelProvider(this).get(ChatViewModel::class.java) }
    private lateinit var authManager: AuthManager
    private val PICK_FILE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authManager = AuthManager(requireContext().applicationContext)
        if (!authManager.isLoggedIn()) {
            redirectToLogin()
        } else {
            setupRecyclerView()
            setupObservers()
            setupFilePicker()
            setupSendButton()
            viewModel.fetchChatHistory()
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    private fun setupObservers() {
        viewModel.chatListState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ChatListState.Success -> {
                    chatAdapter.submitList(state.items) {
                        binding.recyclerViewChat.scrollToPosition(state.items.size - 1)
                    }
                }
                is ChatListState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        viewModel.sendMessageState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SendMessageState.Success -> {
                    chatAdapter.submitList(viewModel.messages.toList()) {
                        binding.recyclerViewChat.scrollToPosition(viewModel.messages.size - 1)
                    }
                }
                is SendMessageState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun setupFilePicker() {
        binding.buttonAttach.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            )
            startActivityForResult(intent, PICK_FILE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedFile = data?.data
            selectedFile?.let {
                viewModel.setSelectedFile(it, getFileName(it))
                binding.editTextMessage.setText(viewModel.selectedFileName)
                binding.editTextMessage.isEnabled = false
            }
        }
    }

    private fun setupSendButton() {
        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString()
            if (message.isNotBlank() || viewModel.selectedFile != null) {
                viewModel.sendMessage(message)
                binding.editTextMessage.text.clear()
                binding.editTextMessage.isEnabled = true
            } else {
                Toast.makeText(requireContext(), "Enter a message or attach a file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            it.moveToFirst()
            it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        } ?: uri.lastPathSegment
    }

    private fun redirectToLogin() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, LoginFragment())
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
