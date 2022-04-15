package com.example.hikingapp.persistence.firebase

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.hikingapp.domain.navigation.MapPointWrapper
import com.example.hikingapp.domain.navigation.SerializableMapPoint
import com.example.hikingapp.domain.navigation.UserNavigationData
import com.example.hikingapp.utils.ElevationDataUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mapbox.geojson.FeatureCollection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
                        val completedNavigations =
                            extractNavigationData(snapshot.value as MutableList<HashMap<String, *>>)
                        var routeAlreadyExists = false
                        for (navigation in completedNavigations) {
                            if (navigation.routeId == userNavigationData.routeId) {
                                routeAlreadyExists = true
                                navigation.appendNavigationData(userNavigationData)
                                break
                            }
                        }
                        if (!routeAlreadyExists) {
                            completedNavigations.add(userNavigationData)
                        }
                        database.getReference(COMPLETED_USER_NAVIGATION_DATA).child(uid)
                            .setValue(completedNavigations)

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

    private fun extractNavigationData(mutableList: MutableList<HashMap<String, *>>): MutableList<UserNavigationData> {
        val navigationDataList = mutableListOf<UserNavigationData>()
        for (navigation in mutableList) {
            val navigationData = UserNavigationData(
                navigation["routeId"] as Long,
                navigation["distanceCovered"] as Double,
                navigation["timeSpent"] as Long,
                navigation["currentElevation"] as MutableList<Long>
            )
            navigationDataList.add(navigationData)
        }
        return navigationDataList
    }

    fun persistNavigation(uid: String, userNavigationData: UserNavigationData) {
        persistUserCompletedRoute(uid, userNavigationData.routeId)
        persistUserNavigationData(uid, userNavigationData)
    }

    fun persistIncompleteNavigation(uid: String, userNavigationData: UserNavigationData) {

    }

    fun setElevationData(routeId: Long, start: Int, end: Int) {
        database.getReference("navigationMapData")
            .child(routeId.toString())
            .child("serializedMapPoints")
            .addValueEventListener(object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val data = snapshot.value as MutableList<HashMap<String,*>>
                        var elementsParsed = 0
                        val thresshold = 48
                        val points:MutableList<SerializableMapPoint> = mutableListOf()
                        data.forEach {

                            val point = SerializableMapPoint(
                                it["pointId"] as String,
                                it["index"] as Long,
                                it["longitude"] as Double,
                                it["latitude"] as Double,
                                null
                            )

                            points.add(point)
                        }
                        points.forEach { point ->
                            if (point.index in start..end) {

                                val query = ElevationDataUtils.formElevationRequestQuery(point)

                                query.enqueueCall(object : Callback<FeatureCollection> {
                                    @RequiresApi(Build.VERSION_CODES.N)
                                    override fun onResponse(
                                        call: Call<FeatureCollection>,
                                        response: Response<FeatureCollection>
                                    ) {
                                        if (response.isSuccessful) {

                                            response.body()?.features()
                                                ?.stream()
                                                ?.mapToLong { feature ->
                                                    feature.properties()?.get("ele")?.asLong!!
                                                }
                                                ?.max()
                                                ?.ifPresent { max ->
                                                    point.elevation = max
                                                    elementsParsed++
                                                }
                                        }
                                        if (elementsParsed == thresshold) {
                                            FirebaseDatabase.getInstance()
                                                .getReference("navDataWithElevation")
                                                .child(routeId.toString())
                                                .setValue(MapPointWrapper(points))
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<FeatureCollection>,
                                        t: Throwable
                                    ) {
                                        TODO("Not yet implemented")
                                    }

                                })
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

}