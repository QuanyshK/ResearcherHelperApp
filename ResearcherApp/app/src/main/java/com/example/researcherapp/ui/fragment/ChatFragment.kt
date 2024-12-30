package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.researcherapp.adapter.ChatAdapter
import com.example.researcherapp.data.network.ApiClient
import com.example.researcherapp.data.network.ChatRequest
import com.example.researcherapp.data.network.ChatResponse
import com.example.researcherapp.databinding.FragmentChatBinding
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatResponse>()
    private lateinit var webSocket: WebSocket

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupSendButton()
        connectWebSocket()
        return binding.root
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    private fun setupSendButton() {
        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString()
            if (message.isNotBlank()) {
                sendMessage(message)
                binding.editTextMessage.text.clear()
            }
        }
    }

    private fun sendMessage(message: String) {
        val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        messages.add(ChatResponse("You", message, timestamp))
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.recyclerViewChat.scrollToPosition(messages.size - 1)

        val request = ChatRequest(message)
        ApiClient.instance.sendMessage(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                response.body()?.let {
                    messages.add(it)
                    chatAdapter.notifyItemInserted(messages.size - 1)
                    binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                messages.add(ChatResponse("System", "Failed to connect", timestamp))
                chatAdapter.notifyItemInserted(messages.size - 1)
            }
        })

        val json = JSONObject().apply {
            put("message", message)
        }
        webSocket.send(json.toString())
    }

    private fun connectWebSocket() {
        webSocket = ApiClient.connectWebSocket(object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val json = JSONObject(text)
                val message = json.getString("message")
                val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                requireActivity().runOnUiThread {
                    messages.add(ChatResponse("AI", message, timestamp))
                    chatAdapter.notifyItemInserted(messages.size - 1)
                    binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        webSocket.close(1000, "Fragment closed")
    }
}
