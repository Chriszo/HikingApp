package com.example.hikingapp.domain.map

import com.example.hikingapp.domain.map.MapPoint
import com.example.hikingapp.domain.weather.WeatherForecast
import com.example.hikingapp.domain.weather.WeatherInfo
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.MultiLineString
import com.mapbox.geojson.Point
import java.io.Serializable

class MapInfo(val origin: Point, val destination: Point, val boundingBox: BoundingBox, val jsonRoute: MultiLineString, val mapPoints: List<MapPoint>?, val routeGeoJsonFileName: String, val elevationDataLoaded: Boolean): Serializable {

    lateinit var directionInstructions: List<DirectionsRoute>
}