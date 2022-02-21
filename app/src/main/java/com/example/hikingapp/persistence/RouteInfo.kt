package com.example.hikingapp.persistence

import com.example.hikingapp.domain.DifficultyLevel
import com.example.hikingapp.domain.RouteType
import com.example.hikingapp.domain.culture.Sight
import java.io.Serializable


class RouteInfo(
    var distance: Int,
    var timeEstimation: Double,
    var routeType: RouteType?,
    var difficultyLevel: DifficultyLevel?,
    var elevationData: MutableList<Int>?,
): Serializable {


    constructor() : this(0, 0.0, null, null, null)

}