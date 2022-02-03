package com.example.hikingapp.search

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.hikingapp.domain.Route
import com.example.hikingapp.persistence.mock.db.MockDatabase
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.search.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors
import kotlin.math.*


// TODO Change mock data with Firebase Realtime Database data
class SearchUtils {


    companion object {

        private const val earthRadius = 6371

        private val validCategories = setOf<String>(
            "waterway",
            "natural",
            "tourism"
        )

        private val validTypes = setOf<String>(
            "river",
            "lake",
            "attraction",
            "mountain",
            "mountain_range"
        )

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

        @RequiresApi(Build.VERSION_CODES.N)
        fun performGeocodingAPICall(
            usersPoint: Point,
            newText: String
        ) {
            /*return searchEngine.search(
                newText,
                searchQueryOptions, searchCallback
            )*/

            val client = OkHttpClient()

            val placesRequest = Request.Builder()
                .url("https://forward-reverse-geocoding.p.rapidapi.com/v1/search?q=$newText&format=geojson&limit=20&polygon_threshold=0.0")
                .get()
                .addHeader("x-rapidapi-host", "forward-reverse-geocoding.p.rapidapi.com")
                .addHeader("x-rapidapi-key", "22333fdf19msh7342040f2befa30p1305b9jsn53524d7ffd0e")
                .build()

            val usersLocationRequest = Request.Builder()
                .url("https://forward-reverse-geocoding.p.rapidapi.com/v1/reverse?lat=" + usersPoint.latitude() + "&lon=" + usersPoint.longitude() + "&accept-language=en&polygon_threshold=0.0")
                .get()
                .addHeader("x-rapidapi-host", "forward-reverse-geocoding.p.rapidapi.com")
                .addHeader("x-rapidapi-key", "22333fdf19msh7342040f2befa30p1305b9jsn53524d7ffd0e")
                .build()

            val userLocationJob = GlobalScope.launch {
                val response = client.newCall(usersLocationRequest).execute().body

                println(response!!.string())
            }

            GlobalScope.launch {
                val response = client.newCall(placesRequest).execute().body

                val responseCollection = response?.let { FeatureCollection.fromJson(it.string()) }

                val validResults = responseCollection?.features()!!
                    .stream()
                    .filter{
                        validCategories.contains(it.getStringProperty("category")) && validTypes.contains(it.getStringProperty("type"))
                    }
                    .map {
                        it.geometry()
                    }
                    .collect(Collectors.toList())
                println(validResults)
            }

            println("Call made")
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun searchByFilters(searchFilters: SearchFiltersWrapper): List<Route> {

            val distanceFilter = if (searchFilters.distance > 0.0) searchFilters.distance else -1.0
            val ratingFilter = if (searchFilters.rating > 0f) searchFilters.rating else -1f

            return MockDatabase.mockSearchResults
                .stream()
                .filter {
                    (it.third.routeInfo?.distance!! / 1000.0 <= distanceFilter) || (distanceFilter == -1.0) // find routes with distance less than or equal to provided distance filter
                }
                .filter {
                    (it.third.routeInfo?.rating!! >= ratingFilter) || (ratingFilter == -1f) // find routes with rating greater than or equal to provided rating filter
                }
                .filter { it.third.routeInfo?.difficultyLevel == searchFilters.difficultyLevel || searchFilters.difficultyLevel == null }
                .filter { it.third.routeInfo?.routeType == searchFilters.type || searchFilters.type == null }
                .map { it.third }
                .collect(Collectors.toList())
        }

    }


}