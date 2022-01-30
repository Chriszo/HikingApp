package com.example.hikingapp.domain.map

import com.mapbox.geojson.Point

open class MapPoint(var point: Point, var elevation: Int?){

    constructor(point: Point) : this(point,-10000)
}