package com.example.hikingapp.ui.ar

//import com.google.android.gms.maps.model.LatLng
import com.google.ar.sceneform.math.Vector3
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * A model describing details about a Place (location, name, type, etc.).
 */
data class Place(
    val id: String,
    val icon: String,
    val name: String,
    val center: Point
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Place) {
            return false
        }
        return this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }
}

fun Place.getPositionVector(azimuth: Float, latLng: Point): Vector3 {
//    val placeLatLng = this.geometry.location.latLng
    val placeLatLng = this.center
    // TODO compute heading
    val heading = computeHeading(latLng, placeLatLng)
    val r = -2f
    val x = r * sin(azimuth + heading).toFloat()
    val y = 1f
    val z = r * cos(azimuth + heading).toFloat()
    return Vector3(x, y, z)
}

fun computeHeading(from: Point, to: Point): Double {
    // http://williams.best.vwh.net/avform.htm#Crs
    val fromLat = Math.toRadians(from.latitude())
    val fromLng = Math.toRadians(from.longitude())
    val toLat = Math.toRadians(to.latitude())
    val toLng = Math.toRadians(to.longitude())
    val dLng = toLng - fromLng
    val heading = atan2(
        sin(dLng) * cos(toLat),
        cos(fromLat) * sin(toLat) - sin(fromLat) * cos(toLat) * cos(
            dLng
        )
    )
    return wrap(Math.toDegrees(heading), -180.0, 180.0)
}

fun wrap(n: Double, min: Double, max: Double): Double {
    return if (n >= min && n < max) n else mod(n - min, max - min) + min
}

fun mod(x: Double, m: Double): Double {
    return (x % m + m) % m
}


data class Geometry(
    val location: GeometryLocation
)

data class GeometryLocation(
    val lat: Double,
    val lng: Double
) {
    val latLng: LatLng
        get() = LatLng(lat, lng)
}
