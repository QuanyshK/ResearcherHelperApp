package com.example.researcherapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.researcherapp.R
import com.example.researcherapp.data.database.AuthManager
import com.example.researcherapp.databinding.ActivityMainBinding
import com.example.researcherapp.ui.fragment.ChatFragment
import com.example.researcherapp.ui.fragment.LoginFragment
import com.example.researcherapp.ui.fragment.ProfileFragment
import com.example.researcherapp.ui.fragment.ResearchListFragment
import com.example.researcherapp.ui.fragment.ScienceFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var authManager: AuthManager
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)

        replaceFragment(ProfileFragment())

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.chat -> {
                    if (authManager.isLoggedIn()) {
                        replaceFragment(ChatFragment())
                    } else {
                        replaceFragment(LoginFragment())
                    }
                    true
                }
                R.id.search -> {
                    if (authManager.isLoggedIn()) {
                        replaceFragment(ResearchListFragment())
                    } else {
                        replaceFragment(LoginFragment())
                    }
                    true
                }
                R.id.hackLink -> {
                    if (authManager.isLoggedIn()) {
                        replaceFragment(ScienceFragment())
                    } else {
                        replaceFragment(LoginFragment())
                    }
                    true
                }
                R.id.profile -> {
                    if (authManager.isLoggedIn()) {
                        replaceFragment(ProfileFragment())
                    } else {
                        replaceFragment(LoginFragment())
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout)

        if (currentFragment?.javaClass == fragment.javaClass) {
            return
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)

        if (!addToBackStack) {
            supportFragmentManager.popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        } else {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }
}
