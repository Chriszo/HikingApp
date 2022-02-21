package com.example.hikingapp.domain.weather.service

import com.example.hikingapp.domain.weather.WeatherInfo
import com.mapbox.geojson.Point

interface WeatherService {

    suspend fun getForecastForDays(point: Point, days: Int, onTestMode: Boolean): List<WeatherInfo>

    fun getForecast(point: Point)

}