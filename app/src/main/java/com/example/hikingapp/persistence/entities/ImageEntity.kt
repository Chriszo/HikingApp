package com.example.hikingapp.persistence.entities

import android.graphics.Bitmap

data class ImageEntity(val imageName: String,val imageBitmap: Bitmap, val isMainImage: Boolean)
