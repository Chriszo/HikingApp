package com.example.hikingapp.ui.navigation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.hikingapp.*
import com.example.hikingapp.domain.culture.Sight
import com.example.hikingapp.domain.enums.DistanceUnitType
import com.example.hikingapp.domain.map.MapInfo
import com.example.hikingapp.domain.navigation.UserNavigationData
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.persistence.firebase.FirebaseUtils
import com.example.hikingapp.persistence.local.LocalDatabase
import com.example.hikingapp.services.map.MapService
import com.example.hikingapp.services.map.MapServiceImpl
import com.example.hikingapp.utils.GlobalUtils
import com.example.hikingapp.viewModels.RouteViewModel
import com.example.hikingapp.viewModels.UserViewModel
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.matching.v5.MapboxMapMatching
import com.mapbox.api.matching.v5.models.MapMatchingResponse
import com.mapbox.api.tilequery.MapboxTilequery
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.*
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.extension.style.image.image
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.formatter.UnitType
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.arrival.ArrivalController
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.*
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.*
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import com.mapbox.navigation.ui.voice.model.SpeechError
import com.mapbox.navigation.ui.voice.model.SpeechValue
import com.mapbox.navigation.ui.voice.model.SpeechVolume
import com.mapbox.turf.TurfMeasurement
import kotlinx.android.synthetic.main.fragment_navigation.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.stream.Collectors

class NavigationFragment : Fragment() {

    private var satelliteMapStyle: ImageView? = null
    private var terrainMapStyle: ImageView? = null
    private var trafficMapStyle: ImageView? = null
    private var lostButton: Button? = null
    private var mapStyleOptions: MaterialCardView? = null
    private var textCurrentElevationView: TextView? = null
    private var textTimeEstimatedView: TextView? = null
    private var textDistanceCoveredView: TextView? = null
    private var textDistanceRemainingView: TextView? = null
    private var mapView: MapView? = null
    private var navigationView: View? = null

    private var playButton: Button? = null
    private var pauseButton: Button? = null
    private var stopButton: Button? = null

    private var checkPointsIndex = 0

    private var nearbyPointsOfInterest = mutableSetOf<Long>()

    private var currentRoute: Route? = null
    private var initialFilteredCoordinates: List<Point> = mutableListOf()

    private var mainRoutes: MutableList<DirectionsRoute> = mutableListOf()

    private var currentLocation: Point? = null

    private var isOutOfRoute = false

    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    private var userNavigationData: UserNavigationData? = null

    private var timeCounter: Long = 0L

    private var checkPoints: MutableList<Int> = mutableListOf()

    private lateinit var navigationViewModel: NavigationViewModel
    private val routeViewModel: RouteViewModel by activityViewModels()

    private val chosenRoutePref: SharedPreferences by lazy {
        requireActivity().applicationContext.getSharedPreferences("chosenRoute", 0)
    }

    private val userViewModel: UserViewModel by activityViewModels()
//    private var _binding: FragmentNavigationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
//    private val binding get() = navigationView!!

    private lateinit var mapboxMap: MapboxMap
    private var mapInfo: MapInfo? = null

    private val mapService: MapService by lazy {
        MapServiceImpl()
    }

    private var associatedSights: MutableList<Sight>? = null

    private val navigationLocationProvider = NavigationLocationProvider()

    private lateinit var mapboxNavigation: MapboxNavigation

    private val mapboxReplayer = MapboxReplayer()

    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)

    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    private lateinit var navigationCamera: NavigationCamera

    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeOverviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            20.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeFollowingPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    private lateinit var maneuverApi: MapboxManeuverApi

    private lateinit var tripProgressApi: MapboxTripProgressApi

    private lateinit var routeLineApi: MapboxRouteLineApi

    private lateinit var routeLineView: MapboxRouteLineView

    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()

    private lateinit var routeArrowView: MapboxRouteArrowView

    private var isVoiceInstructionsMuted = false
        set(value) {
            field = value
            if (value) {
                navigationView!!.soundButton.muteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(0f))
            } else {
                navigationView!!.soundButton.unmuteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(1f))
            }
        }

    private lateinit var speechApi: MapboxSpeechApi

    private lateinit var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer

    private companion object {
        private const val BUTTON_ANIMATION_DURATION = 1500L
    }

    private var routeCompleted: Boolean = false

    @RequiresApi(Build.VERSION_CODES.N)
    private val offRouteObserver = OffRouteObserver { offRoute ->
        if (offRoute) {
            isOutOfRoute = true
            requireActivity().runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    "You are out of your route.",
                    Toast.LENGTH_LONG
                ).show()
            }

            // TODO need to test somehow
            // Re-define a new Route to the last passed checkpoint???
            val lastCheckPointRouteIndex =
                if (checkPoints.isNullOrEmpty()) 0 else checkPoints[checkPointsIndex]
            if (!initialFilteredCoordinates.isNullOrEmpty()) {
                initialFilteredCoordinates[lastCheckPointRouteIndex].let {
                    defineRoute(
                        mutableListOf(it, currentLocation!!),
                        wayPointsIncluded = false,
                        reversedRoute = true
                    )
                }
            }
        }
    }

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

    @RequiresApi(Build.VERSION_CODES.N)
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // update the camera position to account for the progressed fragment of the route
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        // draw the upcoming maneuver arrow on the map
        val style = mapboxMap.getStyle()

        if (style != null) {

            if (TripSessionState.STARTED == mapboxNavigation.getTripSessionState()) {
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
                navigationView!!.maneuverView.visibility = View.VISIBLE
                navigationView!!.maneuverView.renderManeuvers(maneuvers)
            }
        )
        // update user navigation data
        userNavigationData!!.distanceCovered = routeProgress.distanceTraveled.toDouble()
//        userNavigationData.timeSpent = System.currentTimeMillis() - timeCounter

        // update bottom trip progress summary

        navigationView!!.text_distance_remaining.text = getString(
            R.string.distance_remaining_content, GlobalUtils.getTwoDigitsDistance(
                routeProgress.distanceRemaining.toDouble(),
                DistanceUnitType.KILOMETERS
            )
        )


        navigationView!!.text_distance_covered.text = getString(
            R.string.distance_covered_content, GlobalUtils.getTwoDigitsDistance(
                userNavigationData!!.distanceCovered,
                DistanceUnitType.KILOMETERS
            )
        )
        navigationView!!.text_time_estimated.text = getString(
            R.string.estimated_time_content, String.format(
                "%.2f",
                GlobalUtils.getTimeInMinutes(routeProgress.durationRemaining)
            )
        )

    }

    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            // update location puck's position on the map
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )

            currentLocation =
                Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude)

            GlobalScope.launch {
                callElevationDataAPI(enhancedLocation) // Update elevation data value
            }

            if (!isOutOfRoute) {
                associatedSights?.forEach { sight ->
                    GlobalScope.launch {
                        val currentLocation =
                            Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude)
                        val sightLocation =
                            sight.let { Point.fromLngLat(it.point?.lng!!, it.point?.lat!!) }
                        if (TurfMeasurement.distance(currentLocation, sightLocation) < 0.075) {
                            requireActivity().runOnUiThread {
                                if (!nearbyPointsOfInterest.contains(sight.sightId)) {
                                    nearbyPointsOfInterest.add(sight.sightId)
                                    // TODO add Map element(?)
                                    Toast.makeText(
                                        requireContext(),
                                        "You are approaching sight: " + sight.name,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                        }
                    }
                }
            }


            // update camera position to account for new location
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            // if this is the first location update the activity has received,
            // it's best to immediately move the camera to the current user location
            if (!firstLocationUpdateReceived) {
                firstLocationUpdateReceived = true
                navigationCamera.requestNavigationCameraToOverview(
                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                        .maxDuration(0) // instant transition
                        .build()
                )
            }
        }
    }

    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        speechApi.generate(voiceInstructions, speechCallback)
    }

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

    private val voiceInstructionsPlayerCallback =
        MapboxNavigationConsumer<SpeechAnnouncement> { value ->
            // remove already consumed file to free-up space
            speechApi.clean(value)
        }

    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)

    private val arrivalController = object : ArrivalController {
        override fun navigateNextRouteLeg(routeLegProgress: RouteLegProgress): Boolean {
            if (mapboxNavigation.getTripSessionState() == TripSessionState.STOPPED) {
                return false
            }
            return true
        }
    }

    private val arrivalObserver = object : ArrivalObserver {

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onFinalDestinationArrival(routeProgress: RouteProgress) {
            println("FINAL DESTINATION REACHED!")

            userNavigationData?.timeSpent = System.currentTimeMillis() - timeCounter
            userNavigationData?.distanceCovered = routeProgress.distanceTraveled.toDouble()

            if (isOutOfRoute) {
                Toast.makeText(
                    requireContext(),
                    "You have arrived at the last visited checkpoint!",
                    Toast.LENGTH_LONG
                ).show()
                mapboxNavigation.setRoutes(mainRoutes, checkPointsIndex)
                isOutOfRoute = false
            } else {
                routeCompleted = true
                val mainIntent =
                    Intent(requireContext(), EndOfNavigationActivity::class.java)
                mainIntent.putExtra("route", currentRoute)
                mainIntent.putExtra("authInfo", userViewModel.user.value)
                mainIntent.putExtra("userNavigationData", userNavigationData)

                LocalDatabase.saveNavigationDataLocally(
                    userViewModel.user.value!!.uid,
                    userNavigationData!!
                )
                FirebaseUtils.persistNavigation(
                    userViewModel.user.value!!.uid,
                    userNavigationData!!
                )

                startActivity(mainIntent)
//                finish()
            }
        }

        override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {
            checkPointsIndex++
        }

        override fun onWaypointArrival(routeProgress: RouteProgress) {
            Toast.makeText(
                requireContext(),
                "Checkpoint ${checkPointsIndex + 1} reached.",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun callElevationDataAPI(
        currentLocation: Location
    ) {

        val elevationQuery = formElevationRequestQuery(currentLocation)

        elevationQuery.enqueueCall(object : Callback<FeatureCollection> {

            override fun onResponse(
                call: Call<FeatureCollection>,
                response: Response<FeatureCollection>
            ) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    if (response.isSuccessful) {

                        response.body()?.features()
                            ?.stream()
                            ?.mapToLong { feature ->
                                feature.properties()?.get("ele")?.asLong!!
                            }
                            ?.max()
                            ?.ifPresent { max ->
                                if (Objects.isNull(userNavigationData)) {
                                    userNavigationData = UserNavigationData(currentRoute!!.routeId)
                                }
                                userNavigationData?.currentElevation?.add(max)
                                navigationView!!.current_elevation.text =
                                    getString(
                                        R.string.elevation_content,
                                        max.toString()
                                    )
                            }
                        call.cancel()
                    } else {
                        println("handle wrong")
                    }
                } else {
                    //TODO add implementation for backwards compatibility
                }
            }

            override fun onFailure(call: Call<FeatureCollection>, t: Throwable) {
                Log.e(Log.ERROR.toString(), "An error occured " + t.message)
                elevationQuery.cancelCall()
                return
            }
        })
    }

    private fun formElevationRequestQuery(currentLocation: Location): MapboxTilequery {
        return MapboxTilequery.builder()
            .accessToken(getString(R.string.mapbox_access_token))
            .tilesetIds(GlobalUtils.TERRAIN_ID)
            .limit(50)
            .layers(GlobalUtils.TILEQUERY_ATTRIBUTE_REQUESTED_ID)
            .query(Point.fromLngLat(currentLocation.longitude, currentLocation.latitude))
            .build()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)



    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        requireActivity().actionBar?.title = "Navigation"
        if (userViewModel.user.value == null) {
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        } else {

//            _binding = FragmentNavigationBinding.inflate(layoutInflater)

            navigationView = inflater.inflate(R.layout.fragment_navigation, container, false)

//            val root = binding.root

            // initialize Mapbox Navigation
            mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
                MapboxNavigationProvider.retrieve()
            } else {
                MapboxNavigationProvider.create(
                    NavigationOptions.Builder(requireActivity().applicationContext)
                        .accessToken(getString(R.string.mapbox_access_token))
                        // comment out the location engine setting block to disable simulation
                        .locationEngine(replayLocationEngine)
                        .build()
                )
            }


            mapView = navigationView!!.mapView as MapView

            mapboxMap = mapView!!.getMapboxMap()

            textDistanceRemainingView = navigationView!!.text_distance_remaining as TextView
            textDistanceCoveredView = navigationView!!.text_distance_covered as TextView
            textTimeEstimatedView = navigationView!!.text_time_estimated as TextView
            textCurrentElevationView = navigationView!!.current_elevation as TextView

            playButton = navigationView!!.play as Button
            pauseButton = navigationView!!.pause as Button
            stopButton = navigationView!!.stop as Button
            lostButton = navigationView!!.lost_button as Button

            mapStyleOptions = navigationView!!.mapStyle_options as MaterialCardView
            trafficMapStyle = navigationView!!.traffic_map_style as ImageView
            satelliteMapStyle = navigationView!!.satellite_map_style as ImageView
            terrainMapStyle = navigationView!!.terrain_map_style as ImageView

            textDistanceRemainingView!!.text = getString(R.string.distance_remaining_empty)

            textDistanceCoveredView!!.text =
                getString(R.string.distance_covered_empty)

            textTimeEstimatedView!!.text =
                getString(R.string.estimated_time_empty)

            textCurrentElevationView!!.text = ""

            // initialize the location puck
            mapView!!.location.apply {
                this.locationPuck = LocationPuck2D(
                    bearingImage = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.mapbox_navigation_puck_icon
                    )
                )
                setLocationProvider(navigationLocationProvider)
                enabled = true
            }


            // initialize Navigation Camera
            viewportDataSource =
                MapboxNavigationViewportDataSource(mapboxMap)
            navigationCamera = NavigationCamera(
                mapboxMap,
                mapView!!.camera,
                viewportDataSource
            )
            // set the animations lifecycle listener to ensure the NavigationCamera stops
            // automatically following the user location when the map is interacted with
            mapView!!.camera.addCameraAnimationsLifecycleListener(
                NavigationBasicGesturesHandler(navigationCamera)
            )
            navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->

                // shows/hide the recenter button depending on the camera state
                when (navigationCameraState) {
                    NavigationCameraState.TRANSITION_TO_FOLLOWING -> navigationView!!.recenter.visibility =
                        View.GONE
                    NavigationCameraState.FOLLOWING -> navigationView!!.routeOverview.visibility =
                        View.VISIBLE
                    NavigationCameraState.TRANSITION_TO_OVERVIEW -> navigationView!!.routeOverview.visibility =
                        View.GONE
                    NavigationCameraState.OVERVIEW -> navigationView!!.recenter.visibility =
                        View.VISIBLE
                    NavigationCameraState.IDLE -> navigationView!!.routeOverview.visibility =
                        View.GONE
                    NavigationCameraState.IDLE -> navigationView!!.recenter.visibility =
                        View.VISIBLE
                }
            }
            // set the padding values depending on screen orientation and visible view layout
            if (requireContext().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                viewportDataSource.overviewPadding =
                    landscapeOverviewPadding
            } else {
                viewportDataSource.overviewPadding = overviewPadding
            }
            if (requireContext().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                viewportDataSource.followingPadding =
                    landscapeFollowingPadding
            } else {
                viewportDataSource.followingPadding = followingPadding
            }

            // make sure to use the same DistanceFormatterOptions across different features
            val distanceFormatterOptions =
                mapboxNavigation.navigationOptions.distanceFormatterOptions
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
                        EstimatedTimeToArrivalFormatter(
                            requireContext(),
                            TimeFormat.TWENTY_FOUR_HOURS
                        )
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

            // initialize route line, the withRouteLineBelowLayerId is specified to place
            // the route line below road labels layer on the map
            // the value of this option will depend on the style that you are using
            // and under which layer the route line should be placed on the map layers stack
            val mapboxRouteLineOptions =
                MapboxRouteLineOptions.Builder(requireContext())
                    .withRouteLineBelowLayerId("road-label")
                    .build()
            routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
            routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)

            // initialize maneuver arrow view to draw arrows on the map
            val routeArrowOptions =
                RouteArrowOptions.Builder(requireContext()).build()
            routeArrowView = MapboxRouteArrowView(routeArrowOptions)


//            val navigationView: View = binding.navigationView.findViewById(R.id.nav_id)

//            navigationView = inflater.inflate(R.layout.activity_navigation,container,false) as ConstraintLayout?

            if (chosenRoutePref.contains("routeId")) {
                println("Chosen Route Id: ${chosenRoutePref.getLong("routeId", 0L)}")

                val routeId = chosenRoutePref.getLong("routeId", 0L)

                val userAuthInfo = userViewModel.user.value
                currentRoute =
                    routeViewModel.currentRoutes.value?.stream()?.filter { it.routeId == routeId }
                        ?.findFirst()?.orElse(null)
/*
                binding = ActivityNavigationBinding.inflate(layoutInflater)
                setContentView(binding.navigationView)*/


                val routeMapEntity = LocalDatabase.getRouteMapContent(currentRoute!!.routeId)


                mapInfo = mapService.getMapInformation(
                    routeMapEntity!!.routeMapContent,
                    routeMapEntity.routeMapName
                )

                /*  database.getReference("routeMaps")
                      .addValueEventListener(object : ValueEventListener {
                          override fun onDataChange(snapshot: DataSnapshot) {

                              val routeMap = (snapshot.value as HashMap<String, *>).entries
                                  .stream()
                                  .filter { routeMapEntry -> routeMapEntry.key.split("_")[1].toLong() == currentRoute!!.routeId }
                                  .map { it.value as String }
                                  .findFirst().orElse(null)



                              FirebaseStorage.getInstance().getReference("routeMaps/").child(routeMap)
                                  .getBytes(GlobalUtils.MEGABYTE * 5)
                                  .addOnSuccessListener { routeMapBytes ->

                                      currentRoute!!.mapInfo =
                                          mapService.getMapInformation(
                                              String(routeMapBytes),
                                              routeMap
                                          )
                                      LocalDatabase.saveRouteMapContent(
                                          currentRoute!!.routeId,
                                          RouteMapEntity(routeMap, String(routeMapBytes))
                                      )

                                      mapInfo = mapService.getMapInformation(
                                          String(routeMapBytes), //routeMapEntity!!.routeMapContent,
                                          routeMap //routeMapEntity.routeMapName
                                      )*/

                associatedSights =
                    LocalDatabase.getSightsOfRoute(currentRoute!!.routeId)



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
                                +geoJsonSource(GlobalUtils.SYMBOL_SOURCE_ID) {
                                    featureCollection(
                                        FeatureCollection.fromFeatures(
                                            listOf(
                                                Feature.fromGeometry(
                                                    Point.fromLngLat(
                                                        mapInfo!!.origin.longitude(),
                                                        mapInfo!!.origin.latitude()
                                                    )
                                                ),
                                                Feature.fromGeometry(
                                                    Point.fromLngLat(
                                                        mapInfo!!.destination.longitude(),
                                                        mapInfo!!.destination.latitude()
                                                    )
                                                )
                                            )
                                        )
                                    )
                                }
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
                                    iconIgnorePlacement(true)
                                }
                            }
                            ),
                    {
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

                // initialize view interactions
                stopButton!!.setOnClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        clearRouteAndStopNavigation()
                    }

                    var intent: Intent? = null
                    if (routeCompleted) {
                        intent =
                            Intent(context, EndOfNavigationActivity::class.java)
                        userNavigationData?.timeSpent =
                            System.currentTimeMillis() - timeCounter
                        intent.putExtra("route", currentRoute)
                        intent.putExtra(
                            "userNavigationData",
                            userNavigationData
                        )
                        LocalDatabase.saveNavigationDataLocally(
                            userAuthInfo!!.uid,
                            userNavigationData!!
                        )
                        FirebaseUtils.persistNavigation(
                            userAuthInfo!!.uid,
                            userNavigationData!!
                        )

                    } else {
                        intent = Intent(context, MainActivity::class.java)
                        LocalDatabase.saveNavigationDataLocally(
                            userAuthInfo!!.uid,
                            userNavigationData!!
                        )
                        FirebaseUtils.persistUserInCompletedRoute(
                            userAuthInfo!!.uid,
                            userNavigationData!!.routeId
                        )
                    }
                    intent.putExtra("authInfo", userAuthInfo)
                    startActivity(intent)
//                    finish()
                }

                // Action which starts the Navigation
                playButton!!.setOnClickListener {
                    if (mapboxNavigation.getRoutes().isEmpty()) {

                        val routePoints =
                            if (mapInfo!!.jsonRoute is MultiLineString) {
                                (mapInfo!!.jsonRoute as MultiLineString).coordinates()[0]
                            } else {
                                (mapInfo!!.jsonRoute as LineString).coordinates()
                            }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            defineRoute(
                                routePoints!!.toList(),
                                wayPointsIncluded = true,
                                reversedRoute = false,
                                isInitial = true
                            )

                        }

                    } else {
                        resumeNavigation()
                    }
                    playButton!!.visibility = View.GONE
                    pauseButton!!.visibility = View.VISIBLE
                }
                pauseButton!!.setOnClickListener {
                    pauseNavigation()
                    playButton!!.visibility = View.VISIBLE
                    pauseButton!!.visibility = View.GONE
                }
                lostButton!!.setOnClickListener {
                    pauseButton!!.performClick()
                    database.getReference("contacts")
                        .child("${userAuthInfo!!.uid}")
                        .addValueEventListener(object : ValueEventListener {
                            @RequiresApi(Build.VERSION_CODES.N)
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val contacts =
                                        snapshot.value as MutableList<String>
                                    contacts.forEach { phoneNumber ->
                                        phoneNumber.apply {
                                            sendSMS(
                                                this
                                            )
                                        }
                                    }

                                    Toast.makeText(
                                        requireContext(),
                                        "SMS message has been sent to your contacts.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "No contacts have been defined.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                offRouteObserver.onOffRouteStateChanged(true)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                }

                navigationView!!.recenter.setOnClickListener {
                    navigationCamera.requestNavigationCameraToFollowing()
                    navigationView!!.routeOverview.showTextAndExtend(
                        BUTTON_ANIMATION_DURATION
                    )
                }
                navigationView!!.routeOverview.setOnClickListener {
                    navigationCamera.requestNavigationCameraToOverview()
                    navigationView!!.recenter.showTextAndExtend(BUTTON_ANIMATION_DURATION)
                }
                navigationView!!.soundButton.setOnClickListener {
                    // mute/unmute voice instructions
                    isVoiceInstructionsMuted = !isVoiceInstructionsMuted
                }
                navigationView!!.cameraButton.setOnClickListener {

                    //TODO change permission granting
                    if (checkSelfPermission(
                            requireContext(),
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_DENIED
                    )
                        requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            GlobalUtils.CAMERA_REQUEST
                        )


                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(
                        cameraIntent,
                        GlobalUtils.CAMERA_REQUEST
                    )
                }

                navigationView!!.switchMapStyle.setOnClickListener {
                    if (mapStyleOptions!!.visibility == View.GONE) {
                        mapStyleOptions!!.visibility = View.VISIBLE
                    } else if (mapStyleOptions!!.visibility == View.VISIBLE) {
                        mapStyleOptions!!.visibility = View.GONE
                    }
                }

                trafficMapStyle!!.setOnClickListener {
                    setMapStyle(Style.TRAFFIC_DAY, mapInfo!!)
                    mapStyleOptions!!.visibility = View.GONE
                }

                satelliteMapStyle!!.setOnClickListener {
                    setMapStyle(Style.SATELLITE, mapInfo!!)
                    mapStyleOptions!!.visibility = View.GONE
                }

                terrainMapStyle!!.setOnClickListener {
                    setMapStyle(Style.OUTDOORS, mapInfo!!)
                    mapStyleOptions!!.visibility = View.GONE
                }

                // set initial sounds button state
                navigationView!!.soundButton.unmute()

                // start the trip session to being receiving location updates in free drive
                // and later when a route is set also receiving route progress updates
                mapboxNavigation.startTripSession()
            }
            return navigationView
        }
        return null
    }

    private fun setMapStyle(mapStyle: String, mapInfo: MapInfo, routeLineIsVisible: Boolean = true) {
        mapboxMap.loadStyle(
            (
                    style(styleUri = mapStyle) {
                        +geoJsonSource(GlobalUtils.LINE_SOURCE_ID) {
                            url("asset://" + mapInfo.routeGeoJsonFileName)
                        }
                        +geoJsonSource(GlobalUtils.SYMBOL_SOURCE_ID) {
                            featureCollection(
                                FeatureCollection.fromFeatures(
                                    listOf(
                                        Feature.fromGeometry(
                                            Point.fromLngLat(
                                                mapInfo.origin.longitude(),
                                                mapInfo.origin.latitude()
                                            )
                                        ),
                                        Feature.fromGeometry(
                                            Point.fromLngLat(
                                                mapInfo.destination.longitude(),
                                                mapInfo.destination.latitude()
                                            )
                                        )
                                    )
                                )
                            )
                        }
                        +lineLayer(GlobalUtils.LINE_LAYER_ID, GlobalUtils.LINE_SOURCE_ID) {
                            lineCap(LineCap.ROUND)
                            lineJoin(LineJoin.ROUND)
                            lineOpacity(1.0)
                            lineWidth(8.0)
                            lineColor("#FF0000")
                        }
                        +image(GlobalUtils.RED_MARKER_ID) {
                            bitmap(BitmapFactory.decodeResource(resources, R.drawable.red_marker))
                        }
                        +symbolLayer(GlobalUtils.SYMBOL_LAYER_ID, GlobalUtils.SYMBOL_SOURCE_ID) {
                            iconImage(GlobalUtils.RED_MARKER_ID)
                            iconAllowOverlap(true)
                            iconSize(0.5)
                            iconIgnorePlacement(true)
                        }
                    }),
            {
                updateCamera(mapInfo, null)
            },
            object : OnMapLoadErrorListener {
                override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
                    Log.e(
                        MapActivity::class.java.simpleName,
                        "Error loading map: " + eventData.message
                    )
                }
            }
        )
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()
        println("INTO START")

        if (userViewModel.user.value != null) {
            mapboxNavigation.registerRoutesObserver(routesObserver)
            mapboxNavigation.registerOffRouteObserver(offRouteObserver)
            mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
            mapboxNavigation.registerLocationObserver(locationObserver)
            mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
            mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
            mapboxNavigation.setArrivalController(arrivalController)
            mapboxNavigation.registerArrivalObserver(arrivalObserver)


            if (mapboxNavigation.getRoutes().isEmpty()) {
                // if simulation is enabled (ReplayLocationEngine set to NavigationOptions)
                // but we're not simulating yet,
                // push a single location sample to establish origin
                mapboxReplayer.pushEvents(
                    listOf(
                        ReplayRouteMapper.mapToUpdateLocation(
                            eventTimestamp = 0.0,
                            point = mapInfo!!.origin
                        )
                    )
                )
                mapboxReplayer.playFirstLocation()
            }
        }
        // register event listeners
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStop() {
        super.onStop()
        if (userViewModel.user.value != null) {

            // unregister event listeners to prevent leaks or unnecessary resource consumption
            mapboxNavigation.unregisterRoutesObserver(routesObserver)
            mapboxNavigation.unregisterOffRouteObserver(offRouteObserver)
            mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
            mapboxNavigation.unregisterLocationObserver(locationObserver)
            mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
            mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
            mapboxNavigation.setArrivalController(null)
            mapboxNavigation.unregisterArrivalObserver(arrivalObserver)
        }
    }

    private fun updateCamera(mapInfo: MapInfo, bearing: Double?) {
        // Create a polygon
        val triangleCoordinates = listOf(
            listOf(
                Point.fromLngLat(
                    mapInfo.boundingBox?.northeast()
                        ?.longitude()!!, // TODO Handle the setting of bounding box for routes of type LineString
                    mapInfo.boundingBox.northeast().latitude()
                ),
                Point.fromLngLat(
                    mapInfo.boundingBox.southwest().longitude(),
                    mapInfo.boundingBox.southwest().latitude()
                )
            )
        )
        val polygon = Polygon.fromLngLats(triangleCoordinates)
        // Convert to a camera options from a given geometry and padding
        val cameraPosition =
            mapboxMap.cameraForGeometry(polygon, EdgeInsets(75.0, 40.0, 60.0, 40.0))
        // Set camera position
        mapboxMap.setCamera(cameraPosition)
    }

    private fun sendSMS(phoneNumber: String?) {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.SEND_SMS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.SEND_SMS
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.SEND_SMS),
                    0
                );
            }
        }
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(
            phoneNumber,
            null,
            "ATTENTION!!!\n I'm Lost. " +
                    "\n My current location's coordinates are:\n ${currentLocation!!.latitude()}, ${currentLocation!!.longitude()}",
            null,
            null
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun defineRoute(
        routePoints: List<Point>,
        wayPointsIncluded: Boolean,
        reversedRoute: Boolean,
        isInitial: Boolean = false
    ) {

        val modulo = 10

        val filteredCoordintates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            defineRoutePoints(routePoints, modulo)
        } else {
            TODO("VERSION.SDK_INT < N")
        }

        if (isInitial) {
            checkPoints = defineCheckPoints(filteredCoordintates, modulo)
        }

        requestCustomRoute(
            filteredCoordintates,
            checkPoints,
            wayPointsIncluded,
            reversedRoute,
            isInitial
        )
    }

    private fun requestCustomRoute(
        filteredCoordintates: List<Point>,
        checkPoints: List<Int>,
        wayPointsIncluded: Boolean,
        reversedRoute: Boolean,
        isInitial: Boolean
    ) {

        val mapMatchingBuilder = MapboxMapMatching.builder()
            .accessToken(getString(R.string.mapbox_access_token))
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .steps(true)
            .bannerInstructions(true)
            .voiceInstructions(true)
        //TODO Find a more efficient way to compute route points for the obtaining of instructions. This is fully customized to current route at fillopapou.

        if (wayPointsIncluded) {
            mapMatchingBuilder.waypointIndices(*checkPoints.toTypedArray())
        }
        if (reversedRoute) {
            mapMatchingBuilder.coordinates(filteredCoordintates.reversed())
        } else {
            mapMatchingBuilder.coordinates(filteredCoordintates)
        }
        val mapboxMapMatchingRequest = mapMatchingBuilder.build()


        mapboxMapMatchingRequest.enqueueCall(object : Callback<MapMatchingResponse> {
            override fun onResponse(
                call: Call<MapMatchingResponse>,
                response: Response<MapMatchingResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.matchings()?.let { matchingList ->
                        matchingList[0].toDirectionRoute().apply {
                            if (isInitial) {
                                mainRoutes = mutableListOf(this)
                                initialFilteredCoordinates = filteredCoordintates
                            }
                            setRouteAndStartNavigation(listOf(this))
                        }
                    }

                }
            }

            override fun onFailure(call: Call<MapMatchingResponse>, throwable: Throwable) {

            }
        })

    }

    private fun setRouteAndStartNavigation(
        routes: List<DirectionsRoute>
    ) {
        // set routes, where the first route in the list is the primary route that
        // will be used for active guidance
        mapboxNavigation.setRoutes(routes)
        userNavigationData = UserNavigationData(currentRoute!!.routeId)
        timeCounter = System.currentTimeMillis()
        // start location simulation along the primary route
        startSimulation(routes.first())

        // show UI elements
        navigationView!!.soundButton.visibility = View.VISIBLE
        navigationView!!.routeOverview.visibility = View.VISIBLE
        navigationView!!.tripProgressCard.visibility = View.VISIBLE

        // move the camera to overview when new route is available
        navigationCamera.requestNavigationCameraToOverview()
    }

    private fun startSimulation(route: DirectionsRoute) {
        mapboxReplayer.run {
            stop()
            clearEvents()
            val replayEvents = ReplayRouteMapper().mapDirectionsRouteGeometry(route)
            pushEvents(replayEvents)
            seekTo(replayEvents.first())
            play()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun defineRoutePoints(routePoints: List<Point>, modulo: Int): List<Point> {

        var finalRoutePoints: MutableList<IndexedValue<Point>>?
        var mod = modulo
        do {
            finalRoutePoints = mutableListOf()
            finalRoutePoints.add(IndexedValue(0, routePoints[0]))
            routePoints.withIndex().forEach {
                if (it.index != 0 && it.index != routePoints.size - 1) {
                    if (it.index % mod == 0) {
                        finalRoutePoints.add(IndexedValue(it.index, it.value))
                    }
                }
            }
            finalRoutePoints.add(
                IndexedValue(
                    routePoints.size - 1,
                    routePoints[routePoints.size - 1]
                )
            )
            if (finalRoutePoints.size > 25) {
                mod++
            }
        } while (finalRoutePoints?.size!! > 25)

        return finalRoutePoints.stream().map { it.value }.collect(Collectors.toList())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun defineCheckPoints(
        filteredCoordintates: List<Point>,
        modulo: Int
    ): MutableList<Int> {

        var mod = modulo
        var finalCheckPoints: MutableList<IndexedValue<Point>>?
        do {
            finalCheckPoints = mutableListOf()
            finalCheckPoints.add(IndexedValue(0, filteredCoordintates[0]))
            filteredCoordintates.withIndex().forEach {
                if (it.index != 0 && it.index != filteredCoordintates.size - 1) {
                    if (it.index % mod == 0) {
                        finalCheckPoints.add(IndexedValue(it.index, it.value))
                    }
                }
            }
            finalCheckPoints.add(
                IndexedValue(
                    filteredCoordintates.size - 1,
                    filteredCoordintates[filteredCoordintates.size - 1]
                )
            )
            if (finalCheckPoints.size < 3) {
                mod--
            }
        } while (finalCheckPoints?.size!! < 3 && mod > 0)
        return finalCheckPoints.stream().map { it.index }.collect(Collectors.toList())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun clearRouteAndStopNavigation() {
        // clear
        mapboxNavigation.setRoutes(listOf())
        userNavigationData!!.timeSpent = System.currentTimeMillis() - timeCounter
        // stop simulation
        mapboxReplayer.stop()

        // hide UI elements
        navigationView!!.soundButton.visibility = View.INVISIBLE
        navigationView!!.maneuverView.visibility = View.INVISIBLE
        navigationView!!.routeOverview.visibility = View.INVISIBLE
        navigationView!!.tripProgressCard.visibility = View.INVISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun resumeNavigation() {
        if (TripSessionState.STOPPED == mapboxNavigation.getTripSessionState()) {
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
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), 234
                )
//                return
            }
            mapboxNavigation.startTripSession()
        }
    }

    private fun pauseNavigation() {

        if (TripSessionState.STARTED == mapboxNavigation.getTripSessionState()) {
            mapboxNavigation.stopTripSession()
            userNavigationData!!.timeSpent = System.currentTimeMillis() - timeCounter
            // stop simulation
            mapboxReplayer.stop()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
        navigationView = null
    }
}