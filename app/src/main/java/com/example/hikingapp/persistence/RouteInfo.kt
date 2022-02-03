package com.example.hikingapp.persistence

import android.media.Rating
import com.example.hikingapp.domain.DifficultyLevel
import com.example.hikingapp.domain.RouteType

class RouteInfo(
    var distance: Double, // in meters
    var timeEstimation: Double, // in minutes
    var routeType: RouteType,
    var difficultyLevel: DifficultyLevel,
    var rating: Float
) {

}