package com.example.hikingapp.persistence.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.domain.navigation.UserNavigationData
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.users.PhotoItem
import com.example.hikingapp.domain.users.reviews.Review
import com.example.hikingapp.persistence.entities.ImageEntity
import com.example.hikingapp.persistence.entities.RouteMapEntity
import java.util.stream.Collectors

class LocalDatabase {

    companion object {

        private val photosLocalStorage = mutableMapOf<String, List<ImageEntity>>()
        private val sightsLocalStorage = mutableMapOf<Long, Sight>()
        private val associationsLocalStorage = mutableMapOf<Long, MutableList<Sight>>()
        private val userNavigationStorage = mutableMapOf<String, MutableList<UserNavigationData>>()
        private val routeMapStorage = mutableMapOf<Long, RouteMapEntity?>()
        private val reviewsStorage = mutableMapOf<Long, MutableList<Review>>()
        private val photosData = mutableMapOf<String,Bitmap>()
        private var routesLocalStorage = mutableListOf<Route>()

        @RequiresApi(Build.VERSION_CODES.N)
        fun saveImage(
            id: Long,
            className: String,
            imageName: String,
            photoItem: PhotoItem,
            isMainImage: Boolean
        ) {

            when (className) {
                "Route" -> photosLocalStorage.computeIfAbsent(
                    "R_$id"
                ) {
                    mutableListOf<ImageEntity>().apply {
                        this.add(
                            (ImageEntity(
                                imageName,
                                photoItem.imageBitmap!!,
                                isMainImage
                            ))
                        )
                    }
                }
                "Sight" -> photosLocalStorage.computeIfAbsent(
                    "S_$id"
                ) {
                    mutableListOf<ImageEntity>().apply {
                        this.add(
                            ImageEntity(
                                imageName,
                                photoItem.imageBitmap!!,
                                isMainImage
                            )
                        )
                    }
                }
            }
            photosData[imageName.split(".")[0]] = photoItem.imageBitmap!!
        }

        fun getBitmapForImageName(imageName: String): Bitmap? {
            return photosData[imageName]
        }

        fun getAllImages(): MutableMap<String,Bitmap> {
            return photosData
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getImage(id: Long, className: String, imageName: String?): Bitmap? {
            val mapKey = if (className == "Route") "R_$id" else "S_$id"

            return if (photosLocalStorage.containsKey(mapKey)) {
                photosLocalStorage[mapKey]?.stream()
                    ?.filter { imageEntity -> imageEntity.imageName == imageName }
                    ?.map { it.imageBitmap }?.findFirst()?.orElse(null)
            } else null
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getMainImage(id: Long, className: String): Bitmap? {
            val mapKey = if (className == "Route") "R_$id" else "S_$id"

            return if (photosLocalStorage.containsKey(mapKey)) {
                photosLocalStorage[mapKey]?.stream()
                    ?.filter { imageEntity -> imageEntity.isMainImage }
                    ?.map { it.imageBitmap }?.findFirst()?.orElse(null)
            } else null
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getAllImages(id: Long, className: String): MutableList<Bitmap>? {
            val mapKey = if (className == "Route") "R_$id" else "S_$id"
            return photosLocalStorage[mapKey]?.stream()?.map { it.imageBitmap }
                ?.collect(Collectors.toList())?.toMutableList()
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getImages(id: Long, className: String): MutableList<PhotoItem?>? {
            val mapKey = if (className == "Route") "R_$id" else "S_$id"
            return photosLocalStorage[mapKey]?.stream()?.filter { !it.isMainImage }
                ?.map { PhotoItem(it.imageName,it.imageBitmap) }
                ?.collect(Collectors.toList())?.toMutableList()
        }

        fun containsImages(mapKey: String): Boolean {
            return photosLocalStorage.containsKey(mapKey) && !photosLocalStorage[mapKey].isNullOrEmpty()
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun saveSight(routeId: Long?, sight: Sight) {
            sightsLocalStorage[sight.sightId] = sight
            routeId?.run {
                associationsLocalStorage.computeIfAbsent(this) { mutableListOf<Sight>() }.add(sight)
            }
        }


        fun getSight(sightId: Long): Sight? {
            return sightsLocalStorage[sightId]
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getSightOfRoute(routeId: Long, sightId: Long): Sight? {
            return associationsLocalStorage[routeId]?.stream()?.filter { it.sightId == sightId }
                ?.findFirst()?.orElse(null)
        }

        fun getSightsOfRoute(routeId: Long): MutableList<Sight>? {
            return if (associationsLocalStorage.containsKey(routeId) && associationsLocalStorage.get(routeId) != null) {
                associationsLocalStorage.get(routeId) as MutableList<Sight>
            } else{
                null
            }
        }

        fun saveNavigationDataLocally(uid: String, userNavigationData: UserNavigationData) {
            if (userNavigationStorage.containsKey(uid)) {
                if (userNavigationStorage[uid].isNullOrEmpty()) {
                    userNavigationStorage[uid] = mutableListOf(userNavigationData)
                } else {
                    userNavigationStorage[uid]!!.add(userNavigationData)
                }
            } else {
                userNavigationStorage[uid] = mutableListOf(userNavigationData)
            }

        }

        fun saveRouteMapContent(routeId: Long, routeMapEntity: RouteMapEntity) {
            routeMapStorage[routeId] = routeMapEntity
        }

        fun getRouteMapContent(routeId: Long): RouteMapEntity? {
            return routeMapStorage[routeId]
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun setReviewsForRoute(routeId: Long, reviews: MutableList<Review>) {
            reviewsStorage.computeIfAbsent(routeId,{ mutableListOf<Review>()}).addAll(reviews)
        }

        fun getReviewsForRoute(routeId: Long): MutableList<Review> {
            return reviewsStorage[routeId] ?: mutableListOf()
        }

        fun saveRoutes(routes: MutableList<Route>) {
            routesLocalStorage = routes
        }

        fun getRoutes(): MutableList<Route> {
            return routesLocalStorage
        }

    }


}