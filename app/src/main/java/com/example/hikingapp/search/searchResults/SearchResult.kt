package com.example.hikingapp.search.searchResults

import com.fasterxml.jackson.databind.ObjectMapper

data class SearchResult(
    val licence: String?,
    val osm_id: Long,
    val address: Address?,
    val osm_type: String?,
    val boundingbox: List<String?>?,
    val place_id: Long,
    val lat: String?,
    val lon: String?,
    val display_name: String?
){

    constructor() : this(null,0,null,null,null,0,null,null,null)

    companion object{

        fun fromJson(resultsTextResponse: String): SearchResult {

            val objectMapper = ObjectMapper()
            return objectMapper.readValue<SearchResult>(resultsTextResponse,SearchResult::class.java)

        }

    }

}