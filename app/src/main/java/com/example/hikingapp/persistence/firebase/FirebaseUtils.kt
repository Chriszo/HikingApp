package com.example.hikingapp.persistence.firebase

import com.example.hikingapp.domain.navigation.UserNavigationData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FirebaseUtils {

    private const val COMPLETED_USER_ROUTES = "completedRouteAssociations"
    private const val NOT_COMPLETED_USER_ROUTES = "notCompletedRouteAssociations"
    private const val COMPLETED_USER_NAVIGATION_DATA = "completedUserNavigationData"

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    private fun persistUserCompletedRoute(userId: String, routeId: Long) {

        database.getReference(COMPLETED_USER_ROUTES).child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val completedRouteIds = snapshot.value as MutableList<Long>
                        if (!completedRouteIds.contains(routeId)) {
                            completedRouteIds.add(routeId)
                            database.getReference(COMPLETED_USER_ROUTES).child(userId)
                                .setValue(completedRouteIds)
                        }
                    } else {
                        database.getReference(COMPLETED_USER_ROUTES).child(userId)
                            .setValue(mutableListOf(routeId))
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    fun persistUserInCompletedRoute(userId: String, routeId: Long) {

        database.getReference(NOT_COMPLETED_USER_ROUTES).child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val inCompletedRouteIds = snapshot.value as MutableList<Long>
                        if (!inCompletedRouteIds.contains(routeId)) {
                            inCompletedRouteIds.add(routeId)
                            database.getReference(NOT_COMPLETED_USER_ROUTES).child(userId)
                                .setValue(inCompletedRouteIds)
                        }
                    } else {
                        database.getReference(NOT_COMPLETED_USER_ROUTES).child(userId)
                            .setValue(mutableListOf(routeId))
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun persistUserNavigationData(uid: String, userNavigationData: UserNavigationData) {
        database.getReference(COMPLETED_USER_NAVIGATION_DATA).child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val completedNavigations = snapshot.value as MutableList<UserNavigationData>
                        var routeAlreadyExists = false
                        for (navigation in completedNavigations) {
                            if (navigation.routeId == userNavigationData.routeId) {
                                routeAlreadyExists = true
                                break
                            }
                        }
                        if (!routeAlreadyExists) {
                            completedNavigations.add(userNavigationData)
                            database.getReference(COMPLETED_USER_NAVIGATION_DATA).child(uid)
                                .setValue(completedNavigations)
                        }
                    } else {
                        database.getReference(COMPLETED_USER_NAVIGATION_DATA).child(uid)
                            .setValue(mutableListOf(userNavigationData))
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    fun persistNavigation(uid: String, userNavigationData: UserNavigationData) {
        persistUserCompletedRoute(uid, userNavigationData.routeId)
        persistUserNavigationData(uid, userNavigationData)
    }

    fun persistIncompleteNavigation(uid: String, userNavigationData: UserNavigationData) {

    }

}