package com.example.hikingapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.databinding.ActivitySettingsBinding


private lateinit var binding: ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.extras?.containsKey("user") == true && intent.extras!!["user"] != null) {
            binding = ActivitySettingsBinding.inflate(layoutInflater)

            setContentView(binding.root)

            val selectedView = binding.selectedSetting

            val selectedSetting = intent.extras?.get("selectedSetting") as String

            selectedView.text = selectedSetting
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }
}