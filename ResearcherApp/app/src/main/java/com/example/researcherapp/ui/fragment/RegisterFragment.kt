package com.example.researcherapp.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.researcherapp.R
import com.example.researcherapp.data.database.AuthManager
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
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext().applicationContext)
        loadTokenFromPrefs()
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
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
        } else if (!isValidEmail(email)) {
            Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show()
        } else {
            val request = RegisterRequest(username, email, password)
            ApiClient.instance.register(request).enqueue(object : Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.token?.let { token ->
                            ApiClient.setAuthToken(token)
                            saveTokenToPrefs(token)
                            Toast.makeText(context, "Registered successfully", Toast.LENGTH_SHORT).show()
                            replaceFragment(ProfileFragment())
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(context, "Register failed: $errorBody", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun saveTokenToPrefs(token: String) {
        val prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit().putString("token", token).apply()
    }

    private fun loadTokenFromPrefs() {
        val prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.getString("token", null)?.let {
            ApiClient.setAuthToken(it)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.popBackStack()
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
