package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                    binding.profileTitle.text = profile?.username ?: "Unknown User"
                    binding.email.text = profile?.email ?: "No Email"
                } else {
                    logout()
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                logout()
            }
        })
    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        authManager.clearAuthToken()

        parentFragmentManager.popBackStack()
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, LoginFragment())
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
