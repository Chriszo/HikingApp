package com.example.hikingapp.domain.route

import com.example.hikingapp.domain.enums.DifficultyLevel
import com.example.hikingapp.domain.enums.RouteType
import com.example.hikingapp.domain.navigation.SerializableMapPoint
import java.io.Serializable


class RouteInfo(
    var distance: Double,
    var timeEstimation: Double,
    var routeType: RouteType?,
    var difficultyLevel: DifficultyLevel?,
    var rating: Float?,
    var elevationData: MutableList<Long>?,
    var navigationData: MutableMap<String, SerializableMapPoint>?
): Serializable {


    constructor() : this(0.0, 0.0, null, null, null, null, null)

}