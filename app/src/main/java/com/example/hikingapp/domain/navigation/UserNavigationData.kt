package com.example.hikingapp.domain.navigation

import java.io.Serializable

class UserNavigationData(var routeId: Long, var distanceCovered: Double, var timeSpent: Long, var currentElevation: MutableList<Long>): Serializable {

    constructor(): this(0L,0.0,0L, mutableListOf())
    constructor(routeId: Long): this(routeId,0.0,0L, mutableListOf())
}