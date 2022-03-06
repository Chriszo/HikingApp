package com.example.hikingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.ui.profile.completed.CompletedRouteFragment
import com.example.hikingapp.ui.profile.saved.SavedRouteFragment
import com.example.hikingapp.ui.route.RouteFragment

class RouteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)

        val route = intent.extras?.get("route") as Route
        val action: String? = intent.extras?.get("action") as String?

        val bundle = Bundle()
        bundle.putSerializable("route", route)


        action.apply {
            when(this) {
                "normal" -> {
                    val routeFragment = RouteFragment()
                    routeFragment.arguments = bundle
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer,routeFragment).commit()
                }
                "saved" -> {
                    val savedRoute = SavedRouteFragment()
                    savedRoute.arguments = bundle
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer,savedRoute).commit()
                }
                "completed" -> {
                    val completedRoute = CompletedRouteFragment()
                    completedRoute.arguments = bundle
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer,completedRoute).commit()
                }
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(this.localClassName,"on Destroy called...")
    }
}