package com.example.hikingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.hikingapp.databinding.ActivitySearchResultsBinding
import com.example.hikingapp.databinding.ActivitySettingsBinding


private lateinit var binding: ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val selectedView = binding.selectedSetting

        val selectedSetting = intent.extras?.get("selectedSetting") as String ?: "Default Value"

        selectedView.text = selectedSetting

    }
}