package com.example.hikingapp.ar.geospatial

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.domain.route.Route
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.codelabs.hellogeospatial.helpers.ARCoreSessionLifecycleHelper
import com.google.ar.core.codelabs.hellogeospatial.helpers.GeoPermissionsHelper
import com.google.ar.core.examples.java.common.helpers.FullScreenHelper
import com.google.ar.core.examples.java.common.samplerender.SampleRender
import com.google.ar.core.exceptions.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class GeoARActivity : AppCompatActivity() {
  companion object {
    private const val TAG = "HelloGeoActivity"
  }

  lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper
  lateinit var view: ARGeoView
  lateinit var renderer: ARGeoRenderer
  val geoAnchors = mutableListOf<Anchor>()

  lateinit var currentRoute: Route
  lateinit var authInfo: FirebaseUser

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    arCoreSessionHelper = ARCoreSessionLifecycleHelper(this)
    // If Session creation or Session.resume() fails, display a message and log detailed
    // information.
    arCoreSessionHelper.exceptionCallback =
      { exception ->
        val message =
          when (exception) {
            is UnavailableUserDeclinedInstallationException ->
              "Please install Google Play Services for AR"
            is UnavailableApkTooOldException -> "Please update ARCore"
            is UnavailableSdkTooOldException -> "Please update this app"
            is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
            is CameraNotAvailableException -> "Camera not available. Try restarting the app."
            else -> "Failed to create AR session: $exception"
          }
        Log.e(TAG, "ARCore threw an exception", exception)
        view.snackbarHelper.showError(this, message)
      }

    initializeRouteInformation()

    // Configure session features.
    arCoreSessionHelper.beforeSessionResume = ::configureSession
    lifecycle.addObserver(arCoreSessionHelper)


    renderer = ARGeoRenderer(this)
    lifecycle.addObserver(renderer)

    // Set up Hello AR UI.
    view = ARGeoView(this)
    lifecycle.addObserver(view)
    setContentView(view.root)

    // Sets up an example renderer using our HelloGeoRenderer.
    SampleRender(view.surfaceView, renderer, assets)

    FirebaseDatabase.getInstance().getReference("anchors").child("route_${currentRoute.routeId}").addListenerForSingleValueEvent(object:
      ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {

        // Set up the Hello AR renderer.

        val session = arCoreSessionHelper.session
        val earth = session?.earth

        if (snapshot.exists()) {
          val anchors = snapshot.value as List<HashMap<String, Double>>

          anchors.forEach { anchor ->
            val latitude = anchor["latitude"] as Double
            val longitude = anchor["longitude"] as Double
            val altitude = earth!!.cameraGeospatialPose.altitude - 1
            val qx = 0f
            val qy = 0f
            val qz = 0f
            val qw = 1f
            val anchor = earth!!.resolveAnchorOnTerrain(latitude, longitude, altitude, qx, qy, qz, qw)
            println(anchor.terrainAnchorState)

            geoAnchors.add(anchor)
          }
          renderer.setGeospatialAnchors(geoAnchors)

        }

      }

      override fun onCancelled(error: DatabaseError) {
      }

    })



    // Setup ARCore session lifecycle helper and configuration.

  }

  private fun initializeRouteInformation() {
    intent.extras?.apply {
      currentRoute = get("route") as Route
      authInfo = get("authInfo") as FirebaseUser
    }
  }

  // Configure the session, setting the desired options according to your usecase.
  fun configureSession(session: Session) {
    // TODO: Configure ARCore to use GeospatialMode.ENABLED.
    session.configure(session.config.apply {
      geospatialMode = Config.GeospatialMode.ENABLED
    })
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    results: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, results)
    if (!GeoPermissionsHelper.hasGeoPermissions(this)) {
      // Use toast instead of snackbar here since the activity will exit.
      Toast.makeText(this, "Camera and location permissions are needed to run this application", Toast.LENGTH_LONG)
        .show()
      if (!GeoPermissionsHelper.shouldShowRequestPermissionRationale(this)) {
        // Permission denied with checking "Do not ask again".
        GeoPermissionsHelper.launchPermissionSettings(this)
      }
      finish()
    }
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    val storage: FirebaseStorage = FirebaseStorage.getInstance()

    if (requestCode == 1888 && resultCode == RESULT_OK) {
      val imageBitmap = data!!.extras!!["data"] as Bitmap?
      val outputStream = ByteArrayOutputStream()
      imageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
      val byteArray = outputStream.toByteArray()
      storage.getReference("routes/" + currentRoute.routeId.toString() + "/photos")
        .listAll()
        .addOnSuccessListener { listResult ->
          val nextIndex = listResult.items.size + 1
          storage.getReference("routes/" + currentRoute.routeId.toString() + "/photos/photo_" + currentRoute.routeId.toString() + "_" + nextIndex + ".jpg")
            .putBytes(byteArray)
            .addOnSuccessListener { taskSnapshot ->
              println("Successfully uploaded image.")
              Toast.makeText(
                this,
                "Photo saved successfully.",
                Toast.LENGTH_LONG
              ).show()
            }
        }
//            dispatchTakePictureIntent()
//            imageView.setImageBitmap(imageBitmap)
    }
  }
}
