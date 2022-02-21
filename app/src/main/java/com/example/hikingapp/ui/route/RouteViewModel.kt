package com.example.hikingapp.ui.route

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hikingapp.domain.Route

class RouteViewModel : ViewModel() {

    val route = MutableLiveData<Route>()

    val elevationData = MutableLiveData<List<Int>>()

    fun defineRoute(r: Route) {
        route.value = r
    }

    fun setElevationData(elevationDataList: MutableList<Int>) {
        route.value?.routeInfo?.elevationData = elevationDataList
        elevationData.value = elevationDataList
    }

}