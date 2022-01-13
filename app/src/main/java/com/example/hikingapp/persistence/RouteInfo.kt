package com.example.hikingapp.persistence

import com.example.hikingapp.domain.DifficultyLevel
import com.example.hikingapp.domain.RouteType

class RouteInfo {

    private var distance: Int
    private var timeEstimation: Double
    private var routeType: RouteType
    private var difficultyLevel: DifficultyLevel

    constructor(distance: Int, timeEstimation: Double, routeType: RouteType, difficultyLevel: DifficultyLevel) {
        this.distance = distance
        this.timeEstimation = timeEstimation
        this.routeType = routeType
        this.difficultyLevel = difficultyLevel
    }
}