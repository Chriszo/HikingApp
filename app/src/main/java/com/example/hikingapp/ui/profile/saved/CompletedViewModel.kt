package com.example.hikingapp.ui.profile.saved

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.users.PhotoItem

class CompletedViewModel : ViewModel() {

    val route = MutableLiveData<Route>()
    val photos = MutableLiveData<List<PhotoItem?>>()
    val elevationData = MutableLiveData<List<Long>>()
}