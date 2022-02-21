package com.example.hikingapp.domain.route

import com.example.hikingapp.domain.map.MapInfo
import com.example.hikingapp.domain.weather.WeatherForecast
import com.example.hikingapp.domain.weather.WeatherInfo

class Route(val routeInformation: RouteInfo, val mapInformation: MapInfo) {

     lateinit var weatherInformation: WeatherForecast
}