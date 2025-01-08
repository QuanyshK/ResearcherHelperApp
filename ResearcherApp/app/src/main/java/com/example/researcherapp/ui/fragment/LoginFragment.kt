package com.example.researcherapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.researcherapp.R
import com.example.researcherapp.data.database.AuthManager
import com.example.researcherapp.data.network.ApiClient
import com.example.researcherapp.data.network.LoginRequest
import com.example.researcherapp.data.network.TokenResponse
import com.example.researcherapp.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var authManager: AuthManager

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext().applicationContext)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.loginButton.setOnClickListener {
            handleLogin()
        }

        binding.signUpText.setOnClickListener {
            replaceFragment(RegisterFragment())
        }
    }

    private fun handleLogin() {
        val username = binding.loginUsername.text.toString().trim()
        val password = binding.loginPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            showToast("Please fill in both fields")
            return
        }

        val request = LoginRequest(username, password)
        ApiClient.instance.login(request).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    response.body()?.token?.let { token ->
                        ApiClient.setAuthToken(token)
                        authManager.saveAuthToken(token)
                        showToast("Welcome back, $username!")
                        replaceFragment(ProfileFragment())
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Incorrect username or password. Please try again."
                        401 -> "Unauthorized. Please check your login credentials."
                        500 -> "Server error. Please try again later."
                        else -> "Login failed. Please check your internet connection and try again."
                    }
                    showToast(errorMessage)
                    Log.e("LoginFragment", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                val errorMessage = when (t) {
                    is java.net.SocketTimeoutException -> "Connection timed out. Please try again."
                    is java.net.UnknownHostException -> "No internet connection. Please check your network."
                    else -> "Unexpected error occurred: ${t.localizedMessage}"
                }
                showToast(errorMessage)
                Log.e("LoginFragment", "Error: ${t.message}")
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
