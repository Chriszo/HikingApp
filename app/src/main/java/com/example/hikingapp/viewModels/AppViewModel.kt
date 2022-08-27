package com.example.hikingapp.viewModels

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage
import com.mapbox.geojson.Point

class AppViewModel : ViewModel() {

    val storage = MutableLiveData<FirebaseStorage>()
    val mainPhotos = MutableLiveData<MutableList<Bitmap>>()
    val origins = MutableLiveData<MutableList<Point>>()
    val currentLocation = MutableLiveData<Point>()
}