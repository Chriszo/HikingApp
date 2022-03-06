package com.example.hikingapp.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.domain.route.Route
import java.io.Serializable

class ProfileViewModel : ViewModel(), Serializable {

    val savedRoutes = MutableLiveData<MutableList<Route>>()

    val completedRoutes = MutableLiveData<MutableList<Route>>()

    val savedSights = MutableLiveData<MutableList<Sight>>()

    val completedSights = MutableLiveData<MutableList<Sight>>()


    val selectedRouteItems =MutableLiveData<MutableList<Int>>()
    val selectedSightItems =MutableLiveData<MutableList<Int>>()

    val isRoutesLongClickPressed = MutableLiveData<Boolean>()

    val isSightsLongClickPressed = MutableLiveData<Boolean>()

    val savedRoutesEnabled = MutableLiveData<Boolean>()
    val savedSightsEnabled = MutableLiveData<Boolean>()

}