package com.example.hikingapp.domain.route

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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
    var mainPhotoBitmap: Bitmap?,
    var routeInfo: RouteInfo?,
    var weatherForecast: WeatherForecast?,
    var mapInfo: MapInfo?,
    var cultureInfo: CultureInfo?,
    var photos: MutableList<Bitmap>?
) : Serializable, UIElement {

    constructor() : this(0,null, null, null, null,null, null, null, null, null)

}