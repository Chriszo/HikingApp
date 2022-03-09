package com.example.hikingapp.ui.profile.saved

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hikingapp.domain.route.Route

class CompletedViewModel : ViewModel() {

    val route = MutableLiveData<Route>()
    val photos = MutableLiveData<List<Int>>()
    val elevationData = MutableLiveData<List<Long>>()
}