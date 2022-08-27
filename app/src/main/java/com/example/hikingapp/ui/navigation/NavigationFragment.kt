package com.example.hikingapp.ui.navigation

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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
import com.example.hikingapp.NavigationActivity
import com.example.hikingapp.R
import com.example.hikingapp.databinding.FragmentNavigationBinding
import com.example.hikingapp.domain.enums.ActionType
import com.example.hikingapp.domain.map.MapInfo
import com.example.hikingapp.domain.navigation.UserNavigationData
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.persistence.local.LocalDatabase
import com.example.hikingapp.ui.adapters.OnItemCheckedListener
import com.example.hikingapp.ui.adapters.OnItemClickedListener
import com.example.hikingapp.ui.adapters.RouteAdapter
import com.example.hikingapp.utils.GlobalUtils
import com.example.hikingapp.viewModels.AppViewModel
import com.example.hikingapp.viewModels.RouteViewModel
import com.example.hikingapp.viewModels.UserViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.api.matching.v5.MapboxMapMatching
import com.mapbox.api.matching.v5.models.MapMatchingResponse
import com.mapbox.bindgen.Expected
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
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.formatter.UnitType
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.*
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maneuver.view.MapboxManeuverView
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.*
import com.mapbox.navigation.ui.tripprogress.view.MapboxTripProgressView
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import com.mapbox.navigation.ui.voice.model.SpeechError
import com.mapbox.navigation.ui.voice.model.SpeechValue
import com.mapbox.navigation.ui.voice.model.SpeechVolume
import com.mapbox.turf.TurfMeasurement.distance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.HashMap

class NavigationFragment : Fragment(), LocalDBExecutor, OnItemClickedListener {

    private var mapView: MapView? = null
    private lateinit var mapboxMap: MapboxMap
    private val navigationLocationProvider = NavigationLocationProvider()

    private var mapboxNavigation: MapboxNavigation? = null
    private var navigationCamera: NavigationCamera? = null


    private var currentLocation: Point? = null

    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    /**
     * Generates updates for the [MapboxManeuverView] to display the upcoming maneuver instructions
     * and remaining distance to the maneuver point.
     */
    private lateinit var maneuverApi: MapboxManeuverApi

    /**
     * Generates updates for the [MapboxTripProgressView] that include remaining time and distance to the destination.
     */
    private lateinit var tripProgressApi: MapboxTripProgressApi

    /**
     * Generates updates for the [routeLineView] with the geometries and properties of the routes that should be drawn on the map.
     */
    private lateinit var routeLineApi: MapboxRouteLineApi

    /**
     * Draws route lines on the map based on the data from the [routeLineApi]
     */
    private lateinit var routeLineView: MapboxRouteLineView

    /**
     * Generates updates for the [routeArrowView] with the geometries and properties of maneuver arrows that should be drawn on the map.
     */
    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()

    /**
     * Draws maneuver arrows on the map based on the data [routeArrowApi].
     */
    private lateinit var routeArrowView: MapboxRouteArrowView

    /**
     * Stores and updates the state of whether the voice instructions should be played as they come or muted.
     */
    private var isVoiceInstructionsMuted = false
        set(value) {
            field = value
            if (value) {
//                binding.soundButton.mute()
//                binding.soundButton.muteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(0f))
            } else {
//                binding.soundButton.unmute()
//                binding.soundButton.unmuteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(1f))
            }
        }

    /**
     * Extracts message that should be communicated to the driver about the upcoming maneuver.
     * When possible, downloads a synthesized audio file that can be played back to the driver.
     */
    private lateinit var speechApi: MapboxSpeechApi

    /**
     * Plays the synthesized audio files with upcoming maneuver instructions
     * or uses an on-device Text-To-Speech engine to communicate the message to the driver.
     */
    private lateinit var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer

    /**
     * Observes when a new voice instruction should be played.
     */
    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        speechApi.generate(voiceInstructions, speechCallback)
    }

    /**
     * Based on whether the synthesized audio file is available, the callback plays the file
     * or uses the fall back which is played back using the on-device Text-To-Speech engine.
     */
    private val speechCallback =
        MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
            expected.fold(
                { error ->
                    // play the instruction via fallback text-to-speech engine
                    voiceInstructionsPlayer.play(
                        error.fallback,
                        voiceInstructionsPlayerCallback
                    )
                },
                { value ->
                    // play the sound file from the external generator
                    voiceInstructionsPlayer.play(
                        value.announcement,
                        voiceInstructionsPlayerCallback
                    )
                }
            )
        }

    /**
     * When a synthesized audio file was downloaded, this callback cleans up the disk after it was played.
     */
    private val voiceInstructionsPlayerCallback =
        MapboxNavigationConsumer<SpeechAnnouncement> { value ->
            // remove already consumed file to free-up space
            speechApi.clean(value)
        }

    /**
     * Gets notified with progress along the currently active route.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // update the camera position to account for the progressed fragment of the route
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        routeLineApi.updateWithRouteProgress(routeProgress) { result ->
            mapboxMap.getStyle()?.apply {
                routeLineView.renderRouteLineUpdate(this, result)
            }
        }

        // draw the upcoming maneuver arrow on the map
        val style = mapboxMap.getStyle()
        if (style != null) {

            if (TripSessionState.STARTED == mapboxNavigation!!.getTripSessionState()) {
                mapboxMap.getStyle()!!.removeStyleLayer(GlobalUtils.LINE_LAYER_ID)
            }

            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

        // update top banner with maneuver instructions
        val maneuvers = maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold(
            { error ->
                Log.e(this.javaClass.simpleName, error.errorMessage ?: "")
            },
            {
//                binding.maneuverView.visibility = View.VISIBLE
//                binding.maneuverView.renderManeuvers(maneuvers)
            }
        )

    }

    /**
     * Gets notified whenever the tracked routes change.
     *
     * A change can mean:
     * - routes get changed with [MapboxNavigation.setRoutes]
     * - routes annotations get refreshed (for example, congestion annotation that indicate the live traffic along the route)
     * - driver got off route and a reroute was executed
     */
    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.routes.isNotEmpty()) {
            // generate route geometries asynchronously and render them
            val routeLines = routeUpdateResult.routes.map { RouteLine(it, null) }
            routeLineApi.setRoutes(
                routeLines
            ) { value ->
                mapboxMap.getStyle()?.apply {
                    routeLineView.renderRouteDrawData(this, value)
                }
            }
            // update the camera position to account for the new route
            viewportDataSource.onRouteChanged(routeUpdateResult.routes.first())
            viewportDataSource.evaluate()
        } else {
            // remove the route line and route arrow from the map
            val style = mapboxMap.getStyle()
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(
                        style,
                        value
                    )
                }
                routeArrowView.render(style, routeArrowApi.clearArrows())
            }

            // remove the route reference from camera position evaluations
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
        }
    }


    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
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

            if (appViewModel.currentLocation.value == null) {
                appViewModel.currentLocation.postValue(currentLocation)
            }

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

    private val onPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView!!.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView!!.gestures.focalPoint = mapView!!.getMapboxMap().pixelForCoordinate(it)
    }

    private val routeViewModel: RouteViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private val mainPhotosBitmaps = mutableListOf<Bitmap>()
    private val originPoints = mutableListOf<Point>()


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        binding = FragmentNavigationBinding.inflate(inflater, container, false)

//        if (userViewModel.user.value == null) {
//            startActivity(Intent(requireContext(), LoginActivity::class.java))
//        } else {

        val mapFragment: MapFragment =
            (childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment).apply {
                this.setListener(object : MapFragment.OnTouchListener {
                    override fun onTouch() {
                        binding.navigationFragmentId.requestDisallowInterceptTouchEvent(true)
                    }

                })
            }

        initializeNavigationComponents()

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
            NavigationBasicGesturesHandler(navigationCamera!!)
        )
        navigationCamera?.apply { requestNavigationCameraToOverview() }

        val progressBar = binding.progressBar

        resolveRoutesForNavigation()

        appViewModel = ViewModelProvider(this)[AppViewModel::class.java]

        layoutManager = LinearLayoutManager(context)
        recyclerView = binding.searchResultsRecyclerview
        recyclerView.layoutManager = layoutManager

        val noResultsView = binding.noResultsText

        routeViewModel.currentRoutes.observe(viewLifecycleOwner, {
            routes = it!!.toMutableList()

            if (!routes.isNullOrEmpty()) {
                noResultsView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            }

            routes.forEach { route ->

                // 1. Get Main photos of Routes from Storage

                val mainPhotoBitmap: Bitmap? = LocalDatabase.getMainImage(route.routeId, "Route")
                if (mainPhotoBitmap != null) {
                    mainPhotosBitmaps.add(mainPhotoBitmap)
                    if (mainPhotosBitmaps.size == routes.size) {
                        appViewModel.mainPhotos.postValue(mainPhotosBitmaps)
                    }
                } else {
                    storageRef.child(
                        "routes/mainPhotos/${
                            sharedPreferences.getString(
                                route.routeId.toString(),
                                null
                            )
                        }"
                    ).getBytes(1024 * 1024).addOnSuccessListener {
                        route.mainPhotoBitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                        mainPhotosBitmaps.add(route.mainPhotoBitmap!!)
                        if (mainPhotosBitmaps.size == routes.size) {
                            appViewModel.mainPhotos.postValue(mainPhotosBitmaps)
                        }
                    }
                }

                // 2. Get origin points of routes from Database
                FirebaseDatabase.getInstance().getReference("mapData")
                    .child("route_${route.routeId}").child("origin").addValueEventListener(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                route.mapInfo = MapInfo()
                                val origin = snapshot.value as HashMap<String, Double>
                                route.mapInfo?.apply {
                                    this.origin = Point.fromLngLat(
                                        origin["longitude"] as Double,
                                        origin["latitude"] as Double
                                    )
                                    originPoints.add(this.origin)
                                    if (originPoints.size == routes.size) {
                                        appViewModel.origins.postValue(originPoints)
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })

            }


            appViewModel.mainPhotos.observe(viewLifecycleOwner, { photoBitmaps ->
                if (photoBitmaps.size == routes.size && originPoints.size == routes.size && currentLocation != null) {


                    routes.sortBy { route ->
                        distance(currentLocation!!, route.mapInfo!!.origin)
                    }

                    routesAdapter = RouteAdapter(
                        requireContext(),
                        null,
                        routes,
                        itemClickedListener = this,
                        userLoggedIn = userViewModel.user.value != null,
                        actionType = ActionType.NAVIGATION,
                        currentLocation = currentLocation
                    )
                    recyclerView.adapter = routesAdapter

                    progressBar.visibility = View.GONE
                }
            })

            appViewModel.origins.observe(viewLifecycleOwner, { origins ->
                if (origins.size == routes.size && mainPhotosBitmaps.size == routes.size && currentLocation != null) {

                    routes.sortBy { route ->
                        distance(currentLocation!!, route.mapInfo!!.origin)
                    }

                    routesAdapter = RouteAdapter(
                        requireContext(),
                        null,
                        routes,
                        itemClickedListener = this,
                        userLoggedIn = userViewModel.user.value != null,
                        actionType = ActionType.NAVIGATION,
                        currentLocation = currentLocation
                    )
                    recyclerView.adapter = routesAdapter

                    progressBar.visibility = View.GONE
                }
            })

            appViewModel.currentLocation.observe(viewLifecycleOwner, { origins ->
                if (originPoints.size == routes.size && mainPhotosBitmaps.size == routes.size && currentLocation != null) {

                    routes.sortBy { route ->
                        distance(currentLocation!!, route.mapInfo!!.origin)
                    }

                    routesAdapter = RouteAdapter(
                        requireContext(),
                        null,
                        routes,
                        itemClickedListener = this,
                        userLoggedIn = userViewModel.user.value != null,
                        actionType = ActionType.NAVIGATION,
                        currentLocation = currentLocation
                    )
                    recyclerView.adapter = routesAdapter

                    progressBar.visibility = View.GONE
                }
            })

        })

//        }

        return binding.root
    }

    private fun initializeNavigationComponents() {

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

        val distanceFormatterOptions =
            mapboxNavigation!!.navigationOptions.distanceFormatterOptions
                .toBuilder()
                .unitType(UnitType.METRIC)
                .locale(Locale.ENGLISH)
                .build()

        // initialize maneuver api that feeds the data to the top banner maneuver view
        maneuverApi = MapboxManeuverApi(
            MapboxDistanceFormatter(distanceFormatterOptions)
        )

        // initialize bottom progress view
        tripProgressApi = MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(requireContext())
                .distanceRemainingFormatter(
                    DistanceRemainingFormatter(distanceFormatterOptions)
                )
                .timeRemainingFormatter(
                    TimeRemainingFormatter(requireContext())
                )
                .percentRouteTraveledFormatter(
                    PercentDistanceTraveledFormatter()
                )
                .estimatedTimeToArrivalFormatter(
                    EstimatedTimeToArrivalFormatter(requireContext(), TimeFormat.TWENTY_FOUR_HOURS)
                )
                .build()
        )

        // initialize voice instructions api and the voice instruction player
        speechApi = MapboxSpeechApi(
            requireContext(),
            getString(R.string.mapbox_access_token),
            Locale.US.language
        )
        voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(
            requireContext(),
            getString(R.string.mapbox_access_token),
            Locale.US.language
        )

        val traveledResources = RouteLineColorResources.Builder()
            .routeLineTraveledColor(Color.LTGRAY).build()

        // initialize route line, the withRouteLineBelowLayerId is specified to place
        // the route line below road labels layer on the map
        // the value of this option will depend on the style that you are using
        // and under which layer the route line should be placed on the map layers stack
        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(requireContext())
            .withRouteLineBelowLayerId("road-label")
            .withVanishingRouteLineEnabled(true)
            .withRouteLineResources(
                RouteLineResources.Builder().routeLineColorResources(traveledResources).build()
            )
            .build()
        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
        routeLineApi.setVanishingOffset(1.0)
        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)

        // initialize maneuver arrow view to draw arrows on the map
        val routeArrowOptions = RouteArrowOptions.Builder(requireContext()).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun requestCustomRoute(
        filteredCoordintates: List<Point>,
        checkPoints: MutableList<IndexedValue<Point>>,
        wayPointsIncluded: Boolean,
        reversedRoute: Boolean,
        isInitial: Boolean
    ) {

        val routeOptions = RouteOptions.builder()
            .coordinatesList(filteredCoordintates)
            .alternatives(true)
            .bannerInstructions(true)
            .voiceInstructions(true)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .steps(true)
            .waypointIndicesList(mutableListOf(0,1))
            .waypointNamesList(listOf("start", "end"))
            .build()

        val directionsClient = MapboxDirections.builder()
            .accessToken(getString(R.string.mapbox_access_token))
            .routeOptions(routeOptions)
            .build()

        directionsClient.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                if (response.body() == null) {
                    Log.e("DIRECTIONS-RESPONSE-ERROR","No routes found, make sure you set the right user and access token.")
                    return
                } else if (response.body()!!.routes().size < 1) {
                    Log.e("DIRECTIONS-RESPONSE-ERROR","No routes found")
                    return
                }

// Get the directions route
                if (response.isSuccessful) {
                    response.body()?.routes()?.let { routes ->
                        if (!routes.isNullOrEmpty()) {
                            routes[0]?.apply {
//                                routeLineView.showPrimaryRoute(mapboxMap.getStyle()!!)
                                routeLineView.showAlternativeRoutes(mapboxMap.getStyle()!!)
                                setRouteAndStartNavigation(listOf(this))
                            }
                        }
                    }
                }

                val currentRoute = response.body()!!.routes()[0]
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {

            }

        })
    }

    private fun setRouteAndStartNavigation(
        routes: List<DirectionsRoute>
    ) {
        // set routes, where the first route in the list is the primary route that
        // will be used for active guidance
        mapboxNavigation!!.setRoutes(routes)
//        userNavigationData = UserNavigationData(currentRoute!!.routeId)
//        timeCounter = System.currentTimeMillis()
//        // start location simulation along the primary route
//        startSimulation(routes.first())

        // show UI elements
//        binding.soundButton.visibility = View.VISIBLE
//        binding.routeOverview.visibility = View.VISIBLE
//        binding.tripProgressCard.visibility = View.VISIBLE

        // move the camera to overview when new route is available
        navigationCamera!!.requestNavigationCameraToOverview()
    }

    /*private fun startSimulation(route: DirectionsRoute) {
        mapboxReplayer.run {
            stop()
            clearEvents()
            val replayEvents = ReplayRouteMapper().mapDirectionsRouteGeometry(route)
            pushEvents(replayEvents)
            seekTo(replayEvents.first())
            play()
        }
    }*/

    override fun onStart() {
        super.onStart()
        mapboxNavigation?.apply {
            registerLocationObserver(locationObserver)
            registerRoutesObserver(routesObserver)
            registerRouteProgressObserver(routeProgressObserver)
            registerLocationObserver(locationObserver)
            registerVoiceInstructionsObserver(voiceInstructionsObserver)
        }
    }

    override fun onStop() {
        super.onStop()
        mapboxNavigation?.apply {
            unregisterLocationObserver(locationObserver)
            unregisterRoutesObserver(routesObserver)
            unregisterRouteProgressObserver(routeProgressObserver)
            unregisterLocationObserver(locationObserver)
            unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
        }
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
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (mapboxNavigation != null) {
            mapboxNavigation!!.onDestroy()
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun resolveRoutesForNavigation() {
        // Find Closest routes based on a distance thresshold
    }

    override fun onItemClicked(position: Int, bundle: Bundle) {

        val distance = distance(currentLocation!!, routes[position].mapInfo!!.origin)

//        resetMapboxNavigation()

        if ( distance < 5) {
            val intent = Intent(requireContext(), NavigationActivity::class.java)
            intent.putExtra("authInfo", userViewModel.user.value)
            intent.putExtra("route", routes[position])
            intent.putExtra("fromNavigationFragment", true)
            startActivity(intent)
        } else if (distance >=5 && distance < 200){
            println("Route clicked for navigation...")
            requestCustomRoute(mutableListOf(currentLocation!!, routes[position].mapInfo!!.origin),
                mutableListOf(),false,false,false)
        } else {
            println("Route is far away from current location...")
        }
    }

    private fun resetMapboxNavigation() {
        mapboxNavigation?.run {
            onDestroy()
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
                    println("No permission for location tracking...")
                }
                startTripSession()
            }
        }

    }

}