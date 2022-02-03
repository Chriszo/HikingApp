package com.example.hikingapp.search.searchResults

data class Address(
    val road: String?,
    val state: String?,
    val building: String?,
    val county: String?,
    val house_number: String?,
    val city: String?,
    val city_district: String?,
    val state_district: String?,
    val municipality: String?,
    val suburb: String?,
    val country: String?,
    val neighbourhood: String?,
    val country_code: String?,
    val postcode: String?
) {
    constructor() : this(null,null,null,null,null,null,null,null,null,null,null,null,null,null)
}