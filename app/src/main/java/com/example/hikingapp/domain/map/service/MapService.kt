package com.example.hikingapp.domain.map.service

import com.example.hikingapp.domain.map.MapInfo

interface MapService {

    fun getMapInformation(routeName: String): MapInfo

}