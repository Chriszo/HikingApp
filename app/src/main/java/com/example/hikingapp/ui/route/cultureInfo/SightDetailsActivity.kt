package com.example.hikingapp.ui.route.cultureInfo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.hikingapp.R
import com.example.hikingapp.databinding.ActivitySightDetailsBinding
import com.example.hikingapp.domain.culture.Sight

class SightDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySightDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySightDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nameView = binding.sightName
        val descriptionView = binding.sightState
        val mainPhotoView = binding.sightImage

        val sightInfo = intent.extras!!.get("sightInfo") as Sight

        nameView.text = sightInfo.name
        descriptionView.text = sightInfo.description
        sightInfo.mainPhoto?.let { mainPhotoView.setImageResource(it) }

    }
}