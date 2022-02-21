package com.example.hikingapp.domain.route

import com.example.hikingapp.domain.DifficultyLevel
import com.example.hikingapp.domain.route.enums.RouteType
import java.io.Serializable


class RouteInfo(
    var distance: Double,
    var timeEstimation: Double,
    var routeType: RouteType?,
    var difficultyLevel: DifficultyLevel?,
    var rating: Float?,
    var elevationData: MutableList<Int>?,
): Serializable {


    constructor() : this(0.0, 0.0, null, null, null, null)

}