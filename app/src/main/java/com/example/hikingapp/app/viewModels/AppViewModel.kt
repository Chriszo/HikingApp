package com.example.hikingapp.app.viewModels

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage

class AppViewModel : ViewModel() {

    val storage = MutableLiveData<FirebaseStorage>()
    val mainPhotos = MutableLiveData<MutableList<Bitmap>>()
}