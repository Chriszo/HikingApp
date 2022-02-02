package com.example.hikingapp.persistence

import android.media.Rating
import com.example.hikingapp.domain.DifficultyLevel
import com.example.hikingapp.domain.RouteType

class RouteInfo(
    var distance: Int,
    var timeEstimation: Double,
    var routeType: RouteType,
    var difficultyLevel: DifficultyLevel,
    var rating: Float
) {

}