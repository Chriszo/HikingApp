package com.example.hikingapp.domain.map

import com.mapbox.geojson.Point
import java.io.Serializable

open class MapPoint(var point: Point, var elevation: Int?): Serializable{

    constructor(point: Point) : this(point,-10000)
}