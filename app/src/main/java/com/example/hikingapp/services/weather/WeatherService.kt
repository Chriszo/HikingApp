package com.example.hikingapp.services.weather

import com.example.hikingapp.domain.weather.WeatherInfo
import com.mapbox.geojson.Point

interface WeatherService {

    suspend fun getForecastForDays(point: Point, days: Int, onProdMode: Boolean): List<WeatherInfo>

    fun getForecast(point: Point)

}