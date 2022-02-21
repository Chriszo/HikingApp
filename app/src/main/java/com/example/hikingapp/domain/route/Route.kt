package com.example.hikingapp.domain.route

import com.example.hikingapp.domain.culture.CultureInfo
import com.example.hikingapp.domain.map.MapInfo
import com.example.hikingapp.domain.weather.WeatherForecast
import java.io.Serializable

class Route(var routeName: String?, var routeInfo: RouteInfo?, var  weatherForecast: WeatherForecast?, var mapInfo: MapInfo?, var cultureInfo: CultureInfo?): Serializable {

    constructor() : this(null, null,null,null, null)

}