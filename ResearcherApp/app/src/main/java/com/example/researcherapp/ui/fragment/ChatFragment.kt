package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.researcherapp.R
import com.example.researcherapp.adapter.ChatAdapter
import com.example.researcherapp.data.database.AuthManager
import com.example.researcherapp.data.network.ApiClient
import com.example.researcherapp.data.model.ChatMessage
import com.example.researcherapp.data.network.ChatRequest
import com.example.researcherapp.databinding.FragmentChatBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var authManager: AuthManager
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        setupRecyclerView()
        fetchChatHistory()
        setupSendButton()
        authManager = AuthManager(requireContext().applicationContext)
        return binding.root
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    private fun fetchChatHistory() {
        ApiClient.instance.getChatList()
            .enqueue(object : Callback<List<ChatMessage>> {
                override fun onResponse(
                    call: Call<List<ChatMessage>>,
                    response: Response<List<ChatMessage>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { chatList ->
                            messages.clear()
                            messages.addAll(chatList)
                            chatAdapter.notifyDataSetChanged()
                        }
                    } else {
                        context?.let {
                        }
                    }
                }

                override fun onFailure(call: Call<List<ChatMessage>>, t: Throwable) {
                    context?.let {
                        Toast.makeText(it, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }


    private fun setupSendButton() {
        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString()
            if (authManager.isLoggedIn()){
                if (message.isNotBlank()) {
                    sendMessage(message)
                    binding.editTextMessage.text.clear()
                }
            else {
                    context?.let {
                        Toast.makeText(it, "Please log in to write responses", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun sendMessage(message: String) {
        val request = ChatRequest(message)
        ApiClient.instance.sendMessage(request)
            .enqueue(object : Callback<ChatMessage> {
                override fun onResponse(call: Call<ChatMessage>, response: Response<ChatMessage>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            messages.add(it)
                            chatAdapter.notifyItemInserted(messages.size - 1)
                            binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                        }
                    } else {
                        Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ChatMessage>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
