package com.example.researcherapp.data.model

data class ChatMessage(
    val id: Int,
    val user_message: String,
    val bot_response: String,
    val file_name: String?,
    val created_at: String
)