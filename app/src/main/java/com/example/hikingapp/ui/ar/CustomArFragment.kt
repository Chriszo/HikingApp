package com.example.hikingapp.ui.ar

import android.Manifest
import com.google.ar.sceneform.ux.ArFragment

class CustomArFragment: ArFragment() {

    override fun getAdditionalPermissions(): Array<String> =
        // TODO return location permission
        listOf(Manifest.permission.ACCESS_FINE_LOCATION)
            .toTypedArray()
}