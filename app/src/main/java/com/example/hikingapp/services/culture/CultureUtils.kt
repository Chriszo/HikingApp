package com.example.hikingapp.services.culture

import com.example.hikingapp.domain.culture.CultureInfo
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.domain.culture.SightType
import com.example.hikingapp.services.culture.results.SightResponseWrapper
import com.fasterxml.jackson.databind.ObjectMapper
import com.mapbox.geojson.Point
import okhttp3.OkHttpClient
import okhttp3.Request

class CultureUtils {

    companion object {

        private var radius = 5000
        private var language = "en"

        fun retrieveSightInformation(point: Point): CultureInfo? {

            val client = OkHttpClient()

            val request = Request.Builder()
                .url("https://trueway-places.p.rapidapi.com/FindPlacesNearby?location=${point.latitude()}%2C%20${point.longitude()}&type=${SightType.TA.type}&radius=$radius&language=$language")
                .get()
                .addHeader("x-rapidapi-host", "trueway-places.p.rapidapi.com")
                .addHeader("x-rapidapi-key", "22333fdf19msh7342040f2befa30p1305b9jsn53524d7ffd0e")
                .build()

            val response = client.newCall(request).execute()
            val responseWrapper = transform(response.body!!.string())

            return CultureInfo(SightsMapper.map(responseWrapper))

        }

        private fun transform(responseBody: String): SightResponseWrapper {

            val mapper = ObjectMapper()
            return mapper.readValue(responseBody, SightResponseWrapper::class.java)
        }

    }
}