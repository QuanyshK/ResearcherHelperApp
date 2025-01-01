package com.example.researcherapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.researcherapp.data.model.ChatMessage
import com.example.researcherapp.databinding.ItemChatBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    inner class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            val formattedTime = formatTime(message.created_at)

            if (message.bot_response.isNotEmpty() && message.bot_response != "Processing...") {
                binding.textViewBotResponse.visibility = View.VISIBLE
                binding.textViewBotResponse.text = message.bot_response
            } else {
                binding.textViewBotResponse.visibility = View.GONE
            }

            if (!message.file_name.isNullOrEmpty()) {
                binding.textViewUserMessage.visibility = View.VISIBLE
                binding.textViewUserMessage.text = "📎 ${message.file_name}"
            } else if (message.user_message.isNotEmpty()) {
                binding.textViewUserMessage.visibility = View.VISIBLE
                binding.textViewUserMessage.text = message.user_message
            } else {
                binding.textViewUserMessage.visibility = View.GONE
            }

            binding.textViewTimestamp.text = formattedTime
        }

        private fun formatTime(date: String): String {
            return try {
                val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val targetFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val parsedDate: Date = originalFormat.parse(date) ?: Date()
                targetFormat.format(parsedDate)
            } catch (e: Exception) {
                "Unknown Time"
            }
        }
    }
}
