package com.example.hikingapp.persistence.utils

import com.example.hikingapp.domain.enums.DifficultyLevel
import com.example.hikingapp.domain.enums.RouteType
import com.example.hikingapp.persistence.entities.RouteEntity
import com.example.hikingapp.persistence.entities.SightEntity
import com.example.hikingapp.persistence.entities.UserEntity
//import com.example.hikingapp.persistence.mock.db.MockDatabase
import com.google.firebase.database.FirebaseDatabase

object DBUtils {

    private val database = FirebaseDatabase.getInstance()

//    fun initializeDatabaseData() {
//
//        MockDatabase.mockSearchResults.map { it.third }.withIndex().forEach {
//            val routeEntity = RouteEntity(
//                it.value.routeId,
//                it.value.routeName!!,
//                it.value.stateName!!,
//                it.value.mainPhoto!!,
//                it.value.routeInfo?.distance!!,
//                it.value.routeInfo?.timeEstimation!!,
//                it.value.routeInfo?.routeType!!,
//                it.value.routeInfo?.difficultyLevel!!,
//                it.value.routeInfo?.rating!!
//            )
//            database.getReference("routes").child("route${it.value.routeId}").setValue(routeEntity)
//        }
//
//        MockDatabase.mockSights.map { it.third }.withIndex().forEach {
//            val sightEntity = SightEntity(
//                it.value.sightId,
//                it.value.name!!,
//                it.value.description!!,
//                it.value.rating!!,
//                it.value.mainPhoto!!
//            )
//            database.getReference("sights").child("sight${it.value.sightId}").setValue(sightEntity)
//
//
//            MockDatabase.mockUsers.withIndex().forEach {
//                val userEntity = UserEntity(
//                    it.value.uId,
//                    it.value.userName,
//                    it.value.mail,
//                    it.value.password
//                )
//                database.getReference("users").child("user${it.value.uId}").setValue(userEntity)
//            }
//
//            val savedRoutesMap = HashMap<String, List<Long>>()
//            MockDatabase.mockUsersSavedInfo.withIndex().forEach {
//
//                val assoc = mutableListOf<Long>()
//                it.value.second.forEach { savedRoute ->
////                    val savedRouteAssociation = SavedUserRouteEntity(it.value.first,savedRoute)
//                    assoc.add(savedRoute)
//                }
//                savedRoutesMap[it.value.first] = assoc.toList()
//            }
//            database.getReference("savedRouteAssociations")
//                .updateChildren(savedRoutesMap as Map<String, Long>)
//
//            val savedSightsMap = HashMap<String, List<Long>>()
//            MockDatabase.mockUsersSavedInfo.withIndex().forEach {
//
//                val assoc = mutableListOf<Long>()
//                it.value.third.forEach { savedSight ->
////                    val savedRouteAssociation = SavedUserRouteEntity(it.value.first,savedRoute)
//                    assoc.add(savedSight)
//                }
//                savedSightsMap[it.value.first] = assoc.toList()
//            }
//            database.getReference("savedSightAssociations")
//                .updateChildren(savedSightsMap as Map<String, Long>)
//
//
//            val completedRoutesMap = HashMap<String, List<Long>>()
//            MockDatabase.mockUsersCompletedInfo.withIndex().forEach {
//
//                val assoc = mutableListOf<Long>()
//                it.value.second.forEach { savedRoute ->
////                    val savedRouteAssociation = SavedUserRouteEntity(it.value.first,savedRoute)
//                    assoc.add(savedRoute)
//                }
//                completedRoutesMap[it.value.first] = assoc.toList()
//            }
//            database.getReference("completedRouteAssociations")
//                .updateChildren(completedRoutesMap as Map<String, Long>)
//
//            val completedSightsMap = HashMap<String, List<Long>>()
//            MockDatabase.mockUsersCompletedInfo.withIndex().forEach {
//
//                val assoc = mutableListOf<Long>()
//                it.value.third.forEach { savedSight ->
////                    val savedRouteAssociation = SavedUserRouteEntity(it.value.first,savedRoute)
//                    assoc.add(savedSight)
//                }
//                completedSightsMap[it.value.first] = assoc.toList()
//            }
//            database.getReference("completedSightAssociations")
//                .updateChildren(completedSightsMap as Map<String, Long>)
//        }
//    }

    fun mapToRouteEntity(mappedData: HashMap<String, *>): RouteEntity {

        return RouteEntity(
            mappedData["routeId"]!! as Long,
            mappedData["routeName"] as String,
            mappedData["stateName"] as String,
            (mappedData["mainPhoto"] as Long).toInt(),
            mappedData["distance"] as Double,
            mappedData["timeEstimation"] as Double,
            RouteType.valueOf(mappedData["routeType"]!! as String),
            DifficultyLevel.valueOf(mappedData["difficultyLevel"]!! as String),
            (mappedData["rating"] as Double).toFloat()
        )

    }

    fun mapToSightEntity(mappedData: HashMap<String, *>): SightEntity {

        return SightEntity(
            mappedData["sightId"]!! as Long,
            mappedData["name"] as String,
            mappedData["description"] as String,
            (mappedData["rating"] as Double).toFloat(),
            (mappedData["mainPhoto"] as Long).toInt()
        )

    }

}