package com.example.hikingapp.domain.culture

import android.graphics.Bitmap
import com.example.hikingapp.services.culture.results.LocationElement
import com.example.hikingapp.ui.UIElement
import java.io.Serializable

class Sight(
    var sightId: Long,
    var point: LocationElement?,
    var name: String?,
    var description: String?,
    var rating: Float?,
    @Transient var mainPhoto: Bitmap?,
    @Transient var photos: MutableList<Bitmap>?
) : Serializable, UIElement {

    constructor() : this(0, null, null, null, null, null, null)
}