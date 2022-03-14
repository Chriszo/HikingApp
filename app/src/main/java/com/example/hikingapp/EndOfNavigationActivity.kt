package com.example.hikingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.hikingapp.domain.navigation.UserNavigationData
import com.google.firebase.database.FirebaseDatabase

class EndOfNavigationActivity : AppCompatActivity() {

    private var userNavigationData: UserNavigationData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_of_navigation)

        userNavigationData = intent.extras?.get("userNavigationData") as UserNavigationData?

        val distanceView = findViewById(R.id.distance_covered) as TextView
        val timeSpentView = findViewById(R.id.textView3) as TextView
        val elevationView = findViewById(R.id.elevation) as TextView

        distanceView.text = userNavigationData?.distanceCovered.toString() + " m"
        timeSpentView.text = (userNavigationData?.timeSpent?.div(60000)).toString() +" min"
        elevationView.text = userNavigationData?.currentElevation?.last().toString() + " m "

    }
}