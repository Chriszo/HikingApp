package com.example.hikingapp.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hikingapp.domain.route.Route
import java.io.Serializable

class ProfileViewModel : ViewModel(), Serializable {

    val favoriteRoutes = MutableLiveData<List<Route>>()

}