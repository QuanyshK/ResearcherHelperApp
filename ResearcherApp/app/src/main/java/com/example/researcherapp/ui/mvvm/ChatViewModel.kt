package com.example.researcherapp.ui.mvvm

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.researcherapp.data.model.ChatMessage
import com.example.researcherapp.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val _chatListState = MutableLiveData<ChatListState>()
    val chatListState: LiveData<ChatListState> get() = _chatListState

    private val _sendMessageState = MutableLiveData<SendMessageState>()
    val sendMessageState: LiveData<SendMessageState> get() = _sendMessageState

    var messages = mutableListOf<ChatMessage>()
    var selectedFile: Uri? = null
    var selectedFileName: String? = null

    fun fetchChatHistory() {
        _chatListState.value = ChatListState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.instance.getChatList().execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        messages.clear()
                        messages.addAll(response.body() ?: emptyList())
                        _chatListState.value = ChatListState.Success(messages)
                    } else {
                        _chatListState.value = ChatListState.Error("Failed to load chat history")
                    }
                }
            } catch (e: Exception) {
                _chatListState.postValue(ChatListState.Error("Error: ${e.localizedMessage}"))
            }
        }
    }

    fun sendMessage(message: String) {
        val userMessage = ChatMessage(
            id = messages.size + 1,
            user_message = selectedFileName ?: message,
            bot_response = "",
            file_name = selectedFileName ?: "",
            created_at = getCurrentTime()
        )

        messages.add(userMessage)
        _sendMessageState.value = SendMessageState.Success(userMessage)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val messageBody = RequestBody.create("text/plain".toMediaTypeOrNull(), message)
                val fileNameBody = RequestBody.create("text/plain".toMediaTypeOrNull(), selectedFileName ?: "")

                val filePart = selectedFile?.let {
                    val tempFile = createTempFileFromUri(it)
                    val requestFile = RequestBody.create(
                        getApplication<Application>().contentResolver.getType(it)?.toMediaTypeOrNull(),
                        tempFile
                    )
                    MultipartBody.Part.createFormData("file", selectedFileName ?: "unknown", requestFile)
                }

                val response = ApiClient.instance.sendMessage(messageBody, filePart, fileNameBody).execute()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { botResponse ->
                            val index = messages.indexOf(userMessage)
                            if (index != -1) {
                                messages[index] = botResponse.copy(user_message = selectedFileName ?: message)
                                _sendMessageState.value = SendMessageState.Success(botResponse)
                                _chatListState.value = ChatListState.Success(messages)
                            }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        _sendMessageState.value = SendMessageState.Error(errorBody)
                    }
                }
            } catch (e: Exception) {
                _sendMessageState.postValue(SendMessageState.Error("Error: ${e.localizedMessage}"))
            }
        }
    }

    fun setSelectedFile(uri: Uri, fileName: String?) {
        selectedFile = uri
        selectedFileName = fileName
    }

    private fun createTempFileFromUri(uri: Uri): File {
        val inputStream: InputStream? = getApplication<Application>().contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload", null, getApplication<Application>().cacheDir)
        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
    }
}


sealed class ChatListState {
    object Loading : ChatListState()
    data class Success(val items: List<ChatMessage>) : ChatListState()
    data class Error(val message: String) : ChatListState()
}

sealed class SendMessageState {
    object Loading : SendMessageState()
    data class Success(val message: ChatMessage) : SendMessageState()
    data class Error(val message: String) : SendMessageState()
}