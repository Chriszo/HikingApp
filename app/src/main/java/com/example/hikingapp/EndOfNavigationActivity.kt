package com.example.hikingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.hikingapp.databinding.ActivityEndOfNavigationBinding
import com.example.hikingapp.domain.navigation.UserNavigationData
import com.example.hikingapp.domain.route.Route
import com.google.firebase.database.FirebaseDatabase

class EndOfNavigationActivity : AppCompatActivity() {

    private var currentRoute: Route? = null
    private var userNavigationData: UserNavigationData? = null

    private lateinit var binding: ActivityEndOfNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEndOfNavigationBinding.inflate(layoutInflater)

        setContentView(binding.root)

        userNavigationData = intent.extras?.get("userNavigationData") as UserNavigationData?
        currentRoute = intent.extras?.get("route") as Route?

        val distanceView = binding.distanceCovered // findViewById(R.id.distance_covered) as TextView
        val timeSpentView = binding.textView3 // findViewById(R.id.textView3) as TextView
        val elevationView = binding.elevation //findViewById(R.id.elevation) as TextView
        val shareButton = binding.shareButton
        val reviewButton = binding.reviewButton

        distanceView.text = userNavigationData?.distanceCovered.toString() + " m"
        timeSpentView.text = (userNavigationData?.timeSpent?.div(60000)).toString() +" min"
        elevationView.text = userNavigationData?.currentElevation?.last().toString() + " m "

        shareButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "Check out this amazing hiking route ${currentRoute!!.routeName}")
            startActivity(Intent.createChooser(intent, "Share Route"))
        }

        reviewButton.setOnClickListener {
            startActivity(Intent(this,ReviewActivity::class.java))
        }

    }
}