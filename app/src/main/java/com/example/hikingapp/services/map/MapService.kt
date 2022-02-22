package com.example.hikingapp.services.map

import com.example.hikingapp.domain.map.MapInfo

interface MapService {

    fun getMapInformation(jsonContent: String): MapInfo

}