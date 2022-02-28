package com.example.hikingapp.persistence.mock.db

import com.example.hikingapp.R
import com.example.hikingapp.domain.enums.DifficultyLevel
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.route.RouteInfo
import com.example.hikingapp.domain.enums.RouteType
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng

class MockDatabase {


    companion object {

        val routesMap: HashMap<String, Triple<String, String, LatLng?>> = hashMapOf(
            "Philopappou" to Triple(
                "Philopappou",
                "philopappou_trail.geojson",
                null
            ),
            "Pelion" to Triple(
                "Pelion",
                "seichsou_trail.geojson",
                null
            )
        )

        val mockSearchResults = setOf<Triple<Set<String>, Point, Route>>(
            Triple(
                setOf("philopappou", "filopappou", "filopapou", "philopapou"),
                Point.fromLngLat(23.71683574779853, 37.97362915837593),
                Route(
                    "Philopappou", "Attica", R.drawable.philopappou, RouteInfo(
                        4.5, 65.2, RouteType.LINEAR,
                        DifficultyLevel.EASY, 3.5f, null
                    ), null, null, null, listOf(R.drawable.philopappou)
                )
            ),
            Triple(
                setOf("Philolaou", "filolaou"),
                Point.fromLngLat(23.751349476821982, 37.965990579031484),
                Route(
                    "Philolaou",
                    "Attica",
                    R.drawable.thiseion,
                    RouteInfo(6.7, 55.9, RouteType.CYCLIC, DifficultyLevel.EASY, 2.5f, null),
                    null,
                    null,
                    null,
                    listOf(R.drawable.thiseion)
                )
            ),
            Triple(
                setOf("pilion", "pilio", "pelion", "pelio"),
                Point.fromLngLat(23.044974025226864, 39.4436732830854),
                Route(
                    "Pelion",
                    "Thessaly",
                    R.drawable.pelion_bridge,
                    RouteInfo(5.2, 78.2, RouteType.LINEAR, DifficultyLevel.MODERATE, 4.6f, null),
                    null,
                    null,
                    null,
                    listOf(R.drawable.pelion_bridge, R.drawable.pelion_greece)
                )
            ),
            Triple(
                setOf("olympus", "olympos", "όλυμπος", "ολυμπος"),
                Point.fromLngLat(22.34898859796338, 40.10140396689491),
                Route(
                    "Olympus",
                    "Central Macedonia",
                    R.drawable.olympus,
                    RouteInfo(5.5, 91.7, RouteType.LINEAR, DifficultyLevel.HARD, 4.8f, null),
                    null,
                    null,
                    null,
                    listOf(R.drawable.olympus, R.drawable.olymmpus_enipeas)
                )
            ),
            Triple(
                setOf("pindos", "pindus"),
                Point.fromLngLat(22.34898859796338, 40.10140396689491),
                Route(
                    "Pindus",
                    "Epirus",
                    R.drawable.thiseion,
                    RouteInfo(5.5, 91.7, RouteType.LINEAR, DifficultyLevel.HARD, 4.8f, null),
                    null,
                    null,
                    null,
                    listOf(R.drawable.thiseion)
                )
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