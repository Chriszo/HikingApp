package com.example.hikingapp.domain.culture

import android.graphics.drawable.Drawable
import com.mapbox.geojson.Point
import java.io.Serializable

class Sight(
    var point: Point?,
    var name: String?,
    var description: String?,
    var rating: Float?,
    var mainPhoto: Int?,
    var photos: MutableList<Drawable>?
) : Serializable {

    constructor() : this(null, null, null, null, null, null)
}