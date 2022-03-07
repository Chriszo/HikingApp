package com.example.hikingapp.persistence.mock.db

import com.example.hikingapp.R
import com.example.hikingapp.domain.culture.CultureInfo
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.domain.enums.DifficultyLevel
import com.example.hikingapp.domain.enums.RouteType
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.route.RouteInfo
import com.example.hikingapp.domain.users.User
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
                    1, "Philopappou", "Attica", R.drawable.philopappou, RouteInfo(
                        4.5, 65.2, RouteType.LINEAR,
                        DifficultyLevel.EASY, 3.5f, null
                    ), null, null, CultureInfo(
                        mutableListOf(
                            Sight(
                                1,
                                Point.fromLngLat(23.71683574779853, 37.97362915837593),
                                "Thiseion",
                                "Attica",
                                3.8f,
                                R.drawable.thiseion,
                                mutableListOf()
                            ),
                            Sight(
                                2,
                                Point.fromLngLat(23.751349476821982, 37.965990579031484),
                                "Lentzos",
                                "Attica",
                                4.3f,
                                R.drawable.philopappou,
                                mutableListOf()
                            )
                        )
                    ), listOf(R.drawable.philopappou)
                )
            ),
            Triple(
                setOf("Philolaou", "filolaou"),
                Point.fromLngLat(23.751349476821982, 37.965990579031484),
                Route(
                    2,
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
                    3,
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
                    4,
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
                Point.fromLngLat(20.754802018618687, 39.992533423861374),
                Route(
                    5,
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

        val mockSights = setOf<Triple<Set<String>, Point, Sight>>(
            Triple(
                setOf("philopappou", "filopappou", "filopapou", "philopapou"),
                Point.fromLngLat(23.71683574779853, 37.97362915837593),
                Sight(
                    1,
                    Point.fromLngLat(23.71683574779853, 37.97362915837593),
                    "Thiseion",
                    "Attica",
                    3.8f,
                    R.drawable.thiseion,
                    mutableListOf()
                )
            ),
            Triple(
                setOf("Philolaou", "filolaou"),
                Point.fromLngLat(23.751349476821982, 37.965990579031484),
                Sight(
                    2,
                    Point.fromLngLat(23.751349476821982, 37.965990579031484),
                    "Lentzos",
                    "Attica",
                    4.3f,
                    R.drawable.philopappou,
                    mutableListOf()
                )
            ),
            Triple(
                setOf("pilion", "pilio", "pelion", "pelio"),
                Point.fromLngLat(23.044974025226864, 39.4436732830854),
                Sight(
                    3,
                    Point.fromLngLat(23.044974025226864, 39.4436732830854),
                    "Makrinitsa",
                    "Thessaly",
                    4.9f,
                    R.drawable.pelion_greece,
                    mutableListOf()
                )
            ),
            Triple(
                setOf("olympus", "olympos", "όλυμπος", "ολυμπος"),
                Point.fromLngLat(22.34898859796338, 40.10140396689491),
                Sight(
                    4,
                    Point.fromLngLat(22.34898859796338, 40.10140396689491),
                    "Stefani",
                    "Central Macedonia",
                    4.8f,
                    R.drawable.olympus,
                    mutableListOf()
                )
            )
        )

        val mockUsers = setOf<User>(
            User(1,"M47JKPnilaU39gnHxAvvBj62DmW2","user-1", "user1@mail.com", "password1", null),
            User(2,"bO32JG2nRLY65EHswirMDJIBg6l2","user-2", "user2@mail.com", "password2", null),
            User(3,"","user-3", "user3@mail.com", "password3", null)
        )


        val mockUsersSavedInfo = setOf<Triple<Long, MutableList<Long>, MutableList<Long>>>(
            Triple(1, mutableListOf(1, 3), mutableListOf(2, 3)),
            Triple(2, mutableListOf(1, 2), mutableListOf(1)),
            Triple(3, mutableListOf(3, 4), mutableListOf(3, 4))
        )

        val mockUsersCompletedInfo = setOf<Triple<Long, MutableList<Long>, MutableList<Long>>>(
            Triple(1, mutableListOf(2), mutableListOf(1, 2)),
            Triple(2, mutableListOf(1, 3), mutableListOf(2,3)),
            Triple(3, mutableListOf(2,3), mutableListOf(4))
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