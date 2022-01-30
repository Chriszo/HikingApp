package com.example.hikingapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.hikingapp.databinding.ActivitySampleNavigationBinding
import com.example.hikingapp.domain.DistanceUnitType
import com.example.hikingapp.persistence.MapInfo
import com.example.hikingapp.persistence.mock.db.MockDatabase
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.api.matching.v5.MapboxMapMatching
import com.mapbox.api.matching.v5.models.MapMatchingResponse
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.*
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.extension.style.image.image
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.formatter.UnitType
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.arrival.ArrivalController
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.*
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maneuver.view.MapboxManeuverView
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
import com.mapbox.navigation.ui.tripprogress.view.MapboxTripProgressView
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import com.mapbox.navigation.ui.voice.model.SpeechError
import com.mapbox.navigation.ui.voice.model.SpeechValue
import com.mapbox.navigation.ui.voice.model.SpeechVolume
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * This example demonstrates a basic turn-by-turn navigation experience by putting together some UI elements to showcase
 * navigation camera transitions, guidance instructions banners and playback, and progress along the route.
 *
 * Before running the example make sure you have put your access_token in the correct place
 * inside [app/src/main/res/values/mapbox_access_token.xml]. If not present then add this file
 * at the location mentioned above and add the following content to it
 *
 * <?xml version="1.0" encoding="utf-8"?>
 * <resources xmlns:tools="http://schemas.android.com/tools">
 *     <string name="mapbox_access_token"><PUT_YOUR_ACCESS_TOKEN_HERE></string>
 * </resources>
 *
 * The example assumes that you have granted location permissions and does not enforce it. However,
 * the permission is essential for proper functioning of this example. The example also uses replay
 * location engine to facilitate navigation without actually physically moving.
 *
 * How to use this example:
 * - You can long-click the map to select a destination.
 * - The guidance will start to the selected destination while simulating location updates.
 * You can disable simulation by commenting out the [replayLocationEngine] setter in [NavigationOptions].
 * Then, the device's real location will be used.
 * - At any point in time you can finish guidance or select a new destination.
 * - You can use buttons to mute/unmute voice instructions, recenter the camera, or show the route overview.
 */
class SampleNavigationActivity : AppCompatActivity() {

    private var mapInfo: MapInfo? = null

    private val hikingRouteOptionsBuilder: RouteOptions.Builder by lazy {
        RouteOptions.builder()
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .steps(true)
            .bannerInstructions(true)
            .annotationsList(
                listOf(
                    DirectionsCriteria.ANNOTATION_CONGESTION_NUMERIC,
                    DirectionsCriteria.ANNOTATION_MAXSPEED,
                    DirectionsCriteria.ANNOTATION_SPEED,
                    DirectionsCriteria.ANNOTATION_DURATION,
                    DirectionsCriteria.ANNOTATION_DISTANCE,
                    DirectionsCriteria.ANNOTATION_CLOSURE
                )
            )
            .enableRefresh(false)
//                .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(this)
//            .coordinatesList(coordinates)
//            .waypointIndicesList(listOf(0, coordinates.size - 1))
        // provide the bearing for the origin of the request to ensure
        // that the returned route faces in the direction of the current user movement
//                .bearingsList(
//                    listOf(
//                        Bearing.builder()
//                            .angle(originLocation.bearing.toDouble())
//                            .degrees(45.0)
//                            .build(),
//                        null
//                    )
//                )
    }

    private val arrivalController = object : ArrivalController {
        override fun navigateNextRouteLeg(routeLegProgress: RouteLegProgress): Boolean {
            if (mapboxNavigation.getTripSessionState() == TripSessionState.STOPPED) {
                return false
            }
            return true
        }
    }

    private companion object {
        private const val BUTTON_ANIMATION_DURATION = 1500L
    }

    /**
     * Debug tool used to play, pause and seek route progress events that can be used to produce mocked location updates along the route.
     */
    private val mapboxReplayer = MapboxReplayer()

    /**
     * Debug tool that mocks location updates with an input from the [mapboxReplayer].
     */
    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)

    /**
     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
     */
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)

    /**
     * Bindings to the example layout.
     */
    private lateinit var binding: ActivitySampleNavigationBinding

    /**
     * Mapbox Maps entry point obtained from the [MapView].
     * You need to get a new reference to this object whenever the [MapView] is recreated.
     */
    private lateinit var mapboxMap: MapboxMap

    /**
     * Mapbox Navigation entry point. There should only be one instance of this object for the app.
     * You can use [MapboxNavigationProvider] to help create and obtain that instance.
     */
    private lateinit var mapboxNavigation: MapboxNavigation

    /**
     * Used to execute camera transitions based on the data generated by the [viewportDataSource].
     * This includes transitions from route overview to route following and continuously updating the camera as the location changes.
     */
    private lateinit var navigationCamera: NavigationCamera

    /**
     * Produces the camera frames based on the location and routing data for the [navigationCamera] to execute.
     */
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    /*
     * Below are generated camera padding values to ensure that the route fits well on screen while
     * other elements are overlaid on top of the map (including instruction view, buttons, etc.)
     */
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
                binding.soundButton.muteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(0f))
            } else {
                binding.soundButton.unmuteAndExtend(BUTTON_ANIMATION_DURATION)
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
     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
     * to the Maps SDK in order to update the user location indicator on the map.
     */
    private val navigationLocationProvider = NavigationLocationProvider()

    /**
     * Gets notified with location updates.
     *
     * Exposes raw updates coming directly from the location services
     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
     */
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

    /**
     * Gets notified with progress along the currently active route.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // update the camera position to account for the progressed fragment of the route
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        // draw the upcoming maneuver arrow on the map
        val style = mapboxMap.getStyle()
        if (style != null) {
            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

        // update top banner with maneuver instructions
        val maneuvers = maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold(
            { error ->
                Toast.makeText(
                    this@SampleNavigationActivity,
                    error.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            },
            {
                binding.maneuverView.visibility = View.VISIBLE
                binding.maneuverView.renderManeuvers(maneuvers)
            }
        )

        // update bottom trip progress summary

        binding.textDistanceRemaining.text = "Remaining:" +
                getTwoDigitsDistance(
                    routeProgress.distanceRemaining.toDouble(),
                    DistanceUnitType.KILOMETERS
                )

        binding.textDistanceCovered.text =
            "Covered:" + getTwoDigitsDistance(
                routeProgress.distanceTraveled.toDouble(),
                DistanceUnitType.KILOMETERS
            )

        binding.textTimeEstimated.text = "Estimated:" + String.format(
            "%.2f",
            getTimeInMinutes(routeProgress.durationRemaining)
        ) + " min"

//        binding.textTimeCovered.text = "Time: " + routeProgress. + " m"
//        binding.tripProgressView.render(
//            tripProgressApi.getTripProgress(routeProgress)
//        )
    }

    private fun getTwoDigitsDistance(
        rawDistance: Double,
        distanceUnitType: DistanceUnitType
    ): String {
        /*if (DistanceUnitType.KILOMETERS == distanceUnitType) {
            return String.format("%.2f", rawDistance).toDouble()
                .div(1000.0)
        }
        return String.format("%.2f", rawDistance).toDouble()*/
        when (distanceUnitType) {
            DistanceUnitType.METERS -> return String.format(
                "%.2f",
                rawDistance
            ) + DistanceUnitType.METERS.distanceUnit
            DistanceUnitType.KILOMETERS -> return String.format(
                "%.2f",
                (rawDistance.div(1000.0))
            ) + DistanceUnitType.KILOMETERS.distanceUnit
        }
    }

    private fun getTimeInMinutes(seconds: Double): Double {
        return seconds.div(60.0)
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

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        println("INTO CREATE")
        super.onCreate(savedInstanceState)
        binding = ActivitySampleNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapboxMap = binding.mapView.getMapboxMap()

        val obj = intent.getSerializableExtra("mapInfo")

        mapInfo = if (obj != null) {
            println("Getting mapInfo from Intent")
            obj as MapInfo
        } else {
            println("Getting mapInfo from retrieveMapInformation() method")
            retrieveMapInformation(null)
        }

        binding.textDistanceRemaining.text = "Remaining: -"

        binding.textDistanceCovered.text = "Covered: -"

        binding.textTimeEstimated.text = "Estimated: -"

//        binding.textTimeCovered.text = "Time: -"

        // initialize the location puck
        binding.mapView.location.apply {
            this.locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    this@SampleNavigationActivity,
                    R.drawable.mapbox_navigation_puck_icon
                )
            )
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }
        // initialize Mapbox Navigation
        mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(this.applicationContext)
                    .accessToken(getString(R.string.mapbox_access_token))
                    // comment out the location engine setting block to disable simulation
                    .locationEngine(replayLocationEngine)
                    .build()
            )
        }

        // initialize Navigation Camera
        viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap)
        navigationCamera = NavigationCamera(
            mapboxMap,
            binding.mapView.camera,
            viewportDataSource
        )
        // set the animations lifecycle listener to ensure the NavigationCamera stops
        // automatically following the user location when the map is interacted with
        binding.mapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )
        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->

            // shows/hide the recenter button depending on the camera state
            when (navigationCameraState) {
                NavigationCameraState.TRANSITION_TO_FOLLOWING -> binding.recenter.visibility =
                    View.GONE
                NavigationCameraState.FOLLOWING -> binding.routeOverview.visibility = View.VISIBLE
                NavigationCameraState.TRANSITION_TO_OVERVIEW -> binding.routeOverview.visibility =
                    View.GONE
                NavigationCameraState.OVERVIEW -> binding.recenter.visibility = View.VISIBLE
                NavigationCameraState.IDLE -> binding.routeOverview.visibility = View.GONE
                NavigationCameraState.IDLE -> binding.recenter.visibility = View.VISIBLE
            }
        }
        // set the padding values depending on screen orientation and visible view layout
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.overviewPadding = landscapeOverviewPadding
        } else {
            viewportDataSource.overviewPadding = overviewPadding
        }
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.followingPadding = landscapeFollowingPadding
        } else {
            viewportDataSource.followingPadding = followingPadding
        }

        // make sure to use the same DistanceFormatterOptions across different features
        val distanceFormatterOptions = mapboxNavigation.navigationOptions.distanceFormatterOptions
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
            TripProgressUpdateFormatter.Builder(this)
                .distanceRemainingFormatter(
                    DistanceRemainingFormatter(distanceFormatterOptions)
                )
                .timeRemainingFormatter(
                    TimeRemainingFormatter(this)
                )
                .percentRouteTraveledFormatter(
                    PercentDistanceTraveledFormatter()
                )
                .estimatedTimeToArrivalFormatter(
                    EstimatedTimeToArrivalFormatter(this, TimeFormat.TWENTY_FOUR_HOURS)
                )
                .build()
        )

        // initialize voice instructions api and the voice instruction player
        speechApi = MapboxSpeechApi(
            this,
            getString(R.string.mapbox_access_token),
            Locale.US.language
        )
        voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(
            this,
            getString(R.string.mapbox_access_token),
            Locale.US.language
        )

        // initialize route line, the withRouteLineBelowLayerId is specified to place
        // the route line below road labels layer on the map
        // the value of this option will depend on the style that you are using
        // and under which layer the route line should be placed on the map layers stack
        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(this)
            .withRouteLineBelowLayerId("road-label")
            .build()
        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)

        // initialize maneuver arrow view to draw arrows on the map
        val routeArrowOptions = RouteArrowOptions.Builder(this).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)

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
                            bitmap(BitmapFactory.decodeResource(resources, R.drawable.red_marker))
                        }
                        +symbolLayer(GlobalUtils.SYMBOL_LAYER_ID, GlobalUtils.SYMBOL_SOURCE_ID) {
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
                        SampleNavigationActivity::class.java.simpleName,
                        "Error loading map: " + eventData.message
                    )
                }
            }
        )

        // initialize view interactions
        binding.stop.setOnClickListener {
            clearRouteAndStopNavigation()
            // TODO Go to another Fragment/ Activity (?)
        }

        // Action which starts the Navigation
        binding.play.setOnClickListener {
            if (mapboxNavigation.getRoutes().isEmpty()) {
                findRoute(mapInfo!!.jsonRoute.coordinates()[0])
            } else {
                resumeNavigation()
            }
            binding.play.visibility = View.GONE
            binding.pause.visibility = View.VISIBLE
        }
        binding.pause.setOnClickListener {
            pauseNavigation()
            binding.play.visibility = View.VISIBLE
            binding.pause.visibility = View.GONE
        }
        binding.lostButton.setOnClickListener {
            // TODO Inform Contacts for being lost functionality
        }

        binding.recenter.setOnClickListener {
            navigationCamera.requestNavigationCameraToFollowing()
            binding.routeOverview.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        binding.routeOverview.setOnClickListener {
            navigationCamera.requestNavigationCameraToOverview()
            binding.recenter.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        binding.soundButton.setOnClickListener {
            // mute/unmute voice instructions
            isVoiceInstructionsMuted = !isVoiceInstructionsMuted
        }
        binding.cameraButton.setOnClickListener {

            //TODO change permission granting
            val cameraRequest = 1888
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
                requestPermissions(arrayOf(Manifest.permission.CAMERA), cameraRequest)


            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, cameraRequest)
        }

        // set initial sounds button state
        binding.soundButton.unmute()

        // start the trip session to being receiving location updates in free drive
        // and later when a route is set also receiving route progress updates
        mapboxNavigation.startTripSession()
    }

    private fun filterRoutePoints(coordinates: List<Point>, modulo: Int): MutableList<Point> {
        var counter = 0
        return coordinates.filterIndexed { index, _ -> index % modulo == 0 && ++counter < 25 }
            .toMutableList()
    }

    private fun retrieveMapInformation(routeName: String?): MapInfo {

        val jsonSource = assets.open(MockDatabase.routesMap["Philopapou"]?.second!!).readBytes()
            .toString(Charsets.UTF_8)
        val routeJson: MultiLineString =
            FeatureCollection.fromJson(jsonSource).features()?.get(0)?.geometry() as MultiLineString

        val origin: Point = routeJson.coordinates()[0][0]
        val destination: Point = routeJson.coordinates()[0][routeJson.coordinates()[0].size - 1]

        return MapInfo(
            origin,
            destination,
            routeJson.bbox()!!,
            routeJson,
            null,
            MockDatabase.routesMap["Philopapou"]?.second!!,
            false
        )
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()
        println("INTO START")

        // register event listeners
        mapboxNavigation.registerRoutesObserver(routesObserver)
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.registerLocationObserver(locationObserver)
        mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
        mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
        mapboxNavigation.setArrivalController(arrivalController)

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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStop() {
        super.onStop()

        // unregister event listeners to prevent leaks or unnecessary resource consumption
        mapboxNavigation.unregisterRoutesObserver(routesObserver)
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
        mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
    }

    override fun onDestroy() {
        println("ON DESTROY CALLED")
        super.onDestroy()
        MapboxNavigationProvider.destroy()
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
    }

    private fun findRoute(coordinates: List<Point>) {
        val originLocation = navigationLocationProvider.lastLocation
        val originPoint = originLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return

        requestCustomRoute(coordinates)

        // execute a route request
        // it's recommended to use the
        // applyDefaultNavigationOptions and applyLanguageAndVoiceUnitOptions
        // that make sure the route request is optimized
        // to allow for support of all of the Navigation SDK features

//        mapboxNavigation.requestRoutes(
//            hikingRouteOptionsBuilder
//                .coordinatesList(coordinates)
//                .waypointIndicesList(listOf(0, coordinates.size - 1))
//                .build(),
//            object : RouterCallback {
//                override fun onRoutesReady(
//                    routes: List<DirectionsRoute>,
//                    routerOrigin: RouterOrigin
//                ) {
//                    setRouteAndStartNavigation(routes)
//                }
//
//                override fun onFailure(
//                    reasons: List<RouterFailure>,
//                    routeOptions: RouteOptions
//                ) {
//                    // no impl
//                    println("AN ERROR OCCURED")
//                    println(reasons)
//                }
//
//                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
//                    // no impl
//                }
//            }
//        )
    }

    private fun requestCustomRoute(coordinates: List<Point>) {

        val mapboxMapMatchingRequest = MapboxMapMatching.builder()
            .accessToken(getString(R.string.mapbox_access_token))
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .steps(true)
            .bannerInstructions(true)
            .voiceInstructions(true)
            //TODO Find a more eficient way to compute route points for the obtaining of instructions. This is fully customed to current route at fillopapou.
            .coordinates(
                listOf(
                    coordinates[0],
                    coordinates[12],
                    coordinates[24],
                    coordinates[36],
                    coordinates[48],
                    coordinates[60],
                    coordinates[72],
                    coordinates[84],
                    coordinates[96],
                    coordinates[108],
                    coordinates[120],
                    coordinates[132],
                    coordinates[144],
                    coordinates[156],
                    coordinates[168],
                    coordinates[180],
                    coordinates[192],
                    coordinates[204],
                    coordinates[216],
                    coordinates[228],
                    coordinates[240],
                    coordinates[252],
                    coordinates[264],
                    coordinates[276],
                    coordinates[coordinates.size - 1]
                )
            )
            .waypointIndices(0, 24)

            //DEFAULT

//            .waypointIndices(0, 12)
//            .steps(true)
//            .voiceInstructions(true)
//            .bannerInstructions(true)
//            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .build()

        mapboxMapMatchingRequest.enqueueCall(object : Callback<MapMatchingResponse> {
            override fun onResponse(
                call: Call<MapMatchingResponse>,
                response: Response<MapMatchingResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.matchings()?.let { matchingList ->
                        matchingList[0].toDirectionRoute().apply {
                            setRouteAndStartNavigation(listOf(this))
                        }
                    }

                }
            }

            override fun onFailure(call: Call<MapMatchingResponse>, throwable: Throwable) {

            }
        })

    }

    private fun setRouteAndStartNavigation(routes: List<DirectionsRoute>) {
        // set routes, where the first route in the list is the primary route that
        // will be used for active guidance
        mapboxNavigation.setRoutes(routes)

        // start location simulation along the primary route
        startSimulation(routes.first())

        // show UI elements
        binding.soundButton.visibility = View.VISIBLE
        binding.routeOverview.visibility = View.VISIBLE
        binding.tripProgressCard.visibility = View.VISIBLE

        // move the camera to overview when new route is available
        navigationCamera.requestNavigationCameraToOverview()
    }

    private fun clearRouteAndStopNavigation() {
        // clear
        mapboxNavigation.setRoutes(listOf())

        // stop simulation
        mapboxReplayer.stop()

        // hide UI elements
        binding.soundButton.visibility = View.INVISIBLE
        binding.maneuverView.visibility = View.INVISIBLE
        binding.routeOverview.visibility = View.INVISIBLE
        binding.tripProgressCard.visibility = View.INVISIBLE
    }

    private fun pauseNavigation() {

        if (TripSessionState.STARTED == mapboxNavigation.getTripSessionState()) {
            mapboxNavigation.stopTripSession()

            // stop simulation
            mapboxReplayer.stop()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun resumeNavigation() {
        if (TripSessionState.STOPPED == mapboxNavigation.getTripSessionState()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
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

    /*private fun permissionsAreGranted(operator: String, vararg permissions: String): Boolean {
        val permissionValues = listOf<Boolean>().toMutableList()
        permissions.iterator().forEach {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionValues.add(false)
            } else {
                permissionValues.add(true)
            }
        }

        if ((operator.contentEquals("AND") && permissionValues.contains(false)) ||
            (operator.contentEquals("OR") && !permissionValues.contains(true))
        ) {
            return false
        }
        return true
    }*/

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
}
