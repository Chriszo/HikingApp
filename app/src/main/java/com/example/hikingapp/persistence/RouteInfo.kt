package com.example.hikingapp.persistence

import com.example.hikingapp.domain.DifficultyLevel
import com.example.hikingapp.domain.RouteType

class RouteInfo(
    var distance: Int,
    var timeEstimation: Double,
    var routeType: RouteType?,
    var difficultyLevel: DifficultyLevel?,
    var elevationData: MutableList<Int>?
) {


    constructor() : this(0, 0.0, null, null, null)

}