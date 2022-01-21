package com.example.hikingapp.domain.map.service

import com.example.hikingapp.domain.map.MapInfo
import com.example.hikingapp.persistence.mock.db.MockDatabase
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.MultiLineString
import com.mapbox.geojson.Point

class MapServiceImpl: MapService {

    override fun getMapInformation(jsonContent: String): MapInfo {

        val routeJson: MultiLineString =
            FeatureCollection.fromJson(jsonContent).features()?.get(0)?.geometry() as MultiLineString

        val origin: Point = routeJson.coordinates()[0][0]
        val destination: Point = routeJson.coordinates()[0][routeJson.coordinates()[0].size - 1]

        return MapInfo(
            origin,
            destination,
            routeJson.bbox()!!,
            routeJson,
            MockDatabase.routesMap["Philopapou"]?.second!!
        )
    }
}