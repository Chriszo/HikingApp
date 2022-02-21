package com.example.hikingapp.domain.weather

data class WeatherInfo(
    val time: Long?,
    val summary: String?,
    val icon: String?,
    var temperatureHigh: Double?,
    val temperatureLow: Double?
) {

}