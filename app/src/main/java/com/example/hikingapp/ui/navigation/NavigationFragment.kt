package com.example.hikingapp.ui.navigation

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingapp.LocalDBExecutor
import com.example.hikingapp.LoginActivity
import com.example.hikingapp.NavigationActivity
import com.example.hikingapp.R
import com.example.hikingapp.databinding.FragmentNavigationBinding
import com.example.hikingapp.domain.enums.ActionType
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.ui.adapters.OnItemCheckedListener
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.RouteAdapter
import com.example.hikingapp.utils.GlobalUtils
import com.example.hikingapp.viewModels.AppViewModel
import com.example.hikingapp.viewModels.RouteViewModel
import com.example.hikingapp.viewModels.UserViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.image.image
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import java.util.stream.Collectors

class NavigationFragment : Fragment(), LocalDBExecutor, OnItemClickedListener,
    OnItemCheckedListener {

    private var mapView: MapView? = null
    private lateinit var mapboxMap: MapboxMap
    private val navigationLocationProvider = NavigationLocationProvider()

    private lateinit var mapboxNavigation: MapboxNavigation
    private lateinit var navigationCamera: NavigationCamera


    private var currentLocation: Point? = null

    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource


    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
            println("New location...")
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            // update location puck's position on the map
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )

            currentLocation =
                Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude)

//            when (navigationState) {
//                NavigationState.STARTED -> {
//                    val locationKey =
//                        "${currentLocation!!.longitude()}_${currentLocation!!.latitude()}"
//
//                    if (currentRoute!!.routeInfo!!.navigationData!!.containsKey(locationKey)) {
//                        addPointToGraph(currentRoute!!.routeInfo!!.navigationData!![locationKey])
//                    }
//                }
//            }
//
//            if (onNavigationMode) {
//
//                val locationKey = "${currentLocation!!.longitude()}_${currentLocation!!.latitude()}"
//
//                if (currentRoute!!.routeInfo!!.navigationData!!.containsKey(locationKey)) {
//                    addPointToGraph(currentRoute!!.routeInfo!!.navigationData!![locationKey])
//                }
//
//            }

//            else {
//                GlobalScope.launch {
//                    callElevationDataAPIAsync(enhancedLocation) // Update elevation data value
//                }
//            }


//            if (!isOutOfRoute) {
//                associatedSights?.forEach { sight ->
//                    GlobalScope.launch {
//                        val currentLocation =
//                            Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude)
//                        val sightLocation =
//                            sight.let { Point.fromLngLat(it.point?.lng!!, it.point?.lat!!) }
//                        if (TurfMeasurement.distance(currentLocation, sightLocation) < 0.075) {
//                            runOnUiThread {
//                                if (!nearbyPointsOfInterest.contains(sight.sightId)) {
//                                    nearbyPointsOfInterest.add(sight.sightId)
//                                    // TODO add Map element(?)
//                                    Toast.makeText(
//                                        this@NavigationActivity,
//                                        "You are approaching sight: " + sight.name,
//                                        Toast.LENGTH_LONG
//                                    ).show()
//                                }
//                            }
//
//                        }
//                    }
//                }
//            }


            // update camera position to account for new location
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            // if this is the first location update the activity has received,
            // it's best to immediately move the camera to the current user location
//            if (!firstLocationUpdateReceived) {
//                firstLocationUpdateReceived = true
//                navigationCamera.requestNavigationCameraToOverview(
//                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
//                        .maxDuration(0) // instant transition
//                        .build()
//                )
//            }
        }
    }


    private lateinit var binding: FragmentNavigationBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var routesAdapter: RouteAdapter
    private lateinit var routes: MutableList<Route>

    private var routesForNavigation = mutableListOf<Route>()

    private lateinit var appViewModel: AppViewModel

    private val storageRef: StorageReference by lazy {
        FirebaseStorage.getInstance().reference
    }

    private val sharedPreferences: SharedPreferences by lazy {
        requireActivity().applicationContext.getSharedPreferences("mainPhotoPrefs", 0)
    }

    private val checkedRoutesPrefs: SharedPreferences by lazy {
        requireActivity().applicationContext.getSharedPreferences("checkedRoutePrefs", 0)
    }

    private val onPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView!!.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView!!.gestures.focalPoint = mapView!!.getMapboxMap().pixelForCoordinate(it)
    }

    private val routeViewModel: RouteViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        binding = FragmentNavigationBinding.inflate(inflater, container, false)

        if (userViewModel.user.value == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        } else {

            val mapFragment: MapFragment =
                (childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment).apply {
                    this.setListener(object : MapFragment.OnTouchListener {
                        override fun onTouch() {
                            binding.navigationFragmentId.requestDisallowInterceptTouchEvent(true)
                        }

                    })
                }

            mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
                MapboxNavigationProvider.retrieve()
            } else {
                MapboxNavigationProvider.create(
                    NavigationOptions.Builder(requireActivity().applicationContext)
                        .accessToken(getString(R.string.mapbox_access_token))
                        // comment out the location engine setting block to disable simulation
//                        .locationEngine(replayLocationEngine)
                        .build()
                ).apply {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        println("No permission for location tracking...")
                    }
                    startTripSession()
                }
            }

            mapView = mapFragment.requireView().findViewById(R.id.mapView)
            mapboxMap = mapView!!.getMapboxMap()

            mapView!!.location.apply {
                this.locationPuck = LocationPuck2D(
                    bearingImage = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.mapbox_navigation_puck_icon
                    )
                )
                setLocationProvider(navigationLocationProvider)
                enabled = true

                loadMap()
            }


            viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap)
            navigationCamera = NavigationCamera(
                mapboxMap,
                mapView!!.camera,
                viewportDataSource
            )
            mapView!!.camera.addCameraAnimationsLifecycleListener(
                NavigationBasicGesturesHandler(navigationCamera)
            )
            navigationCamera.requestNavigationCameraToOverview()

            val progressBar = binding.progressBar


            checkedRoutesPrefs.registerOnSharedPreferenceChangeListener { sharedPreferences, s ->
                resolveRoutesForNavigation()
            }

            resolveRoutesForNavigation()

            appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

            layoutManager = LinearLayoutManager(context)
            recyclerView = binding.searchResultsRecyclerview
            recyclerView.layoutManager = layoutManager

            val noResultsView = binding.noResultsText

            if (!routes.isNullOrEmpty()) {
                noResultsView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            }

            routes.forEach { route ->
                storageRef.child(
                    "routes/mainPhotos/${
                        sharedPreferences.getString(
                            route.routeId.toString(),
                            null
                        )
                    }"
                ).getBytes(1024 * 1024).addOnSuccessListener {
                    route.mainPhotoBitmap = BitmapFactory.decodeByteArray(it, 0, it.size)

                    val temp =
                        if (appViewModel.mainPhotos.value == null) mutableListOf() else appViewModel.mainPhotos.value
                    temp?.add(route.mainPhotoBitmap!!)
                    appViewModel.mainPhotos.postValue(temp)
                }
            }

            appViewModel.mainPhotos.observe(viewLifecycleOwner, { photoBitmaps ->
                if (photoBitmaps.size == routes.size) {
                    routesAdapter = RouteAdapter(
                        requireContext(),
                        null,
                        routes,
                        itemClickedListener = this,
                        itemCheckedListener = this,
                        userLoggedIn = userViewModel.user.value != null,
                        actionType = ActionType.NAVIGATION
                    )
                    recyclerView.adapter = routesAdapter

                    progressBar.visibility = View.GONE
                }
            })

            routeViewModel.routesSelectedForNavigation.observe(viewLifecycleOwner, {
                resolveRoutesForNavigation()
            })
        }


        return binding.root
    }

    override fun onStart() {
        super.onStart()
        mapboxNavigation.registerLocationObserver(locationObserver)
    }

    override fun onStop() {
        super.onStop()
        mapboxNavigation.unregisterLocationObserver(locationObserver)
    }

    private fun initLocationComponent() {

        mapView!!.location.updateSettings {
            this.locationPuck = LocationPuck2D(
                topImage = AppCompatResources.getDrawable(
                    requireContext(),
                    com.mapbox.maps.plugin.locationcomponent.R.drawable.mapbox_user_icon
                ),
                bearingImage = AppCompatResources.getDrawable(
                    requireContext(),
                    com.mapbox.maps.plugin.locationcomponent.R.drawable.mapbox_user_bearing_icon
                ),
                shadowImage = AppCompatResources.getDrawable(
                    requireContext(),
                    com.mapbox.maps.plugin.locationcomponent.R.drawable.mapbox_user_stroke_icon
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )

            this.pulsingEnabled = true
            this.enabled = true
        }
    }

    private fun loadMap() {
        mapboxMap.loadStyle(
            (
                    style(styleUri = Style.SATELLITE) {
//                        +geoJsonSource("line") {
////                            url("asset://seichsou_trail.geojson")
//                            url("asset://" + mapInfo?.routeGeoJsonFileName)
//                        }
//                        +lineLayer(GlobalUtils.LINE_LAYER_ID, GlobalUtils.LINE_SOURCE_ID) {
//                            lineCap(LineCap.ROUND)
//                            lineJoin(LineJoin.ROUND)
//                            lineOpacity(1.0)
//                            lineWidth(8.0)
//                            lineColor("#FF0000")
//                        }
//                        +geoJsonSource(GlobalUtils.SYMBOL_SOURCE_ID) {
//                            featureCollection(
//                                FeatureCollection.fromFeatures(
//                                    listOf(
//                                        Feature.fromGeometry(
//                                            Point.fromLngLat(
//                                                mapInfo!!.origin.longitude(),
//                                                mapInfo!!.origin.latitude()
//                                            )
//                                        ),
//                                        Feature.fromGeometry(
//                                            Point.fromLngLat(
//                                                mapInfo!!.destination.longitude(),
//                                                mapInfo!!.destination.latitude()
//                                            )
//                                        )
//                                    )
//                                )
//                            )
//                        }

                        +image(GlobalUtils.RED_MARKER_ID) {
                            bitmap(
                                BitmapFactory.decodeResource(
                                    resources,
                                    R.drawable.red_marker
                                )
                            )
                        }
                        +symbolLayer(
                            GlobalUtils.SYMBOL_LAYER_ID,
                            GlobalUtils.SYMBOL_SOURCE_ID
                        ) {
                            iconImage(GlobalUtils.RED_MARKER_ID)
                            iconAllowOverlap(true)
                            iconSize(0.5)
                            iconAnchor(IconAnchor.BOTTOM)
                            iconIgnorePlacement(true)
                        }
                    }
                    ),
            {

                mapView!!.location.addOnIndicatorPositionChangedListener(
                    onPositionChangedListener
                )
//                    routeLineView.hideOriginAndDestinationPoints(it)
//                mapboxMap.addOnMapLoadedListener {
//                    findRoute(mapInfo!!.jsonRoute.coordinates()[0])
//                }
            },
            object : OnMapLoadErrorListener {
                override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
                    Log.e(
                        NavigationActivity::class.java.simpleName,
                        "Error loading map: " + eventData.message
                    )
                }
            }
        )

//        initializeMapboxNavigationInstance()
//
//        if (mapboxNavigation.getRoutes().isEmpty()) {
//            // if simulation is enabled (ReplayLocationEngine set to NavigationOptions)
//            // but we're not simulating yet,
//            // push a single location sample to establish origin
//            mapboxReplayer.pushEvents(
//                listOf(
//                    ReplayRouteMapper.mapToUpdateLocation(
//                        eventTimestamp = 0.0,
//                        point = mapInfo!!.origin
//                    )
//                )
//            )
//
//        }
    }

    private fun updateCamera(point: Point, bearing: Double?) {

        initLocationComponent()

        /* viewBinding.mapView.camera.easeTo(
             CameraOptions.Builder()
                 .center(point)
                 .bearing(bearing)
                 .pitch(0.0)
                 .zoom(15.0)
                 .padding(EdgeInsets(1000.0, 0.0, 0.0, 0.0))
                 .build(),
             mapAnimationOptionsBuilder.build()
         )*/

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onDestroyView() {
        super.onDestroyView()

        checkedRoutesPrefs.getStringSet(GlobalUtils.routeIdsForNavigation, mutableSetOf())!!
            .toList().apply {
                FirebaseDatabase.getInstance()
                    .getReference("selected_routes_nav")
                    .child(userViewModel.user.value!!.uid)
                    .setValue(this)
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun resolveRoutesForNavigation() {
        checkedRoutesPrefs.getStringSet(GlobalUtils.routeIdsForNavigation, null)?.let { routeIds ->

            routeViewModel.routesSelectedForNavigation.value?.apply {
                routeIds.addAll(
                    this.stream()
                        .map { it.routeId.toString() }
                        .collect(Collectors.toList())
                )
            } ?: mutableListOf()

            routes = routeViewModel.currentRoutes.value!!
                .stream()
                .filter { routeIds.contains(it.routeId.toString()) }
                .collect(Collectors.toList())
        }
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {
        val intent = Intent(requireContext(), NavigationActivity::class.java)

        intent.putExtra("authInfo", userViewModel.user.value)
        intent.putExtra("route", routes[position])
        storeSelectedRoutesForNavigation()

        startActivity(intent)
    }

    private fun storeSelectedRoutesForNavigation() {
        val routeIdsSet = mutableSetOf<String>()
        routesForNavigation.forEach {
            routeIdsSet.add(it.routeId.toString())
        }
        routeViewModel.routesSelectedForNavigation.postValue(routesForNavigation)
        checkedRoutesPrefs.edit().putStringSet(GlobalUtils.routeIdsForNavigation, routeIdsSet)
            .apply()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onItemChecked(position: Int) {
        val routeToRemove = routes[position]

        routes.remove(routeToRemove).also {
            routesAdapter = RouteAdapter(
                requireContext(),
                null,
                routes,
                itemClickedListener = this,
                itemCheckedListener = this,
                userLoggedIn = userViewModel.user.value != null,
                actionType = ActionType.NAVIGATION
            )
            recyclerView.adapter = routesAdapter
        }
        routesForNavigation.removeIf {
            it.routeId == routeToRemove.routeId
        }

        routeViewModel.routesSelectedForNavigation.postValue(routesForNavigation)
        val routeIdsSet =
            checkedRoutesPrefs.getStringSet(GlobalUtils.routeIdsForNavigation, mutableSetOf())
        routeIdsSet!!.removeIf { it == routeToRemove.routeId.toString() }
        checkedRoutesPrefs.edit().putStringSet(GlobalUtils.routeIdsForNavigation, routeIdsSet)
            .apply()
    }

    override fun onItemUnchecked(position: Int) {
        TODO("Not yet implemented")
    }

}