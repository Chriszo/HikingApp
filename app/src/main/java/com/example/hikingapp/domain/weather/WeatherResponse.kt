package com.example.hikingapp.domain.weather

import com.fasterxml.jackson.annotation.JsonIgnore

class WeatherResponse {

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    lateinit var timezone: String

    @JsonIgnore
    lateinit var currently: String
    lateinit var daily: WeatherInfoWrapper

    @JsonIgnore
    lateinit var alerts: String

    @JsonIgnore
    lateinit var flags: String

    @JsonIgnore
    lateinit var offset: String

}