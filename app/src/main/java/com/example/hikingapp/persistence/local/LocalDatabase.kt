package com.example.hikingapp.persistence.local

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.persistence.entities.ImageEntity
import java.util.stream.Collectors

class LocalDatabase {

    companion object {

        private val photosLocalStorage = mutableMapOf<String, List<ImageEntity>>()
        private val sightsLocalStorage = mutableMapOf<Long, Sight>()
        private val associationsLocalStorage = mutableMapOf<Long, MutableList<Sight>>()

        @RequiresApi(Build.VERSION_CODES.N)
        fun saveImage(
            id: Long,
            className: String,
            imageName: String,
            bitmap: Bitmap,
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
                                bitmap,
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
                                bitmap,
                                isMainImage
                            )
                        )
                    }
                }
            }
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
        fun getImages(id: Long, className: String): MutableList<Bitmap?>? {
            val mapKey = if (className == "Route") "R_$id" else "S_$id"
            return photosLocalStorage[mapKey]?.stream()?.filter { !it.isMainImage }
                ?.map { it.imageBitmap }
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
            return associationsLocalStorage[routeId]?.stream()?.filter { it.sightId == sightId }?.findFirst()?.orElse(null)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getSightsOfRoute(routeId: Long): MutableList<Sight>? {
            return associationsLocalStorage[routeId]?.toMutableList()
        }
    }


}