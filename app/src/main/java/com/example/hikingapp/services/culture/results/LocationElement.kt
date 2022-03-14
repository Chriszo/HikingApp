package com.example.hikingapp.services.culture.results

import java.io.Serializable

data class LocationElement(val lat:Double?, val lng: Double?): Serializable {

    constructor(): this(null,null)

}
