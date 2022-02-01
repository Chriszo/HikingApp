package com.example.hikingapp.domain

import com.example.hikingapp.persistence.MapInfo
import com.example.hikingapp.persistence.RouteInfo
import com.example.hikingapp.persistence.WeatherInfo

class Route(val routeName: String, val routeInfo: RouteInfo?, val weatherInfo: WeatherInfo?, val mapInfo: MapInfo?) {

}