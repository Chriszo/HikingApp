package com.example.hikingapp.services.culture

import android.os.Build
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.services.culture.results.SightResponseElement
import com.example.hikingapp.services.culture.results.SightResponseWrapper
import com.mapbox.geojson.Point
import java.util.stream.Collectors

class SightsMapper {

    companion object {

        fun map(responseWrapper: SightResponseWrapper): MutableList<Sight>? {

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                responseWrapper.results
                    ?.stream()
                    ?.map { mapToSight(it) }
                    ?.collect(Collectors.toList())
            } else {
                TODO("VERSION.SDK_INT < N")
            }
        }

        private fun mapToSight(responseElement: SightResponseElement): Sight {

            val sight = Sight()
            sight.point = Point.fromLngLat(
                responseElement.location!!.lng!!.toDouble(),
                responseElement.location.lat!!.toDouble()
            )
            sight.name = responseElement.name
            sight.description = responseElement.address
            sight.photos = emptyList<Any>().toMutableList()

            return sight
        }

    }

}