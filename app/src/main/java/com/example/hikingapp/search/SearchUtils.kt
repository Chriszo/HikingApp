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

class SearchUtils {

    companion object {

        @RequiresApi(Build.VERSION_CODES.N)
        fun searchDatabaseByPlace(placeName: String): MutableList<Route> {

            return Optional.ofNullable(MockDatabase.mockSearchResults
                    .stream()
                    .filter { containsKeyword(it.first,placeName) }
                    .map { it.third }
                    .collect(Collectors.toList()))
                    .orElse(listOf())

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

     /*   @RequiresApi(Build.VERSION_CODES.N)
        private fun foundInKeywords(keyword: String?): Boolean {
            MockDatabase.mockSearchResults.stream().map {
                it.first
            }
                .filter{ containsKeyword(it,keyword!!)}
                .

            *//*return MockDatabase.mockKeywords
                .stream()
                .anyMatch { StringUtils.containsIgnoreCase(it,keyword) }*//*
        }*/

        @RequiresApi(Build.VERSION_CODES.N)
        private fun containsKeyword(it: Set<String>, keyword: String): Boolean {

            return it.stream().anyMatch { StringUtils.containsIgnoreCase(it,keyword) }
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