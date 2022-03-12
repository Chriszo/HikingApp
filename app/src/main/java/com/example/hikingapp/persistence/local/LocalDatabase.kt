package com.example.hikingapp.persistence.local

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.hikingapp.persistence.entities.ImageEntity
import java.util.stream.Collectors

class LocalDatabase {

    companion object {
        private val photosStorage = mutableMapOf<String, List<ImageEntity>>()

        @RequiresApi(Build.VERSION_CODES.N)
        fun saveImage(
            id: Long,
            className: String,
            imageName: String,
            bitmap: Bitmap,
            isMainImage: Boolean
        ) {

            when (className) {
                "Route" -> photosStorage.computeIfAbsent(
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
                "Sight" -> photosStorage.computeIfAbsent(
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

            return if (photosStorage.containsKey(mapKey)) {
                photosStorage[mapKey]?.stream()
                    ?.filter { imageEntity -> imageEntity.imageName == imageName }
                    ?.map { it.imageBitmap }?.findFirst()?.orElse(null)
            } else null
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getMainImage(id: Long, className: String): Bitmap? {
            val mapKey = if (className == "Route") "R_$id" else "S_$id"

            return if (photosStorage.containsKey(mapKey)) {
                photosStorage[mapKey]?.stream()
                    ?.filter { imageEntity -> imageEntity.isMainImage }
                    ?.map { it.imageBitmap }?.findFirst()?.orElse(null)
            } else null
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getAllImages(id: Long, className: String): MutableList<Bitmap>? {
            val mapKey = if (className == "Route") "R_$id" else "S_$id"
            return photosStorage[mapKey]?.stream()?.map { it.imageBitmap }
                ?.collect(Collectors.toList())?.toMutableList()
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getImages(id: Long, className: String): MutableList<Bitmap>? {
            val mapKey = if (className == "Route") "R_$id" else "S_$id"
            return photosStorage[mapKey]?.stream()?.filter { !it.isMainImage }?.map { it.imageBitmap }
                ?.collect(Collectors.toList())?.toMutableList()
        }

        fun containsImages(mapKey: String): Boolean {
            return photosStorage.containsKey(mapKey) && !photosStorage[mapKey].isNullOrEmpty()
        }

    }


}