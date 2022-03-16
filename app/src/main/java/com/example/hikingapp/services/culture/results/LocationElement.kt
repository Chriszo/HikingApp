package com.example.hikingapp.services.culture.results

import java.io.Serializable

data class LocationElement(var lat:Double?, var lng: Double?): Serializable {

    constructor(): this(null,null)

}
