package com.example.hikingapp

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.hikingapp.R
import com.example.hikingapp.databinding.ActivitySampleNavigationBinding
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.api.matching.v5.MapboxMapMatching
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.MultiLineString
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
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
import com.mapbox.navigation.ui.tripprogress.model.DistanceRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.ui.tripprogress.model.PercentDistanceTraveledFormatter
import com.mapbox.navigation.ui.tripprogress.model.TimeRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.ui.tripprogress.view.MapboxTripProgressView
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement
import com.mapbox.navigation.ui.voice.model.SpeechError
import com.mapbox.navigation.ui.voice.model.SpeechValue
import com.mapbox.navigation.ui.voice.model.SpeechVolume
import java.util.Locale

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
        binding.tripProgressView.render(
            tripProgressApi.getTripProgress(routeProgress)
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

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapboxMap = binding.mapView.getMapboxMap()

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
                NavigationCameraState.TRANSITION_TO_FOLLOWING,
                NavigationCameraState.FOLLOWING -> binding.recenter.visibility = View.INVISIBLE
                NavigationCameraState.TRANSITION_TO_OVERVIEW,
                NavigationCameraState.OVERVIEW,
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
                    EstimatedTimeToArrivalFormatter(this, TimeFormat.NONE_SPECIFIED)
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

//         load map style
//        mapboxMap.loadStyleUri(
//            Style.MAPBOX_STREETS
//        ) {
//            // add long click listener that search for a route to the clicked destination
//            binding.mapView.gestures.addOnMapLongClickListener { point ->
//                findRoute(point)
//                true
//            }
//        }

        mapboxMap.loadStyle(
            (
                    style(styleUri = Style.OUTDOORS) {
                        +geoJsonSource("line") {
//                            url("asset://seichsou_trail.geojson")
                            url("asset://philopappou_trail.geojson")
                        }
                        +lineLayer("linelayer", "line") {
                            lineCap(LineCap.ROUND)
                            lineJoin(LineJoin.ROUND)
                            lineOpacity(0.7)
                            lineWidth(8.0)
                            lineColor("#FF0000")
                        }
                    }
                    ),
            {
                // In each trail, the start and end point will be constant and defined once in route initialization.

                val coordinates = MultiLineString.fromJson("""
                                {
                            "type": "MultiLineString",
                            "coordinates": [[[23.72004, 37.97462, 71.0], [23.72005, 37.97472, 70.0], [23.71983, 37.97474, 73.0], [23.71989, 37.97458, 72.0], [23.7201, 37.97406, 76.0], [23.72019, 37.97383, 78.0], [23.72025, 37.97366, 79.0], [23.72036, 37.97337, 80.0], [23.72051, 37.97295, 80.0], [23.72059, 37.97274, 80.0], [23.72068, 37.97278, 79.0], [23.72071, 37.97281, 79.0], [23.72071, 37.97275, 79.0], [23.72071, 37.97271, 79.0], [23.72072, 37.97267, 79.0], [23.72072, 37.97265, 79.0], [23.72074, 37.97263, 79.0], [23.72076, 37.97261, 79.0], [23.72078, 37.97259, 79.0], [23.7208, 37.97258, 79.0], [23.72083, 37.97257, 79.0], [23.72085, 37.97256, 79.0], [23.72088, 37.97257, 79.0], [23.7209, 37.97258, 79.0], [23.72092, 37.97258, 79.0], [23.72094, 37.97257, 79.0], [23.72096, 37.97256, 79.0], [23.72099, 37.97255, 80.0], [23.72101, 37.97254, 80.0], [23.72104, 37.97254, 80.0], [23.72108, 37.97254, 80.0], [23.72112, 37.97254, 80.0], [23.72115, 37.97255, 80.0], [23.72119, 37.97255, 80.0], [23.72124, 37.97255, 80.0], [23.72127, 37.97255, 80.0], [23.72131, 37.97255, 80.0], [23.72135, 37.97255, 80.0], [23.72138, 37.97257, 80.0], [23.72143, 37.97257, 80.0], [23.72149, 37.9726, 79.0], [23.72162, 37.97266, 78.0], [23.72172, 37.9727, 77.0], [23.72186, 37.97275, 73.0], [23.72172, 37.9727, 77.0], [23.72162, 37.97266, 78.0], [23.72149, 37.9726, 79.0], [23.72143, 37.97257, 80.0], [23.72138, 37.97257, 80.0], [23.72135, 37.97255, 80.0], [23.72131, 37.97255, 80.0], [23.72127, 37.97255, 80.0], [23.72124, 37.97255, 80.0], [23.72119, 37.97255, 80.0], [23.72115, 37.97255, 80.0], [23.72112, 37.97254, 80.0], [23.72108, 37.97254, 80.0], [23.72104, 37.97254, 80.0], [23.72101, 37.97254, 80.0], [23.72099, 37.97255, 80.0], [23.72096, 37.97256, 79.0], [23.72094, 37.97257, 79.0], [23.72092, 37.97258, 79.0], [23.7209, 37.97258, 79.0], [23.72088, 37.97257, 79.0], [23.72085, 37.97256, 79.0], [23.72083, 37.97257, 79.0], [23.7208, 37.97258, 79.0], [23.72078, 37.97259, 79.0], [23.72076, 37.97261, 79.0], [23.72074, 37.97263, 79.0], [23.72072, 37.97265, 79.0], [23.72072, 37.97267, 79.0], [23.72071, 37.97271, 79.0], [23.72071, 37.97275, 79.0], [23.72071, 37.97281, 79.0], [23.72068, 37.97278, 79.0], [23.72059, 37.97274, 80.0], [23.72079, 37.97221, 83.0], [23.72063, 37.97221, 83.0], [23.72041, 37.97223, 84.0], [23.72012, 37.97229, 85.0], [23.71996, 37.97233, 85.0], [23.71984, 37.97236, 86.0], [23.71973, 37.9724, 86.0], [23.71957, 37.97247, 84.0], [23.71945, 37.97254, 83.0], [23.71933, 37.97262, 84.0], [23.7189, 37.97298, 96.0], [23.71879, 37.97308, 99.0], [23.71868, 37.97309, 103.0], [23.71863, 37.97299, 105.0], [23.71856, 37.97291, 106.0], [23.71852, 37.97287, 105.0], [23.71848, 37.97283, 105.0], [23.71844, 37.9728, 104.0], [23.7184, 37.97278, 104.0], [23.71836, 37.97277, 103.0], [23.7183, 37.97276, 103.0], [23.71823, 37.97276, 103.0], [23.718, 37.97276, 102.0], [23.71794, 37.97274, 100.0], [23.71787, 37.9727, 98.0], [23.71783, 37.97266, 97.0], [23.71779, 37.97263, 96.0], [23.71777, 37.9726, 96.0], [23.71776, 37.97258, 95.0], [23.71776, 37.97255, 94.0], [23.71776, 37.97253, 94.0], [23.71777, 37.9725, 93.0], [23.71778, 37.97246, 93.0], [23.71779, 37.97242, 92.0], [23.71818, 37.9721, 88.0], [23.71831, 37.97196, 95.0], [23.71842, 37.97196, 97.0], [23.7186, 37.97184, 104.0], [23.71867, 37.9718, 106.0], [23.71877, 37.97178, 107.0], [23.71881, 37.97178, 107.0], [23.71908, 37.9716, 102.0], [23.71911, 37.97159, 102.0], [23.71936, 37.97148, 96.0], [23.71993, 37.97143, 104.0], [23.72001, 37.97139, 106.0], [23.72029, 37.97092, 106.0], [23.72015, 37.97073, 104.0], [23.72008, 37.97064, 105.0], [23.71999, 37.9705, 107.0], [23.71997, 37.97047, 107.0], [23.71992, 37.97033, 108.0], [23.71993, 37.97023, 109.0], [23.71987, 37.97015, 108.0], [23.71993, 37.9701, 108.0], [23.71998, 37.97007, 108.0], [23.72006, 37.96997, 107.0], [23.72019, 37.96996, 106.0], [23.72018, 37.96981, 105.0], [23.72013, 37.96974, 105.0], [23.72011, 37.96967, 103.0], [23.7201, 37.96962, 102.0], [23.72009, 37.96958, 100.0], [23.72005, 37.9695, 98.0], [23.71999, 37.96947, 98.0], [23.71996, 37.96942, 97.0], [23.71995, 37.96934, 97.0], [23.71993, 37.96927, 97.0], [23.71997, 37.96923, 96.0], [23.71997, 37.96922, 96.0], [23.72001, 37.96922, 95.0], [23.72008, 37.96925, 96.0], [23.72011, 37.96923, 96.0], [23.72017, 37.96922, 96.0], [23.72021, 37.96921, 97.0], [23.72026, 37.96918, 97.0], [23.72032, 37.96914, 100.0], [23.72035, 37.96912, 102.0], [23.72032, 37.96914, 100.0], [23.72035, 37.96912, 102.0], [23.72048, 37.96905, 108.0], [23.72051, 37.96901, 111.0], [23.72054, 37.96886, 119.0], [23.72057, 37.96881, 120.0], [23.72056, 37.96878, 122.0], [23.72053, 37.96874, 124.0], [23.72056, 37.96867, 126.0], [23.72062, 37.9686, 128.0], [23.72065, 37.96855, 128.0], [23.7207, 37.96854, 128.0], [23.72077, 37.96845, 130.0], [23.7208, 37.96839, 132.0], [23.7209, 37.96833, 133.0], [23.72093, 37.96828, 133.0], [23.72097, 37.96823, 133.0], [23.72105, 37.96817, 134.0], [23.72113, 37.96811, 136.0], [23.72116, 37.96808, 137.0], [23.72096, 37.96789, 137.0], [23.72098, 37.96786, 138.0], [23.72101, 37.96782, 139.0], [23.721, 37.96773, 140.0], [23.72111, 37.96767, 144.0], [23.72116, 37.9676, 146.0], [23.72124, 37.96757, 147.0], [23.72134, 37.96754, 148.0], [23.72139, 37.9675, 148.0], [23.72131, 37.96747, 148.0], [23.72107, 37.96739, 145.0], [23.72099, 37.9674, 143.0], [23.72079, 37.9673, 137.0], [23.7206, 37.96721, 130.0], [23.72039, 37.96714, 122.0], [23.72031, 37.96709, 121.0], [23.72023, 37.96702, 121.0], [23.71999, 37.9669, 120.0], [23.71989, 37.96684, 119.0], [23.71963, 37.96672, 119.0], [23.71939, 37.9666, 119.0], [23.71906, 37.96645, 115.0], [23.71869, 37.96622, 111.0], [23.7184, 37.96612, 113.0], [23.7182, 37.96603, 114.0], [23.71813, 37.96599, 114.0], [23.7179, 37.96587, 112.0], [23.71776, 37.96576, 106.0], [23.7179, 37.96587, 112.0], [23.71813, 37.96599, 114.0], [23.71807, 37.96599, 115.0], [23.71802, 37.96597, 114.0], [23.71786, 37.96594, 113.0], [23.71779, 37.96595, 113.0], [23.71773, 37.96599, 112.0], [23.71771, 37.96604, 112.0], [23.71773, 37.96614, 114.0], [23.7178, 37.96626, 114.0], [23.71786, 37.96632, 114.0], [23.71775, 37.96644, 110.0], [23.71767, 37.96647, 107.0], [23.7176, 37.96646, 106.0], [23.71755, 37.96649, 104.0], [23.71751, 37.9665, 102.0], [23.71747, 37.96653, 101.0], [23.71736, 37.96655, 98.0], [23.71731, 37.96649, 97.0], [23.71725, 37.96646, 95.0], [23.7172, 37.96632, 93.0], [23.71718, 37.96604, 93.0], [23.71711, 37.96576, 91.0], [23.71712, 37.9656, 89.0], [23.71718, 37.96544, 87.0], [23.7172, 37.96539, 86.0], [23.71727, 37.96533, 86.0], [23.71743, 37.96529, 88.0], [23.71742, 37.96529, 88.0], [23.7177, 37.96527, 91.0], [23.71788, 37.96523, 92.0], [23.71803, 37.96524, 96.0], [23.71826, 37.96531, 102.0], [23.71843, 37.96546, 106.0], [23.71904, 37.9656, 97.0], [23.71902, 37.96563, 98.0], [23.71899, 37.96566, 100.0], [23.71902, 37.96563, 98.0], [23.71904, 37.9656, 97.0], [23.7191, 37.96556, 94.0], [23.71919, 37.96559, 94.0], [23.71924, 37.96561, 94.0], [23.71991, 37.96585, 93.0], [23.72029, 37.96596, 95.0], [23.7206, 37.96604, 100.0], [23.72076, 37.96612, 104.0], [23.72087, 37.96616, 105.0], [23.7211, 37.96622, 107.0], [23.72129, 37.9663, 110.0], [23.72145, 37.96637, 112.0], [23.72149, 37.96639, 112.0], [23.72173, 37.96644, 110.0], [23.72193, 37.96645, 106.0], [23.7221, 37.96653, 108.0], [23.72219, 37.96662, 111.0], [23.72221, 37.96664, 112.0], [23.72238, 37.96681, 114.0], [23.72249, 37.967, 116.0], [23.72257, 37.96706, 115.0], [23.72277, 37.96716, 111.0], [23.72282, 37.96719, 110.0], [23.72308, 37.96739, 109.0], [23.72312, 37.96748, 111.0], [23.72308, 37.96765, 114.0], [23.72311, 37.96775, 115.0], [23.72313, 37.96782, 115.0], [23.72318, 37.96797, 115.0], [23.72309, 37.96831, 115.0], [23.72264, 37.96876, 108.0], [23.72263, 37.96876, 108.0], [23.72222, 37.96899, 110.0], [23.72185, 37.96877, 116.0], [23.72175, 37.96873, 118.0], [23.72163, 37.96876, 118.0], [23.72151, 37.96897, 118.0], [23.72146, 37.96923, 115.0], [23.72154, 37.96934, 111.0], [23.72155, 37.96941, 109.0], [23.72219, 37.97007, 99.0], [23.72207, 37.97015, 98.0], [23.72213, 37.97013, 99.0], [23.7222, 37.97012, 99.0], [23.72226, 37.97013, 99.0], [23.72231, 37.97014, 99.0], [23.72249, 37.97021, 100.0], [23.72282, 37.97017, 99.0], [23.72366, 37.97006, 102.0], [23.72409, 37.97001, 98.0], [23.72433, 37.96997, 93.0], [23.72447, 37.96995, 92.0], [23.72477, 37.9699, 97.0], [23.72478, 37.96998, 96.0]]],
                            "bbox": [23.72478, 37.97474, 23.71711, 37.96523]
                        }
                            """.trimIndent()).coordinates()[0]
                binding.mapView.gestures.addOnMapLongClickListener { point ->
                    findRoute(coordinates)
                    true
                }
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

        // set initial sounds button state
        binding.soundButton.unmute()

        // start the trip session to being receiving location updates in free drive
        // and later when a route is set also receiving route progress updates
        mapboxNavigation.startTripSession()
    }


    override fun onStart() {
        super.onStart()

        // register event listeners
        mapboxNavigation.registerRoutesObserver(routesObserver)
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.registerLocationObserver(locationObserver)
        mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
        mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)

        if (mapboxNavigation.getRoutes().isEmpty()) {
            // if simulation is enabled (ReplayLocationEngine set to NavigationOptions)
            // but we're not simulating yet,
            // push a single location sample to establish origin
            mapboxReplayer.pushEvents(
                listOf(
                    ReplayRouteMapper.mapToUpdateLocation(
                        eventTimestamp = 0.0,
                        point = Point.fromLngLat(23.72004, 37.97462)
                    )
                )
            )
            mapboxReplayer.playFirstLocation()
        }
    }

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
        super.onDestroy()
        MapboxNavigationProvider.destroy()
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
    }

//    private fun matchRoute() {
//        MapboxMapMatching.builder().coordinates()
//    }

    private fun findRoute(destination: Point) {
        val originLocation = navigationLocationProvider.lastLocation
        val originPoint = originLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return

        // execute a route request
        // it's recommended to use the
        // applyDefaultNavigationOptions and applyLanguageAndVoiceUnitOptions
        // that make sure the route request is optimized
        // to allow for support of all of the Navigation SDK features
        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .coordinatesList(listOf(originPoint, destination))
                // provide the bearing for the origin of the request to ensure
                // that the returned route faces in the direction of the current user movement
                .bearingsList(
                    listOf(
                        Bearing.builder()
                            .angle(originLocation.bearing.toDouble())
                            .degrees(45.0)
                            .build(),
                        null
                    )
                )
                .build(),
            object : RouterCallback {
                override fun onRoutesReady(
                    routes: List<DirectionsRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    setRouteAndStartNavigation(routes)
                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {
                    // no impl
                }

                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                    // no impl
                }
            }
        )
    }


    private fun findRoute(coordinates: MutableList<Point>) {
        val originLocation = navigationLocationProvider.lastLocation
        val originPoint = originLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return

        // execute a route request
        // it's recommended to use the
        // applyDefaultNavigationOptions and applyLanguageAndVoiceUnitOptions
        // that make sure the route request is optimized
        // to allow for support of all of the Navigation SDK features
        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .coordinatesList(coordinates)
                // provide the bearing for the origin of the request to ensure
                // that the returned route faces in the direction of the current user movement
                .bearingsList(
                    listOf(
                        Bearing.builder()
                            .angle(originLocation.bearing.toDouble())
                            .degrees(45.0)
                            .build(),
                        null
                    )
                )
                .build(),
            object : RouterCallback {
                override fun onRoutesReady(
                    routes: List<DirectionsRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    setRouteAndStartNavigation(routes)
                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {
                    // no impl
                }

                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                    // no impl
                }
            }
        )
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
