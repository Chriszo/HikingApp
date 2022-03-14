package com.example.hikingapp.services.culture

import android.graphics.Bitmap
import android.os.Build
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.services.culture.results.SightResponseElement
import com.example.hikingapp.services.culture.results.SightResponseWrapper
import com.google.firebase.database.FirebaseDatabase
import com.mapbox.geojson.Point
import java.util.*
import java.util.stream.Collectors
import kotlin.random.Random

class SightsMapper {

    companion object {

        var sightIdCounter = 0L

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
            sight.sightId = ++sightIdCounter
            sight.point = responseElement.location!!
            sight.name = responseElement.name
            sight.description = responseElement.address
            sight.photos = emptyList<Bitmap>().toMutableList()
            sight.rating = Random.nextDouble(0.0, 5.0).toFloat()
            FirebaseDatabase.getInstance().getReference("sights").child("sight_${sight.sightId}").setValue(sight)
            return sight
        }
    }


    /*

    FOR DB ACTIONS

    @RequiresApi(Build.VERSION_CODES.N)
    fun map(responseWrapper: SightResponseWrapper, route: Route): MutableList<Sight>? {
        var counter = 0
        val data = responseWrapper.results
            ?.stream()
            ?.map { mapToSight(it, route) }
            ?.collect(Collectors.toList())

        val sightEntities = data?.stream()?.map { SightEntity((++counter).toLong(),it.name!!,it.description!!,it.rating!!,0) }!!.collect(Collectors.toList())

        val map = mapOf<String,List<SightEntity>>(Pair(route.routeId.toString(),sightEntities))

         database.getReference("route_sights").updateChildren(map)

        return data
    }

}*/

}