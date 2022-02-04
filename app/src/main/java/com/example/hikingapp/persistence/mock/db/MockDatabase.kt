package com.example.hikingapp.persistence.mock.db

import com.example.hikingapp.domain.DifficultyLevel
import com.example.hikingapp.domain.Route
import com.example.hikingapp.domain.RouteType
import com.example.hikingapp.persistence.RouteInfo
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng

class MockDatabase {


    companion object {
//        val coordsMap: HashMap<String, Triple<String, LatLng, LatLng>> = hashMapOf(
//            "Neda" to Triple(
//                "Neda",
//                LatLng(37.39907764711385, 21.824395945244753),
//                LatLng(37.391958992182914, 21.817494809788926)
//            ),
//            "Olympus" to Triple(
//                "Olympus",
//                LatLng(40.08521847331976, 22.406822140612377),
//                LatLng(40.08365884069379, 22.404955323296704)
//            ),
//            "Pelion" to Triple(
//                "Pelion",
//                LatLng(39.39324641215495, 23.001515139208983),
//                LatLng(39.37949756110225, 22.981887750459936)
//            )
//        )

        val routesMap: HashMap<String, Triple<String, String, LatLng?>> = hashMapOf(
            "Philopapou" to Triple(
                "Philopapou",
                "philopappou_trail.geojson",
                null
            )
        )
//            ),
//            "Olympus" to Triple(
//                "Olympus",
//                LatLng(40.08521847331976, 22.406822140612377),
//                LatLng(40.08365884069379, 22.404955323296704)
//            ),
//            "Pelion" to Triple(
//                "Pelion",
//                LatLng(39.39324641215495, 23.001515139208983),
//                LatLng(39.37949756110225, 22.981887750459936)
//            )

/*
        val mockSearchResults = setOf<Pair<String, Route>>(
            Pair("philopappou", Route("Philopappou", null, null,null)),
            Pair("pelion", Route("Pelion", null, null,null)),
            Pair("olympus", Route("Olympus", null, null,null))
        )*/

        val mockSearchResults = setOf<Triple<Set<String>, Point, Route>>(
            Triple(
                setOf("philopappou", "filopappou", "filopapou", "philopapou"),
                Point.fromLngLat(23.71683574779853, 37.97362915837593),
                Route("Philopappou", RouteInfo(4.5,65.2,RouteType.LINEAR,DifficultyLevel.EASY,3.5f), null, null)
            ),
            Triple(
                setOf("Philolaou", "filolaou"),
                Point.fromLngLat(23.751349476821982, 37.965990579031484),
                Route("Philolaou", RouteInfo(6.7,55.9,RouteType.CYCLIC,DifficultyLevel.EASY,2.5f), null, null)
            ),
            Triple(
                setOf("pilion", "pilio", "pelion", "pelio"),
                Point.fromLngLat(23.044974025226864, 39.4436732830854),
                Route("Pelion", RouteInfo(5.2,78.2,RouteType.LINEAR,DifficultyLevel.MODERATE,4.6f), null, null)
            ),
            Triple(
                setOf("olympus", "olympos", "όλυμπος", "ολυμπος"),
                Point.fromLngLat(22.34898859796338, 40.10140396689491),
                Route("Olympus", RouteInfo(5.5,91.7,RouteType.LINEAR,DifficultyLevel.HARD,4.8f), null, null)
            ),
            Triple(
                setOf("pindos", "pindus"),
                Point.fromLngLat(22.34898859796338, 40.10140396689491),
                Route("Pindus", RouteInfo(5.5,91.7,RouteType.LINEAR,DifficultyLevel.HARD,4.8f), null, null)
            )
        )


        val mockKeywords = setOf(
            "olympus",
            "olympos",
            "όλυμπος",
            "ολυμπος",
            "πήλιο",
            "πηλιο",
            "φιλοπάππου",
            "pilion",
            "pilio",
            "pelion",
            "pelio",
            "philopappou",
            "filopappou",
            "filopapou",
            "philopapou",
            "Philolaou",
            "filolaou"
        )
    }

}