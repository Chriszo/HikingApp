/*
 * Copyright 2019 Google Inc. All Rights Reserved.
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
package com.example.hikingapp.anchors

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hikingapp.R
import com.example.hikingapp.domain.route.Route
import com.google.ar.core.codelab.cloudanchor.helpers.FullScreenHelper
import com.google.firebase.auth.FirebaseUser

/**
 * Main Activity for the Cloud Anchors Codelab.
 *
 *
 * The bulk of the logic resides in the [CloudAnchorFragment]. The activity only creates
 * the fragment and attaches it to this Activity.
 */
class ArActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        var bundle = Bundle()
        val routeId = intent.extras?.get("routeId") as Long?

        val currentRoute = intent.extras?.get("route") as Route?
        val authInfo = intent.extras?.get("authInfo") as FirebaseUser?

        if (routeId != null) {
            bundle.putLong("routeId", routeId)
        }

        bundle.putSerializable("route", currentRoute)
        bundle.putParcelable("authInfo", authInfo)


        val fm = supportFragmentManager
        var frag = fm.findFragmentById(R.id.fragment_container)
        if (frag == null) {
            frag = CloudAnchorFragment()
            frag.arguments = bundle
            fm.beginTransaction().add(R.id.fragment_container, frag).commit()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus)
    }
}