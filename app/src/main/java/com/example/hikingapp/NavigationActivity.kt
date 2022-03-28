package com.example.hikingapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.hikingapp.databinding.ActivityNavigationBinding
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
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
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
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
import com.mapbox.turf.TurfMeasurement
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors

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
class NavigationActivity : AppCompatActivity() {

    private var initialFilteredCoordinates: List<Point> = mutableListOf()
    private var mainRoutes: MutableList<DirectionsRoute> = mutableListOf()
    private var currentLocation: Point? = null
    private var checkPoints: MutableList<Int> = mutableListOf()
    private var nearbyPointsOfInterest = mutableSetOf<Long>()
    private var checkPointsIndex = 0
    private var associatedSights: MutableList<Sight>? = null
    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }
    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    private var userAuthInfo: FirebaseUser? = null
    private var currentRoute: Route? = null
    private var userNavigationData: UserNavigationData? = null
    private var timeCounter: Long = 0L
    private var isOutOfRoute = false

    lateinit var currentPhotoPath: String

    private var mapInfo: MapInfo? = null

    private val mapService: MapService by lazy {
        MapServiceImpl()
    }

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

    private val checkpointCounter: AtomicInteger = AtomicInteger()

    private val arrivalObserver = object : ArrivalObserver {

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onFinalDestinationArrival(routeProgress: RouteProgress) {
            println("FINAL DESTINATION REACHED!")

            userNavigationData?.timeSpent = System.currentTimeMillis() - timeCounter
            userNavigationData?.distanceCovered = routeProgress.distanceTraveled.toDouble()

            if (isOutOfRoute) {
                Toast.makeText(
                    this@NavigationActivity,
                    "You are on route again! \nYou have arrived at the last visited checkpoint!",
                    Toast.LENGTH_LONG
                ).show()
                mapboxNavigation.setRoutes(mainRoutes, checkPointsIndex)
                isOutOfRoute = false
            } else {
                val mainIntent =
                    Intent(this@NavigationActivity, EndOfNavigationActivity::class.java)
                mainIntent.putExtra("route", currentRoute)
                intent.putExtra("authInfo", userAuthInfo)
                mainIntent.putExtra("userNavigationData", userNavigationData)

                LocalDatabase.saveNavigationDataLocally(userAuthInfo!!.uid, userNavigationData!!)
                FirebaseUtils.persistNavigation(userAuthInfo!!.uid, userNavigationData!!)

                startActivity(mainIntent)
                finish()
            }
        }

        override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {
            checkPointsIndex++
            println("Next checkpoint: ${checkPointsIndex}")
        }

        override fun onWaypointArrival(routeProgress: RouteProgress) {
            println("CHECKPOINT $checkPointsIndex reached")
            Toast.makeText(
                this@NavigationActivity,
                "You have arrived at $checkPointsIndex",
                Toast.LENGTH_LONG
            ).show()
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
    private lateinit var binding: ActivityNavigationBinding

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
                            runOnUiThread {
                                if (!nearbyPointsOfInterest.contains(sight.sightId)) {
                                    nearbyPointsOfInterest.add(sight.sightId)
                                    Toast.makeText(
                                        this@NavigationActivity,
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
                    this@NavigationActivity,
                    error.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            },
            {
                binding.maneuverView.visibility = View.VISIBLE
                binding.maneuverView.renderManeuvers(maneuvers)
            }
        )
        // update user navigation data
        userNavigationData!!.distanceCovered = routeProgress.distanceTraveled.toDouble()
//        userNavigationData.timeSpent = System.currentTimeMillis() - timeCounter

        // update bottom trip progress summary

        binding.textDistanceRemaining.text = getString(
            R.string.distance_remaining_content, GlobalUtils.getTwoDigitsDistance(
                routeProgress.distanceRemaining.toDouble(),
                DistanceUnitType.KILOMETERS
            )
        )


        binding.textDistanceCovered.text = getString(
            R.string.distance_covered_content, GlobalUtils.getTwoDigitsDistance(
                userNavigationData!!.distanceCovered,
                DistanceUnitType.KILOMETERS
            )
        )
        binding.textTimeEstimated.text = getString(
            R.string.estimated_time_content, String.format(
                "%.2f",
                GlobalUtils.getTimeInMinutes(routeProgress.durationRemaining)
            )
        )

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private val offRouteObserver = OffRouteObserver { offRoute ->
        if (offRoute) {
            isOutOfRoute = true
            runOnUiThread {
                Toast.makeText(
                    this@NavigationActivity,
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
                                binding.currentElevation.text =
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

        if (intent?.extras?.containsKey("authInfo") == true && intent!!.extras!!.get("authInfo") != null) {

            userAuthInfo = intent!!.extras!!.get("authInfo") as FirebaseUser?
            currentRoute = intent!!.extras!!.get("route") as Route?

            binding = ActivityNavigationBinding.inflate(layoutInflater)
            setContentView(binding.root)

            mapboxMap = binding.mapView.getMapboxMap()

            val routeMapEntity = LocalDatabase.getRouteMapContent(currentRoute!!.routeId)

            mapInfo = mapService.getMapInformation(
                routeMapEntity!!.routeMapContent,
                routeMapEntity.routeMapName
            )

            associatedSights = LocalDatabase.getSightsOfRoute(currentRoute!!.routeId)

            binding.textDistanceRemaining.text = getString(R.string.distance_remaining_empty)

            binding.textDistanceCovered.text = getString(R.string.distance_covered_empty)

            binding.textTimeEstimated.text = getString(R.string.estimated_time_empty)

            binding.currentElevation.text = ""

            // initialize the location puck
            binding.mapView.location.apply {
                this.locationPuck = LocationPuck2D(
                    bearingImage = ContextCompat.getDrawable(
                        this@NavigationActivity,
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
                    NavigationCameraState.FOLLOWING -> binding.routeOverview.visibility =
                        View.VISIBLE
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
            binding.stop.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    clearRouteAndStopNavigation()
                }
                val intent = Intent(this, EndOfNavigationActivity::class.java)
                userNavigationData?.timeSpent = System.currentTimeMillis() - timeCounter
                intent.putExtra("route", currentRoute)
                intent.putExtra("authInfo", userAuthInfo)
                intent.putExtra("userNavigationData", userNavigationData)

                LocalDatabase.saveNavigationDataLocally(userAuthInfo!!.uid, userNavigationData!!)
                FirebaseUtils.persistUserInCompletedRoute(
                    userAuthInfo!!.uid,
                    userNavigationData!!.routeId
                )

                startActivity(intent)
                finish()
            }

            // Action which starts the Navigation
            binding.play.setOnClickListener {
                if (mapboxNavigation.getRoutes().isEmpty()) {

                    val routePoints = if (mapInfo!!.jsonRoute is MultiLineString) {
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
                binding.play.visibility = View.GONE
                binding.pause.visibility = View.VISIBLE
            }
            binding.pause.setOnClickListener {
                pauseNavigation()
                binding.play.visibility = View.VISIBLE
                binding.pause.visibility = View.GONE
            }
            binding.lostButton.setOnClickListener {
                binding.pause.performClick()
                database.getReference("contacts").child("${userAuthInfo!!.uid}")
                    .addValueEventListener(object : ValueEventListener {
                        @RequiresApi(Build.VERSION_CODES.N)
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val contacts = snapshot.value as MutableList<String>
                                contacts.forEach { phoneNumber -> phoneNumber.apply { sendSMS(this) } }

                                Toast.makeText(
                                    this@NavigationActivity,
                                    "SMS message has been sent to your contacts.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@NavigationActivity,
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
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
                    requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        GlobalUtils.CAMERA_REQUEST
                    )


                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, GlobalUtils.CAMERA_REQUEST)
            }

            binding.switchMapStyle.setOnClickListener {
                if (binding.mapStyleOptions.visibility == View.GONE) {
                    binding.mapStyleOptions.visibility = View.VISIBLE
                } else if (binding.mapStyleOptions.visibility == View.VISIBLE) {
                    binding.mapStyleOptions.visibility = View.GONE
                }
            }

            binding.trafficMapStyle.setOnClickListener {
                setMapStyle(Style.TRAFFIC_DAY, currentRoute!!.mapInfo!!)
                binding.mapStyleOptions.visibility = View.GONE
            }

            binding.satelliteMapStyle.setOnClickListener {
                setMapStyle(Style.SATELLITE, currentRoute!!.mapInfo!!)
                binding.mapStyleOptions.visibility = View.GONE
            }

            binding.terrainMapStyle.setOnClickListener {
                setMapStyle(Style.OUTDOORS, currentRoute!!.mapInfo!!)
                binding.mapStyleOptions.visibility = View.GONE
            }

            // set initial sounds button state
            binding.soundButton.unmute()

            // start the trip session to being receiving location updates in free drive
            // and later when a route is set also receiving route progress updates
            mapboxNavigation.startTripSession()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun sendSMS(phoneNumber: String?) {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.SEND_SMS
                )
            ) {
            } else {
                ActivityCompat.requestPermissions(
                    this,
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GlobalUtils.CAMERA_REQUEST && resultCode == RESULT_OK) {
            val imageBitmap = data!!.extras!!.get("data") as Bitmap
            val outputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()

            storage.getReference("routes/${currentRoute!!.routeId}/photos/photo_1_5.jpg")
                .putBytes(byteArray).addOnSuccessListener {
                    Toast.makeText(this@NavigationActivity, "Photo uploaded successfully.", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {Toast.makeText(this@NavigationActivity, "Photo uploading failed.", Toast.LENGTH_LONG).show()  }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, GlobalUtils.CAMERA_REQUEST)
                }
            }
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


    private fun setMapStyle(mapStyle: String, mapInfo: MapInfo) {
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


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()
        println("INTO START")

        if (intent?.extras?.containsKey("authInfo") == true && intent!!.extras!!.get("authInfo") != null) {
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
        if (intent?.extras?.containsKey("authInfo") == true && intent!!.extras!!.get("authInfo") != null) {

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

    override fun onDestroy() {
        super.onDestroy()
        println("ON DESTROY CALLED")
        MapboxNavigationProvider.destroy()
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()

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

        //DEFAULT

//            .waypointIndices(0, 12)
//            .steps(true)
//            .voiceInstructions(true)
//            .bannerInstructions(true)
//            .profile(DirectionsCriteria.PROFILE_DRIVING)
//            .build()

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
        binding.soundButton.visibility = View.VISIBLE
        binding.routeOverview.visibility = View.VISIBLE
        binding.tripProgressCard.visibility = View.VISIBLE

        // move the camera to overview when new route is available
        navigationCamera.requestNavigationCameraToOverview()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun clearRouteAndStopNavigation() {
        // clear
        mapboxNavigation.setRoutes(listOf())
        userNavigationData!!.timeSpent = System.currentTimeMillis() - timeCounter
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
            userNavigationData!!.timeSpent = System.currentTimeMillis() - timeCounter
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
