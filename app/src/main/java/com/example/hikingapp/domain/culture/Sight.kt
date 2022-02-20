package com.example.hikingapp.domain.culture

import com.mapbox.geojson.Point

class Sight(var point: Point?, var name: String?, var description: String?, var photos: MutableList<Any>?) {

    constructor(): this(null,null,null,null)
}