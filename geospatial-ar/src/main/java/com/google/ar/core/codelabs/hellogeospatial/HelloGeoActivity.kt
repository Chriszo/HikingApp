/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.core.codelabs.hellogeospatial

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.codelabs.hellogeospatial.helpers.ARCoreSessionLifecycleHelper
import com.google.ar.core.codelabs.hellogeospatial.helpers.GeoPermissionsHelper
import com.google.ar.core.codelabs.hellogeospatial.helpers.HelloGeoView
import com.google.ar.core.examples.java.common.helpers.FullScreenHelper
import com.google.ar.core.examples.java.common.samplerender.SampleRender
import com.google.ar.core.exceptions.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
//import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class HelloGeoActivity : AppCompatActivity() {
  companion object {
    private const val TAG = "HelloGeoActivity"
  }

  lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper
  lateinit var view: HelloGeoView
  lateinit var renderer: HelloGeoRenderer
  val geoAnchors = mutableListOf<Anchor>()

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

    // Configure session features.
    arCoreSessionHelper.beforeSessionResume = ::configureSession
    lifecycle.addObserver(arCoreSessionHelper)


    renderer = HelloGeoRenderer(this)
    lifecycle.addObserver(renderer)

    // Set up Hello AR UI.
    view = HelloGeoView(this)
    lifecycle.addObserver(view)
    setContentView(view.root)

    // Sets up an example renderer using our HelloGeoRenderer.
    SampleRender(view.surfaceView, renderer, assets)

    FirebaseDatabase.getInstance().getReference("anchors").addListenerForSingleValueEvent(object:
      ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {

        // Set up the Hello AR renderer.

        val session = arCoreSessionHelper.session
        val earth = session?.earth

        if (snapshot.exists()) {
          val anchors = snapshot.value as List<HashMap<String, Double>>

          anchors.forEach { anchor ->
            val latitude = anchor["latitutde"] as Double
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
//    val storage: FirebaseStorage = FirebaseStorage.getInstance()
//
//    if (requestCode == 1888 && resultCode == RESULT_OK) {
//      val imageBitmap = data!!.extras!!["data"] as Bitmap?
//      val outputStream = ByteArrayOutputStream()
//      imageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
//      val byteArray = outputStream.toByteArray()
//      val routeId = 1
//      storage.getReference("routes/" + routeId.toString() + "/photos")
//        .listAll()
//        .addOnSuccessListener { listResult ->
//          val nextIndex = listResult.items.size + 1
//          storage.getReference("routes/" + routeId.toString() + "/photos/photo_" + routeId.toString() + "_" + nextIndex + ".jpg")
//            .putBytes(byteArray)
//            .addOnSuccessListener { taskSnapshot ->
//              println("Successfully uploaded image.")
//              Toast.makeText(
//                this,
//                "Photo saved successfully.",
//                Toast.LENGTH_LONG
//              ).show()
//            }
//        }
////            dispatchTakePictureIntent()
////            imageView.setImageBitmap(imageBitmap)
//    }
  }
}
