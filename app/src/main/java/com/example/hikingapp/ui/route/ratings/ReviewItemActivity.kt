package com.example.hikingapp.ui.route.ratings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.databinding.ActivityReviewItemBinding
import com.example.hikingapp.domain.users.reviews.Review

class ReviewItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReviewItemBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (intent.extras?.containsKey("review") == true) {
            val review = intent.extras!!.get("review") as Review

            binding.userName.text = review.userName
            binding.reviewDescription.text = review.review
            binding.reviewRating.rating = review.rating?.toFloat()!!
        }
    }
}