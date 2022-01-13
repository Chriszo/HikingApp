package com.example.hikingapp.persistence

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.MultiLineString
import com.mapbox.geojson.Point

//TODO Move class from this package
class MapInfo(val origin: Point, val destination: Point, val boundingBox: BoundingBox, val jsonRoute: MultiLineString, val routeGeoJsonFileName: String) {

    lateinit var directionInstructions: List<DirectionsRoute>
}