package com.example.hikingapp.persistence.entities

import com.example.hikingapp.domain.enums.DifficultyLevel
import com.example.hikingapp.domain.enums.RouteType
import java.io.Serializable

data class RouteEntity(
    val routeId: Long,
    val routeName: String,
    val stateName: String,
    val mainPhoto: Int,
    val distance: Double,
    val timeEstimation: Double,
    val routeType: RouteType,
    val difficultyLevel: DifficultyLevel,
    val rating: Float
): Serializable
