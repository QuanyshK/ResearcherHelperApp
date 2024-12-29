package com.example.researcherapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.researcherapp.adapter.ViewPagerAdapter
import com.example.researcherapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_register -> binding.viewPager.currentItem = 0
                R.id.nav_login -> binding.viewPager.currentItem = 1
            }
            true
        }
    }
}
