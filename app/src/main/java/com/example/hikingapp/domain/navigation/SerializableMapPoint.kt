package com.example.hikingapp.domain.navigation

import java.io.Serializable

class SerializableMapPoint (val pointId: String, val index: Long, val longitude: Double, val latitude: Double, var elevation: Long?): Serializable {
}