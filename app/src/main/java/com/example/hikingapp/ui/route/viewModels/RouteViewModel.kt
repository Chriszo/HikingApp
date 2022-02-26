package com.example.hikingapp.ui.route.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hikingapp.domain.culture.CultureInfo
import com.example.hikingapp.domain.route.Route

class RouteViewModel : ViewModel() {

    val route = MutableLiveData<Route>()

    val elevationData = MutableLiveData<List<Int>>()

    val cultureInfo = MutableLiveData<CultureInfo>()

    fun defineRoute(r: Route) {
        route.value = r
    }

    fun setElevationData(elevationDataList: MutableList<Int>) {
        route.value?.routeInfo?.elevationData = elevationDataList
        elevationData.value = elevationDataList
    }

    fun setCultureInfo(updatedCultureInfo: CultureInfo) {
        route.value?.cultureInfo = updatedCultureInfo
        cultureInfo.value = updatedCultureInfo
    }

}