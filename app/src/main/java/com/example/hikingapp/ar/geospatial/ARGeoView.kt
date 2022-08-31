package com.example.hikingapp.ar.geospatial

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.hikingapp.MainActivity
import com.example.hikingapp.NavigationActivity
import com.example.hikingapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Earth
import com.google.ar.core.GeospatialPose
import com.google.ar.core.examples.java.common.helpers.SnackbarHelper

/** Contains UI elements for Hello Geo. */
class ARGeoView(val activity: GeoARActivity) : DefaultLifecycleObserver {
  val root = View.inflate(activity, R.layout.activity_ar, null)
  val surfaceView = root.findViewById<GLSurfaceView>(R.id.surfaceview)

  val session
    get() = activity.arCoreSessionHelper.session

  val snackbarHelper = SnackbarHelper()

 /* var mapView: MapView? = null
  val mapTouchWrapper = root.findViewById<MapTouchWrapper>(R.id.map_wrapper).apply {
    setup { screenLocation ->
      val latLng: LatLng =
        mapView?.googleMap?.projection?.fromScreenLocation(screenLocation) ?: return@setup
      activity.renderer.onMapClick(latLng)
    }
  }
  val mapFragment =
    (activity.supportFragmentManager.findFragmentById(R.id.map)!! as SupportMapFragment).also {
      it.getMapAsync { googleMap -> mapView = MapView(activity, googleMap) }
    }*/

    var modelsVisible = true
    val arButton = root.findViewById<FloatingActionButton>(R.id.ar_button).apply {
        setOnClickListener {
            modelsVisible = !modelsVisible
            if (modelsVisible) {
                this.setImageResource(R.drawable.ar_2_icon_remove)
            } else {
                this.setImageResource(R.drawable.ar_2_icon)
            }

        }
    }

    val cameraButton = root.findViewById<FloatingActionButton>(R.id.cameraButton).apply {
        this.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_DENIED
            ) requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.CAMERA
                ), 1888
            )

            val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(activity, photoIntent,  1888, null)
        }
    }

    val backButton = root.findViewById<ImageView>(R.id.back_btn).apply {
        this.setOnClickListener {
            val navigationIntent = Intent(activity, NavigationActivity::class.java)
            navigationIntent.putExtra("route", activity.currentRoute)
            navigationIntent.putExtra("authInfo", activity.authInfo)
            activity.startActivity(navigationIntent)
        }
    }

    val homeButton = root.findViewById<ImageView>(R.id.home_btn).apply {
        setOnClickListener {
            val homeIntent = Intent(activity, MainActivity::class.java)
            homeIntent.putExtra("route", activity.currentRoute)
            homeIntent.putExtra("authInfo", activity.authInfo)
            activity.startActivity(homeIntent)
        }
    }



//  val statusText = root.findViewById<TextView>(R.id.statusText)

  fun updateStatusText(earth: Earth, cameraGeospatialPose: GeospatialPose?) {
    activity.runOnUiThread {
      val poseText = if (cameraGeospatialPose == null) "" else
        activity.getString(R.string.geospatial_pose,
                           cameraGeospatialPose.latitude,
                           cameraGeospatialPose.longitude,
                           cameraGeospatialPose.horizontalAccuracy,
                           cameraGeospatialPose.altitude,
                           cameraGeospatialPose.verticalAccuracy,
                           cameraGeospatialPose.heading,
                           cameraGeospatialPose.headingAccuracy)
//      statusText.text = activity.resources.getString(R.string.earth_state,
//                                                     earth.earthState.toString(),
//                                                     earth.trackingState.toString(),
//                                                     poseText)
    }
  }

  override fun onResume(owner: LifecycleOwner) {
    surfaceView.onResume()
  }

  override fun onPause(owner: LifecycleOwner) {
    surfaceView.onPause()
  }
}
