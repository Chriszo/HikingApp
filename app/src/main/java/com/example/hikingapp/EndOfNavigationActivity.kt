package com.example.hikingapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.databinding.ActivityEndOfNavigationBinding
import com.example.hikingapp.domain.enums.DistanceUnitType
import com.example.hikingapp.domain.navigation.UserNavigationData
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.utils.GlobalUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage

class EndOfNavigationActivity : AppCompatActivity() {

    private var authInfo: FirebaseUser? = null
    private var currentRoute: Route? = null
    private var userNavigationData: UserNavigationData? = null

    private lateinit var binding: ActivityEndOfNavigationBinding

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEndOfNavigationBinding.inflate(layoutInflater)

        setContentView(binding.root)

        userNavigationData = intent.extras?.get("userNavigationData") as UserNavigationData?
        currentRoute = intent.extras?.get("route") as Route?
        authInfo = intent.extras?.get("authInfo") as FirebaseUser?


        val distanceView =
            binding.distanceCovered // findViewById(R.id.distance_covered) as TextView
        val timeSpentView = binding.timeElapsed // findViewById(R.id.textView3) as TextView
        val averageElevation = binding.averageElevation // findViewById(R.id.textView3) as TextView
        val shareButton = binding.shareButton
        val reviewButton = binding.reviewButton

        binding.successText.text = "You have successfully completed the route ${currentRoute?.routeName}!"
        distanceView.text =
            "Distance covered: " + GlobalUtils.getTwoDigitsDistance(userNavigationData?.distanceCovered ?: 0.0, DistanceUnitType.KILOMETERS)
        timeSpentView.text =
            "Time elapsed: " + GlobalUtils.getTimeInMinutes((userNavigationData?.timeSpent?.toDouble()
                ?.times(1000.0)) ?:0.0).toString() + " min"
        averageElevation.text =
            "Average elevation: " + GlobalUtils.getTwoDigitsDistance(userNavigationData!!.currentElevation.stream().mapToLong { it }
                .average().orElse(0.0),DistanceUnitType.METERS)

        shareButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "Check out this amazing hiking route ${currentRoute!!.routeName}"
            )
            startActivity(Intent.createChooser(intent, "Share Route"))
        }

        reviewButton.setOnClickListener {
            startActivity(Intent(this, ReviewActivity::class.java))
        }

        binding.goToHome.setOnClickListener {
            val homeIntent = Intent(this, MainActivity::class.java)
            homeIntent.putExtra("authInfo", authInfo)
            startActivity(homeIntent)
        }

    }
}