package com.example.hikingapp.domain.route

import com.example.hikingapp.domain.DifficultyLevel
import com.example.hikingapp.domain.route.enums.RouteType

class RouteInfo(
    private var distance: Int,
    private var timeEstimation: Double,
    private var routeType: RouteType,
    private var difficultyLevel: DifficultyLevel
) {

}