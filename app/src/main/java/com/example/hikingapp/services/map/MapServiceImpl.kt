package com.example.hikingapp.services.map

import com.example.hikingapp.domain.enums.MapType
import com.example.hikingapp.domain.map.MapInfo
import com.example.hikingapp.domain.map.MapPoint
import com.mapbox.geojson.*

class MapServiceImpl : MapService {

    override fun getMapInformation(jsonContent: String, routeMap: String): MapInfo {

        val routeFeatures = FeatureCollection.fromJson(jsonContent).features()?.get(0)

        val routeJson = when {
            routeFeatures?.geometry() is MultiLineString -> {
                routeFeatures.geometry() as MultiLineString
            }
            routeFeatures?.geometry() is LineString -> {
                routeFeatures.geometry() as LineString
            }
            else -> {
                null
            }
        }

        return initRouteCoordinates(
            routeJson as CoordinateContainer<MutableList<out Any>>?,
            routeMap
        )!!
    }

    private fun initRouteCoordinates(
        routeJson: CoordinateContainer<MutableList<out Any>>?,
        routeMap: String
    ): MapInfo? {

        when (routeJson?.javaClass?.simpleName) {
            MapType.LINE.mapType -> return initializeEdgePoints(routeJson as LineString, routeMap)
            MapType.MULTILINE.mapType -> return initializeEdgePoints(
                routeJson as MultiLineString,
                routeMap
            )
        }
        return null
    }

    private fun initializeEdgePoints(routeContent: MultiLineString, routeMap: String): MapInfo {
        val origin: Point = routeContent.coordinates()[0][0]
        val destination: Point =
            routeContent.coordinates()[0][routeContent.coordinates()[0].size - 1]

        val mapPoints = getMapPoints(routeContent)

        return MapInfo(
            origin,
            destination,
            routeContent.bbox()!!,
            routeContent,
            mapPoints,
            routeMap,
            false
        )
    }

    private fun initializeEdgePoints(routeContent: LineString, routeMap: String): MapInfo {
        val origin: Point = routeContent.coordinates()[0]
        val destination: Point = routeContent.coordinates()[routeContent.coordinates().size - 1]

        val mapPoints = getMapPoints(routeContent)

        return MapInfo(
            origin,
            destination,
            routeContent.bbox(),
            routeContent,
            mapPoints,
            routeMap,
            false
        )
    }


    private fun getMapPoints(json: MultiLineString): List<MapPoint> {
        return json.coordinates()[0].map {
            MapPoint(it)
        }

    }

    private fun getMapPoints(json: LineString): List<MapPoint> {
        return json.coordinates().map {
            MapPoint(it)
        }

    }
}