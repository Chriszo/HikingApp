package com.example.hikingapp.domain.map

import com.mapbox.geojson.Point

class ExtendedMapPoint(point: Point, elevation: Long?, var index: Int) : MapPoint(point, elevation) {

    constructor(point: Point) : this(point, null, -1)
}