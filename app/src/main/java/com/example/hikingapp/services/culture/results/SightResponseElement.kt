package com.example.hikingapp.services.culture.results

data class SightResponseElement(
    val id: String?,
    val name: String?,
    val address: String?,
    val phone_number: String?,
    val website: String?,
    val location: LocationElement?,
    val types: List<String>?,
    val distance: Int
) {

    constructor(): this(null,null,null,null,null,null,null,-1)

}