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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.researcherapp.adapter.ChatAdapter
import com.example.researcherapp.data.database.AuthManager
import com.example.researcherapp.data.model.ChatMessage
import com.example.researcherapp.data.network.ApiClient
import com.example.researcherapp.databinding.FragmentChatBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var authManager: AuthManager
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private var selectedFile: Uri? = null
    private var selectedFileName: String? = null
    private val PICK_FILE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext().applicationContext)
        setupRecyclerView()
        fetchChatHistory()
        setupSendButton()
        setupFilePicker()
        return binding.root
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    private fun setupFilePicker() {
        binding.buttonAttach.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            startActivityForResult(intent, PICK_FILE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedFile = data?.data
            selectedFile?.let {
                selectedFileName = getFileName(it)
                binding.editTextMessage.setText(selectedFileName)
                binding.editTextMessage.isEnabled = false
                Toast.makeText(context, "File Selected: $selectedFileName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchChatHistory() {
        ApiClient.instance.getChatList().enqueue(object : Callback<List<ChatMessage>> {
            override fun onResponse(call: Call<List<ChatMessage>>, response: Response<List<ChatMessage>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        messages.clear()
                        messages.addAll(it)
                        chatAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(call: Call<List<ChatMessage>>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSendButton() {
        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString()
            if (selectedFile != null || message.isNotBlank()) {
                sendMessage(message)
                binding.editTextMessage.text.clear()
                binding.editTextMessage.isEnabled = true
                selectedFile = null
                selectedFileName = null
            } else {
                Toast.makeText(context, "Please write something or attach a file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessage(message: String) {
        val messageBody = RequestBody.create("text/plain".toMediaTypeOrNull(), message)

        val filePart: MultipartBody.Part? = selectedFile?.let {
            val inputStream: InputStream? = context?.contentResolver?.openInputStream(it)
            val tempFile = File.createTempFile("upload", null, requireContext().cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val requestFile = RequestBody.create(context?.contentResolver?.getType(it)?.toMediaTypeOrNull(), tempFile)
            MultipartBody.Part.createFormData("file", getFileName(it) ?: "unknown", requestFile)
        }

        val displayMessage = selectedFileName?.let { "file:$it" } ?: message

        ApiClient.instance.sendMessage(messageBody, filePart)
            .enqueue(object : Callback<ChatMessage> {
                override fun onResponse(call: Call<ChatMessage>, response: Response<ChatMessage>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            val chatMessage = it.copy(user_message = displayMessage)
                            messages.add(chatMessage)
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

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = context?.contentResolver?.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                it.moveToFirst()
                name = it.getString(nameIndex)
            }
        }
        return name ?: uri.lastPathSegment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
