package com.example.hikingapp.domain.culture

import android.graphics.Bitmap
import com.example.hikingapp.ui.UIElement
import com.mapbox.geojson.Point
import java.io.Serializable

class Sight(
    var sightId: Long,
    var point: Point?,
    var name: String?,
    var description: String?,
    var rating: Float?,
    var mainPhoto: Int?,
    var photos: MutableList<Bitmap>?
) : Serializable, UIElement {

    constructor() : this(0,null, null, null, null, null, null)
}