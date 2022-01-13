package com.example.hikingapp.persistence.mock.db

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
                null)
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
    }

}