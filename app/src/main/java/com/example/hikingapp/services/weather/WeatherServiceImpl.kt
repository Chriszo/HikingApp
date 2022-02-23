package com.example.hikingapp.services.weather

import com.example.hikingapp.domain.weather.WeatherInfo
import com.example.hikingapp.domain.weather.WeatherInfoDto
import com.example.hikingapp.domain.weather.WeatherInfoMapper
import com.example.hikingapp.utils.SampleData
import com.mapbox.geojson.Point
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class WeatherServiceImpl : WeatherService {


    override suspend fun getForecastForDays(
        point: Point,
        days: Int,
        onProdMode: Boolean
    ): List<WeatherInfo> {

        val weatherDataResponse: String = if (!onProdMode) {
            SampleData.rawWeatherData
        } else {

            val client = HttpClient()
            val response: HttpResponse = client.get(
                "https://dark-sky.p.rapidapi.com/" + point.latitude() + "," + point.longitude() +
                        "?lang=en&units=auto&exclude=hourly,minutely"
            ) {
                header("x-rapidapi-host", "dark-sky.p.rapidapi.com")
                header("x-rapidapi-key", "22333fdf19msh7342040f2befa30p1305b9jsn53524d7ffd0e")
            }
            response.receive()
        }
        return WeatherInfoMapper.mapList(WeatherInfoDto.fromJsonForDays(weatherDataResponse, days))
    }


    override fun getForecast(point: Point) {
    }
}
