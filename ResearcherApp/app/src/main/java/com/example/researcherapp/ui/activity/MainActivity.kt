package com.example.researcherapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.researcherapp.R
import com.example.researcherapp.data.database.AuthManager
import com.example.researcherapp.databinding.ActivityMainBinding
import com.example.researcherapp.ui.fragment.ChatFragment
import com.example.researcherapp.ui.fragment.LoginFragment
import com.example.researcherapp.ui.fragment.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var authManager: AuthManager
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(ChatFragment())
        authManager = AuthManager(this)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.chat -> {
                    replaceFragment(ChatFragment())
                    true
                }
                R.id.profile -> {
                    if (isUserLoggedIn()) {
                        if (currentFragment !is ProfileFragment) {
                            replaceFragment(ProfileFragment())
                        }
                    } else {
                        if (currentFragment !is LoginFragment) {
                            replaceFragment(LoginFragment())
                        }
                    }
                    true
                }
                else -> true
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        supportFragmentManager.popBackStack()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    private fun isUserLoggedIn(): Boolean {
        val authToken = authManager.getAuthToken()
        return !authToken.isNullOrEmpty()
    }
}
