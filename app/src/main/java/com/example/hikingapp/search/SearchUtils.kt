package com.example.hikingapp.search

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.hikingapp.domain.Route
import com.example.hikingapp.persistence.mock.db.MockDatabase
import com.mapbox.geojson.Point
import com.mapbox.search.*
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.stream.Collectors
import kotlin.math.*

class SearchUtils {


    companion object {

        private const val earthRadius = 6371

        @RequiresApi(Build.VERSION_CODES.N)
        fun searchByPlace(placeName: String): MutableList<Route> {

            return Optional.ofNullable(MockDatabase.mockSearchResults
                .stream()
                .filter { containsKeyword(it.first, placeName) }
                .map { it.third }
                .collect(Collectors.toList()))
                .orElse(listOf())

        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun searchByPosition(userLocation: Point): MutableList<Route> {

            return MockDatabase.mockSearchResults
                .stream()
                .sorted(compareBy {
                    distance(userLocation, it.second)
                })
                .map {
                    it.third
                }
                .collect(Collectors.toList())

        }

        private fun distance(point1: Point, point2: Point): Double {

            val latDistance = Math.toRadians(point2.latitude() - point1.latitude())
            val lonDistance = Math.toRadians(point2.longitude() - point1.longitude())

            val a = (sin(latDistance / 2) * sin(latDistance / 2)
                    + (cos(Math.toRadians(point1.latitude())) * cos(Math.toRadians(point2.latitude()))
                    * sin(lonDistance / 2) * sin(lonDistance / 2)))

            val c = a * atan2(sqrt(a), sqrt(1 - a))

            var distance = c * earthRadius * 1000 // distance in meters

            val elevationDifference =
                if ((point1.altitude() - point2.altitude()).isNaN()) 0.0 else point1.altitude() - point2.altitude()

            distance = distance.pow(2.0) + elevationDifference.pow(2.0)

            return sqrt(distance)
        }


        fun defineSearchQueryOptions(searchType: SearchType): SearchOptions {

            val searchBuilder = SearchOptions.Builder()
                .types(QueryType.PLACE, QueryType.POI, QueryType.REGION)
                .fuzzyMatch(true)
                .limit(5)

            return when (searchType) {
                SearchType.BY_PLACE -> searchBuilder.build()
                SearchType.BY_POSITION -> searchBuilder.proximity(Point.fromLngLat(0.0, 0.0))
                    .build()
                SearchType.BY_FILTERS -> searchBuilder.build() // TODO to be continued
            }
        }

        @RequiresApi(Build.VERSION_CODES.N)
        private fun containsKeyword(it: Set<String>, keyword: String): Boolean {

            return it.stream().anyMatch { StringUtils.containsIgnoreCase(it, keyword) }
        }

        fun performGeocodingAPICall(
            searchEngine: SearchEngine,
            newText: String,
            searchQueryOptions: SearchOptions,
            searchCallback: SearchSelectionCallback,
        ): SearchRequestTask {
            return searchEngine.search(
                newText,
                searchQueryOptions, searchCallback
            )
        }

    }


}