package com.example.hikingapp.domain.route

import com.example.hikingapp.domain.enums.DifficultyLevel
import com.example.hikingapp.domain.enums.RouteType
import java.io.Serializable


class RouteInfo(
    var distance: Double,
    var timeEstimation: Double,
    var routeType: RouteType?,
    var difficultyLevel: DifficultyLevel?,
    var rating: Float?,
    var elevationData: MutableList<Long>?,
): Serializable {


    constructor() : this(0.0, 0.0, null, null, null, null)

}