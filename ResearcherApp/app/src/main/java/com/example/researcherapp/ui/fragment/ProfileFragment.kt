package com.example.researcherapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.researcherapp.R
import com.example.researcherapp.databinding.FragmentProfileBinding
import com.example.researcherapp.data.network.ApiClient
import com.example.researcherapp.data.network.ProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        loadProfile()
        setupLogoutButton()
        return binding.root
    }

    private fun loadProfile() {
        ApiClient.instance.getProfile().enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    binding.profileTitle.text = profile?.username
                    binding.email.text = profile?.email
                } else {
                    binding.profileTitle.text = "Failed to load profile"
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                binding.profileTitle.text = "Error loading profile"
            }
        })
    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        val prefs = requireContext().getSharedPreferences("auth", 0)
        prefs.edit().remove("token").apply()

        parentFragmentManager.popBackStack()
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, LoginFragment())
            .commitAllowingStateLoss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
