package com.example.hikingapp.utils

import android.graphics.Bitmap
import com.example.hikingapp.domain.enums.DistanceUnitType
import com.example.hikingapp.domain.route.Route

object GlobalUtils {

    const val LINE_LAYER_ID = "linelayer"
    const val SYMBOL_LAYER_ID = "symbolLayer"
    const val LINE_SOURCE_ID = "line"
    const val SYMBOL_SOURCE_ID = "symbol"
    const val RED_MARKER_ID = "red_marker_id"
    const val TERRAIN_ID = "mapbox.mapbox-terrain-v2"
    const val TILEQUERY_GEOMETRY_ID = "polygon"
    const val TILEQUERY_ATTRIBUTE_REQUESTED_ID = "contour"

    const val MEGABYTE = 1024L * 1024L
    const val CAMERA_REQUEST = 1888


    fun getRoutePair(route: Route): Pair<Route, Bitmap?> {

        val photoBitmap = route.mainPhotoBitmap
        route.mainPhotoBitmap = null

        return Pair(route, photoBitmap)
    }

    fun getTwoDigitsDistance(
        rawDistance: Double,
        distanceUnitType: DistanceUnitType
    ): String {
        /*if (DistanceUnitType.KILOMETERS == distanceUnitType) {
            return String.format("%.2f", rawDistance).toDouble()
                .div(1000.0)
        }
        return String.format("%.2f", rawDistance).toDouble()*/
        when (distanceUnitType) {
            DistanceUnitType.METERS -> return String.format(
                "%.2f",
                rawDistance
            ) + DistanceUnitType.METERS.distanceUnit
            DistanceUnitType.KILOMETERS -> return String.format(
                "%.2f",
                (rawDistance.div(1000.0))
            ) + DistanceUnitType.KILOMETERS.distanceUnit
        }
    }

    fun getTimeInMinutes(seconds: Double): Double {
        return seconds.div(60.0)
    }

}