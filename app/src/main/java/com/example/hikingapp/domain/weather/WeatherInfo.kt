package com.example.hikingapp.domain.weather

import java.io.Serializable

data class WeatherInfo(
    val time: Long?,
    val summary: String?,
    val icon: String?,
    var temperatureHigh: Double?,
    val temperatureLow: Double?
): Serializable {

}