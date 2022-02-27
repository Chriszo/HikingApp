package com.example.hikingapp.ui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hikingapp.domain.culture.CultureInfo
import com.example.hikingapp.domain.route.Route
import java.io.Serializable

class RouteViewModel : ViewModel(), Serializable {

    val route = MutableLiveData<Route>()

    val elevationData = MutableLiveData<List<Int>>()

    val cultureInfo = MutableLiveData<CultureInfo>()

    val photos = MutableLiveData<List<Int>>()

   /* fun defineRoute(r: Route) {
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

    fun setPhotos(updatedPhotosList: MutableList<Int>) {
        route.value?.photos = updatedPhotosList
        photos.value = updatedPhotosList
    }*/

}