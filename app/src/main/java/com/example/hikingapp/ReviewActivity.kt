package com.example.hikingapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.databinding.ActivityReviewBinding
import com.example.hikingapp.domain.navigation.UserNavigationData
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.users.reviews.Review
import com.example.hikingapp.persistence.local.LocalDatabase
import com.example.hikingapp.ui.profile.completed.CompletedRouteFragment
import com.example.hikingapp.utils.GlobalUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.HashMap

class ReviewActivity : AppCompatActivity() {

    private var userNavigationData: UserNavigationData? = null
    private lateinit var fromIntent: String
    private lateinit var binding: ActivityReviewBinding
    private lateinit var route: Route
    private var authInfo: FirebaseUser? = null
    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReviewBinding.inflate(layoutInflater)

        setContentView(binding.root)

        route = intent.extras!!["route"] as Route
        authInfo = intent.extras!!["authInfo"] as FirebaseUser?
        fromIntent = intent.extras!!["fromIntent"] as String
        userNavigationData =
            if (intent.extras!!.containsKey("userNavigationData")) intent.extras?.get("userNavigationData") as UserNavigationData? else null

        val mainRoutePhoto = LocalDatabase.getMainImage(route.routeId, route::class.java.simpleName)

        if (Objects.isNull(mainRoutePhoto)) {

            FirebaseStorage.getInstance()
                .getReference("routes/mainPhotos/route_${route.routeId}_main.jpg")
                .getBytes(GlobalUtils.MEGABYTE * 5).addOnSuccessListener {

                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                binding.routeImage.setImageDrawable(BitmapDrawable(resources, bitmap))
            }
        } else {
            binding.routeImage.setImageDrawable(BitmapDrawable(resources, mainRoutePhoto))
        }

        binding.backButton.setOnClickListener {

            var intent: Intent? = null
            when (fromIntent) {
                EndOfNavigationActivity::class.java.simpleName -> {
                    intent = Intent(this@ReviewActivity, EndOfNavigationActivity::class.java)
                    intent.putExtra("userNavigationData", userNavigationData)
                }
                CompletedRouteFragment::class.java.simpleName -> intent =
                    Intent(this@ReviewActivity, MainActivity::class.java)
            }
            intent!!.putExtra("route", route)
            intent.putExtra("authInfo", authInfo)
            startActivity(intent)
        }

        binding.submitReviewButton.setOnClickListener {
            if (binding.ratingValue.rating == 0f && binding.reviewValue.text.toString().isBlank()) {
                Toast.makeText(
                    this,
                    "You have not provided neither a rating nor a review.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                database.getReference("users").child("user${authInfo!!.uid}").addValueEventListener(object: ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        var userName: String? = null

                        if (snapshot.exists()) {
                            userName = (snapshot.value as HashMap<String,*>)["userName"] as String?
                        }
                        val review = Review(
                            null,
                            userName,
                            binding.reviewValue.text.toString(),
                            binding.ratingValue.rating
                        )
                        database.getReference("reviews").child("${route.routeId}/${authInfo!!.uid}")
                            .setValue(review).addOnSuccessListener {
                                Toast.makeText(
                                    this@ReviewActivity,
                                    "Review for route ${route.routeName} submitted successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                                var intent: Intent? = null
                                when (fromIntent) {
                                    EndOfNavigationActivity::class.java.simpleName -> {
                                        intent =
                                            Intent(this@ReviewActivity, EndOfNavigationActivity::class.java)
                                        intent!!.putExtra("userNavigationData", userNavigationData)
                                    }
                                    CompletedRouteFragment::class.java.simpleName -> intent =
                                        Intent(this@ReviewActivity, MainActivity::class.java)
                                }
                                intent!!.putExtra("route", route)
                                intent!!.putExtra("authInfo", authInfo)
                                intent!!.putExtra("reviewSubmitted", true)
                                startActivity(intent)
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            }

        }
    }
}