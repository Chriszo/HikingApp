package com.example.hikingapp.domain.weather

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
@JsonIgnoreProperties(
    "sunriseTime",
    "sunsetTime",
    "moonPhase",
    "temperatureHighTime",
    "temperatureLowTime",
    "apparentTemperatureHigh",
    "apparentTemperatureHighTime",
    "apparentTemperatureLow",
    "apparentTemperatureLowTime",
    "dewPoint",
    "humidity",
    "pressure",
    "windSpeed",
    "windGust",
    "windGustTime",
    "windBearing",
    "cloudCover",
    "uvIndex",
    "uvIndexTime",
    "visibility",
    "ozone",
    "temperatureMin",
    "temperatureMinTime",
    "temperatureMax",
    "temperatureMaxTime",
    "apparentTemperatureMin",
    "apparentTemperatureMinTime",
    "apparentTemperatureMax",
    "apparentTemperatureMaxTime"
)
class WeatherInfoDto private constructor(){

    var time = 0L
    lateinit var summary: String
    lateinit var icon: String
    var sunriseTime = 0L
    var sunsetTime = 0L
    var moonPhase = 0.0
    var precipIntensity = 0.0
    var precipIntensityMax = 0.0
    var precipIntensityMaxTime = 0.0
    var precipProbability = 0.0
    lateinit var precipType: String
    var temperatureHigh = 0.0
    var temperatureHighTime = 0.0
    var temperatureLow = 0.0
    var temperatureLowTime = 0.0
    var apparentTemperatureHigh = 0.0
    var apparentTemperatureHighTime = 0.0
    var apparentTemperatureLow = 0.0
    var apparentTemperatureLowTime = 0.0
    var dewPoint = 0.0
    var humidity = 0.0
    var pressure = 0.0
    var windSpeed = 0.0
    var windGust = 0.0
    var windGustTime = 0L
    var windBearing = 0
    var cloudCover = 0.0
    var uvIndex = 0
    var uvIndexTime = 0L
    var visibility = 0.0
    var ozone = 0.0
    var temperatureMin = 0.0
    var temperatureMinTime = 0L
    var temperatureMax = 0.0
    var temperatureMaxTime = 0L
    var apparentTemperatureMin = 0.0
    var apparentTemperatureMinTime = 0L
    var apparentTemperatureMax = 0.0
    var apparentTemperatureMaxTime = 0L

    companion object {

        fun fromJson(payload: String): WeatherInfoDto {
            val objectMapper = ObjectMapper()
            val weatherResponse = objectMapper.readValue(payload, WeatherResponse::class.java)
            return weatherResponse.daily.data[0]
        }

        fun fromJsonForDays(payload: String, days: Int): List<WeatherInfoDto> {
            val objectMapper = ObjectMapper()
            val weatherResponse = objectMapper.readValue(payload, WeatherResponse::class.java)
            return weatherResponse.daily.data.subList(0,days-1)
        }

    }

}