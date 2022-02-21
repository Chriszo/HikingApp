package com.example.hikingapp.domain

import com.example.hikingapp.domain.culture.CultureInfo
import com.example.hikingapp.domain.map.MapInfo
import com.example.hikingapp.persistence.RouteInfo
import com.example.hikingapp.domain.weather.WeatherInfo
import java.io.Serializable

class Route(var routeName: String?, var routeInfo: RouteInfo?, var  weatherInfo: WeatherInfo?, var mapInfo: MapInfo?, var cultureInfo: CultureInfo?): Serializable {

    constructor() : this(null, null,null,null, null)

}