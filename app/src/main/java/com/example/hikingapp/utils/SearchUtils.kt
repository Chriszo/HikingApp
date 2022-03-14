package com.example.hikingapp.utils

import android.icu.text.Transliterator
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.hikingapp.TransliterationRules
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.search.SearchFiltersWrapper
import com.example.hikingapp.search.SearchType
import com.example.hikingapp.search.searchResults.SearchResult
import com.google.firebase.database.FirebaseDatabase
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.search.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.lang3.StringUtils
import org.apache.commons.text.similarity.LevenshteinDetailedDistance
import java.util.*
import java.util.stream.Collectors
import kotlin.math.*


class SearchUtils {


    companion object {

        private val database: FirebaseDatabase by lazy {
            FirebaseDatabase.getInstance()
        }

        private const val earthRadius = 6371

        private var searchRequestsBuffer: MutableList<Request> = mutableListOf()

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
            "mountain_range",
            "peak"
        )

        @RequiresApi(Build.VERSION_CODES.N)
        fun searchByPlace(
            placeName: String,
            routes: List<Route>,
            keywords: HashMap<String, Long>
        ): MutableList<Route> {

            val foundRouteIds = keywords.entries
                .stream()
                .filter { StringUtils.containsIgnoreCase(it.key, placeName) }
                .map {
                    it.value
                }.distinct().collect(Collectors.toSet())

            return Optional.ofNullable(routes
                .stream()
                .filter { foundRouteIds.contains(it.routeId) }
                .collect(Collectors.toList()).toMutableList()).orElse(
                mutableListOf()
            )
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun searchByPosition(
            userLocation: Point,
            allRoutes: MutableList<Route>,
            originsMap: MutableMap<String, Point>
        ): MutableList<Route> {

            return allRoutes
                .stream()
                .filter { originsMap.containsKey("route_${it.routeId}") }
                .sorted(compareBy {
                    distance(userLocation, originsMap["route_${it.routeId}"]!!)
                })
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
        suspend fun performGeocodingAPICall(
            usersPoint: Point,
            keyword: String,
            routes: List<Route>,
            originsMap: MutableMap<String, Point>
        ): MutableList<Route> {

            val client = OkHttpClient()

            val placesRequest = Request.Builder()
                .url("https://forward-reverse-geocoding.p.rapidapi.com/v1/search?q=$keyword&format=geojson&limit=20&polygon_threshold=0.0")
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

            val userLocationResult = GlobalScope.async {
                return@async client.newCall(usersLocationRequest).execute().body?.string()
            }

            val searchResults = GlobalScope.async {

                val response = client.newCall(placesRequest).execute().body
                val responseCollection = response?.let { FeatureCollection.fromJson(it.string()) }

                return@async Optional.ofNullable(responseCollection?.features()
                    ?.stream()
                    ?.filter {
                        validCategories.contains(it.getStringProperty("category")) && validTypes.contains(
                            it.getStringProperty("type")
                        )
                    }
                    ?.collect(Collectors.toList()))
                    .orElse(mutableListOf())
            }

            val location = userLocationResult.await()?.let {
                if (searchRequestsBuffer.size > 0) searchRequestsBuffer.removeLast()
                SearchResult.fromJson(it)
            }

            val transliterationCountry = TransliterationRules.values()
                .filter { it.name.equals(location?.address?.country_code?.uppercase()) }
                .first()

            var transliterator: Transliterator? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                transliterator = Transliterator.createFromRules(
                    transliterationCountry.name,
                    transliterationCountry.rule,
                    Transliterator.FORWARD
                )
            }

            val foundRoutes: MutableList<Route> = mutableListOf()
            if (searchResults.isCompleted) {
                try {

                    searchResults.await().parallelStream()
                        .map {
                            val displayName = getTransliteratedText(
                                transliterator,
                                it.getStringProperty("display_name")
                            )
                            val point = it.geometry() as Point
                            return@map SearchUtils().FeatureDto(displayName, point, -1.0)
                        }.forEach { dto ->

                            val topResults =
                                routes
                                    .stream()
                                    .map { route ->
                                        val partialDisplayName = dto.displayName.split(",")[0]
                                        val threshold =
                                            if (partialDisplayName.length > route.routeName!!.length) route.routeName!!.length / 2 else partialDisplayName.length / 2
                                        val levenshteinDistance =
                                            LevenshteinDetailedDistance(threshold)
                                        println("Threshold: " + levenshteinDistance.threshold)
                                        println(
                                            "Term1: " + dto.displayName.split(",")[0] + " -- Term2: " + route.routeName + " = " + levenshteinDistance.apply(
                                                dto.displayName.split(",")[0],
                                                route.routeName
                                            ).distance
                                        )

                                        val searchRating = (1.0.div(
                                            levenshteinDistance.apply(
                                                dto.displayName.split(",")[0],
                                                route.routeName
                                            ).distance
                                        )).times(
                                            1.0.div(
                                                distance(
                                                    dto.point,
                                                    originsMap["route_${route.routeId}"]!!
                                                )
                                            )
                                        )
                                        dto.searchRating = searchRating
                                        return@map route
                                    }
                                    .filter {
                                        dto.searchRating > 0
                                    }
                                    .sorted(compareBy {
                                        dto.searchRating
                                    })
                                    .limit(5)
                                    .collect(Collectors.toList())

                            topResults.reverse()
                            topResults.forEach {
                                foundRoutes.add(it)
                            }
                        }
                } catch (e: Exception) {
                    println(e)
                }
            }
            return foundRoutes
            /*forEach {

                val displayName = it.getStringProperty("display_name")


                println(
                    getTransliteratedText(
                        transliterator,
                        displayName
                    )
                )

                val splittedDisplayName = displayName.split(",")


                println("Keyword similarity(Levenstein Distance): " + StringUtils.getLevenshteinDistance(splittedDisplayName[0],keyword))
                println("Eucleidian Distance: " + distance(Point.fromLngLat(location?.lon!!.toDouble(),location?.lat!!.toDouble()),(it.geometry() as Point)))



                println(splittedDisplayName[splittedDisplayName.size-1].trim())
                println(location?.address?.country?.trim())
            }*/

        }

        private fun getTransliteratedText(transliterator: Transliterator?, text: String): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return transliterator!!.transliterate(text)
            }
            return text
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun searchByFilters(
            searchFilters: SearchFiltersWrapper,
            currentRoutes: MutableList<Route>
        ): List<Route> {

            val distanceFilter = if (searchFilters.distance > 0.0) searchFilters.distance else -1.0
            val ratingFilter = if (searchFilters.rating > 0f) searchFilters.rating else -1f

            return currentRoutes
                .stream()
                .filter {
                    (it.routeInfo?.distance!! / 1000.0 <= distanceFilter) || (distanceFilter == -1.0) // find routes with distance less than or equal to provided distance filter
                }
                .filter {
                    (it.routeInfo?.rating!! >= ratingFilter) || (ratingFilter == -1f) // find routes with rating greater than or equal to provided rating filter
                }
                .filter { it.routeInfo?.difficultyLevel == searchFilters.difficultyLevel || searchFilters.difficultyLevel == null }
                .filter { it.routeInfo?.routeType == searchFilters.type || searchFilters.type == null }
                .map { it }
                .sorted(compareBy {
                    it.routeInfo!!.rating  // Sorting by rating. Maybe define a sorting for distance from user's location
                })
                .collect(Collectors.toList())
                .reversed()
        }

    }

    inner class FeatureDto(val displayName: String, val point: Point, var searchRating: Double) {

        override fun toString(): String {
            return "FeatureDto(displayName='$displayName', point='${point.longitude()}','${point.latitude()}')"
        }
    }


}