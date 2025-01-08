package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.researcherapp.R
import com.example.researcherapp.data.database.AuthManager
import com.example.researcherapp.data.network.ApiClient
import com.example.researcherapp.data.network.ProfileResponse
import com.example.researcherapp.databinding.FragmentProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext().applicationContext)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfile()
        setupLogoutButton()
    }
    private fun loadProfile() {
        ApiClient.instance.getProfile().enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    binding.profileTitle.text = profile?.username ?: "User"
                    binding.email.text = profile?.email ?: "No Email"
                } else {
                    handleProfileError(response.code())
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                val errorMessage = when (t) {
                    is java.net.SocketTimeoutException -> "Loading profile timed out. Please refresh."
                    is java.net.UnknownHostException -> "No internet connection. Try again later."
                    else -> "Failed to load profile. Please try again."
                }
                showToast(errorMessage)
                logout()
            }
        })
    }

    private fun handleProfileError(code: Int) {
        when (code) {
            401 -> {
                showToast("Session expired. Please log in again.")
                logout()
            }
            500 -> showToast("Server error. Please try again later.")
            else -> showToast("Unexpected error. Please refresh.")
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        authManager.clearAuthToken()


        activity?.let {
            it.supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, LoginFragment())
                .commit()
        } ?: run {
            Toast.makeText(context, "Fragment not attached", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
