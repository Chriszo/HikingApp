package com.example.hikingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.ui.route.RouteFragment

class RouteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)

        val route = intent.extras?.get("route") as Route
        val routeFragment = RouteFragment()
        val bundle = Bundle()
        bundle.putSerializable("route", route)
        routeFragment.arguments = bundle

        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer,routeFragment).commit()
    }
}