package com.example.hikingapp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.hikingapp.domain.enums.DistanceUnitType
import com.example.hikingapp.domain.route.Route
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement

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

    const val LAST_PAGE = "lastPage"

    const val routeIdsForNavigation = "routeIdsForNavigation"


    fun getRoutePair(route: Route): Pair<Route, Bitmap?> {

        val photoBitmap = route.mainPhotoBitmap
        route.mainPhotoBitmap = null

        return Pair(route, photoBitmap)
    }

    fun getTwoDigitsDistance(
        rawDistance: Double,
        distanceUnitType: DistanceUnitType,
        transform: Boolean = true
    ): String {
        /*if (DistanceUnitType.KILOMETERS == distanceUnitType) {
            return String.format("%.2f", rawDistance).toDouble()
                .div(1000.0)
        }
        return String.format("%.2f", rawDistance).toDouble()*/

        if (transform) {
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
        } else {
            return String.format("%.2f", rawDistance) + DistanceUnitType.KILOMETERS.distanceUnit
        }

    }

    fun getTimeInMinutes(seconds: Double): Double {
        return seconds.div(60.0)
    }

    fun decodeSampledBitmapFromByteArray(
        byteArray: ByteArray,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun convertToCelsius(temperature: Double): Double {
        return ((temperature - 32) * 5) / 9
    }

    fun convertToFahrenheit(temperature: Double): Double {
        return ((temperature * 9) / 5) + 32
    }

    fun getMetric(value: Double, unit: String?): String {

        return when (unit?.trim()) {
            "m" -> "${"%.2f".format(value)} m"
            "km" -> "${"%.2f".format((value / 1000))} km"
            "mi" -> "${"%.2f".format((value / 1000) * 0.6213712)} mi"
            "cm" -> "${"%.2f".format((value * 100))} cm"
            else -> "${"%.2f".format(value)} m"
        }
    }

    fun getTime(timeEstimation: Double?, unit: String?): String {

        return when (unit?.trim()) {
            "sec" -> "${"%.2f".format(timeEstimation?.times(60))} sec"
            "hrs" -> "${"%.2f".format(timeEstimation?.div(60))} hrs"
            "min" -> "${"%.2f".format(timeEstimation)} min"
            else -> "${"%.2f".format(timeEstimation)} min"
        }
    }

}