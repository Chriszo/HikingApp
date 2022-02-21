package com.example.hikingapp.domain.route

import com.example.hikingapp.domain.DifficultyLevel
import com.example.hikingapp.domain.route.enums.RouteType

class RouteInfo {
    private var distance: Int = 0
    private var timeEstimation: Double = 0.0
    private lateinit var routeType: RouteType
    private lateinit var difficultyLevel: DifficultyLevel
}