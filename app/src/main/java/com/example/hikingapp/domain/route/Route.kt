package com.example.hikingapp.domain.route

import com.example.hikingapp.domain.culture.CultureInfo
import com.example.hikingapp.domain.map.MapInfo
import com.example.hikingapp.domain.weather.WeatherForecast
import com.example.hikingapp.ui.UIElement
import java.io.Serializable

class Route(
    var routeId: Long,
    var routeName: String?,
    var stateName: String?,
    var mainPhoto: Int?,
    var routeInfo: RouteInfo?,
    var weatherForecast: WeatherForecast?,
    var mapInfo: MapInfo?,
    var cultureInfo: CultureInfo?,
    var photos: List<Int>?
) : Serializable, UIElement {

    constructor() : this(0,null, null, null, null, null, null, null, null)

}