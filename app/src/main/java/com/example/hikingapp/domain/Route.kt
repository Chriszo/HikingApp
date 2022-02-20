package com.example.hikingapp.domain

import com.example.hikingapp.domain.culture.CultureInfo
import com.example.hikingapp.persistence.MapInfo
import com.example.hikingapp.persistence.RouteInfo
import com.example.hikingapp.persistence.WeatherInfo

class Route(var routeName: String?, var routeInfo: RouteInfo?, var  weatherInfo: WeatherInfo?, var mapInfo: MapInfo?, var cultureInfo: CultureInfo?) {

    constructor() : this(null, null,null,null, null)

}