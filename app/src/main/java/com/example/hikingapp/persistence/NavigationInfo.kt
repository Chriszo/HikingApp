package com.example.hikingapp.persistence

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.mapboxsdk.geometry.LatLng

class NavigationInfo {

    private lateinit var destination: LatLng
    private lateinit var directionInstructions: List<DirectionsRoute>
}