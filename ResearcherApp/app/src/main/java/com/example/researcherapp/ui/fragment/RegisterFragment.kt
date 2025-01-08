package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.researcherapp.R
import com.example.researcherapp.data.network.ApiClient
import com.example.researcherapp.data.network.RegisterRequest
import com.example.researcherapp.data.network.TokenResponse
import com.example.researcherapp.databinding.FragmentRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerButton.setOnClickListener {
            handleRegister()
        }

        binding.signInText.setOnClickListener {
            replaceFragment(LoginFragment())
        }
    }

    private fun handleRegister() {
        val username = binding.registerUsername.text.toString().trim()
        val email = binding.registerEmail.text.toString().trim()
        val password = binding.registerPassword.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showToast("All fields are required")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address.")
            return
        }

        val request = RegisterRequest(username, email, password)
        ApiClient.instance.register(request).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    showToast("Registration successful! Please log in.")
                    replaceFragment(LoginFragment())
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Username or email already taken. Please try again."
                        500 -> "Registration failed. Try again later."
                        else -> "Unexpected error occurred. Please try again."
                    }
                    showToast(errorMessage)
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                val errorMessage = when (t) {
                    is java.net.UnknownHostException -> "No internet connection. Please try again later."
                    else -> "Failed to register. Error: ${t.localizedMessage}"
                }
                showToast(errorMessage)
            }
        })
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }


    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.popBackStack()
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
