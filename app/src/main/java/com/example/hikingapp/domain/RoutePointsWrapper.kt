package com.example.hikingapp.domain

import com.mapbox.geojson.Point
import java.io.Serializable

class RoutePointsWrapper(var routePoints: MutableList<Point>?) : Serializable {

}