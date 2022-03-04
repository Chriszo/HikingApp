package com.example.hikingapp.domain.weather

import java.io.Serializable

class WeatherForecast : Serializable {

    lateinit var weatherForecast: List<WeatherInfo>
}