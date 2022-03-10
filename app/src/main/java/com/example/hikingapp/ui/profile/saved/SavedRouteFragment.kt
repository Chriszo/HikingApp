package com.example.hikingapp.ui.profile.saved

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.hikingapp.R
import com.example.hikingapp.SampleMapActivity
import com.example.hikingapp.SampleNavigationActivity
import com.example.hikingapp.domain.map.ExtendedMapPoint
import com.example.hikingapp.domain.map.MapInfo
import com.example.hikingapp.domain.map.MapPoint
import com.example.hikingapp.domain.route.Route
import com.example.hikingapp.domain.weather.WeatherForecast
import com.example.hikingapp.services.culture.CultureUtils
import com.example.hikingapp.services.map.MapService
import com.example.hikingapp.services.map.MapServiceImpl
import com.example.hikingapp.services.weather.WeatherService
import com.example.hikingapp.services.weather.WeatherServiceImpl
import com.example.hikingapp.ui.viewModels.RouteViewModel
import com.example.hikingapp.utils.GlobalUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mapbox.api.tilequery.MapboxTilequery
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import kotlinx.android.synthetic.main.fragment_saved_route.view.*
import kotlinx.android.synthetic.main.route_fragment.view.info_nav_view
import kotlinx.android.synthetic.main.route_fragment.view.navigate
import kotlinx.android.synthetic.main.route_fragment.view.routeName
import kotlinx.android.synthetic.main.route_fragment.view.routeRating
import kotlinx.android.synthetic.main.route_fragment.view.route_info_image
import kotlinx.android.synthetic.main.route_fragment.view.show_map
import kotlinx.android.synthetic.main.route_fragment.view.stateName
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.stream.Collectors

class SavedRouteFragment : Fragment() {

    private lateinit var routeMap: String

    private val routeViewModel: RouteViewModel by activityViewModels()

    private lateinit var mapService: MapService

    private lateinit var weatherService: WeatherService

    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    private lateinit var route: Route

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_saved_route, container, false)

        route = arguments?.get("route") as Route

        if (routeViewModel.photos.value.isNullOrEmpty()) {
            routeViewModel.photos.postValue(route.photos)
        }

        mapService = MapServiceImpl()
        weatherService = WeatherServiceImpl()

        val removeBookmarkButton = view.route_remove_bookmark

        removeBookmarkButton.setOnClickListener {

            requireArguments().putSerializable("removeSaved", route)
        }

        database.getReference("routeMaps").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                routeMap = (snapshot.value as HashMap<String, *>).entries
                    .stream()
                    .filter { routeMapEntry -> routeMapEntry.key.split("_")[1].toLong() == route.routeId }
                    .map { it.value as String }
                    .findFirst().orElse(null)

                route.mapInfo = mapService.getMapInformation(getJson())

                GlobalScope.launch {

                    if (routeViewModel.route.value?.routeInfo?.elevationData.isNullOrEmpty()) {
                        route.routeInfo!!.elevationData = setRouteElevationData(route)
                    }

                    val cultureInfoJob =
                        if (routeViewModel.route.value?.cultureInfo?.sights.isNullOrEmpty()) {
                            GlobalScope.launch {

                                if (Objects.isNull(routeViewModel.cultureInfo.value)) {
                                    route.cultureInfo =
                                        CultureUtils.retrieveSightInformation(route.mapInfo!!.origin)
                                    routeViewModel.cultureInfo.postValue(route.cultureInfo)
                                }
                            }
                        } else {
                            null
                        }

                    val weatherInfoJob =
                        if (routeViewModel.route.value?.weatherForecast?.weatherForecast.isNullOrEmpty()) {
                            GlobalScope.launch {
                                val weatherForecast = WeatherForecast()
                                weatherForecast.weatherForecast = weatherService.getForecastForDays(
                                    route.mapInfo!!.origin,
                                    4,
                                    getString(R.string.prodMode).toBooleanStrict()
                                ) //TODO remove this test flag when in PROD
                                route.weatherForecast = weatherForecast
                            }
                        } else {
                            null
                        }

                    cultureInfoJob?.join()
                    weatherInfoJob?.join()

                    routeViewModel.route.postValue(route)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        initializeNavigationComponents(view)

        initializeButtonListeners(view)

        view.route_info_image.setImageResource(route.mainPhoto!!)
        view.routeName.text = route.routeName
        view.stateName.text = route.stateName
        view.routeRating.rating = route.routeInfo!!.rating!!
        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        println("on destroy view")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("on destroy")
    }

    private fun initializeButtonListeners(view: View) {


        val showMapButton = view.show_map
        showMapButton?.setOnClickListener {
            activity?.let {
                val intent = Intent(it, SampleMapActivity::class.java)
                intent.putExtra("routeName", route.routeName)
                intent.putExtra("routeMap", routeMap)
                it.startActivity(intent)
            }
        }

        val showNavButton = view.navigate
        showNavButton?.setOnClickListener {
            activity?.let {
                val intent = Intent(it, SampleNavigationActivity::class.java)
                intent.putExtra("routeName", route.routeName)
                intent.putExtra("routeMap", routeMap)
                it.startActivity(intent)
            }
        }
    }

    private fun initializeNavigationComponents(view: View) {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        val navView = view.info_nav_view
        navView?.setupWithNavController(navController)
    }

    private fun getJson(): String {
        return requireContext().assets.open(routeMap).readBytes()
            .toString(Charsets.UTF_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel = ViewModelProvider(this)[RouteViewModel::class.java]
        // TODO: Use the ViewModel
//        viewModel.defineRoute(route)
    }


    // METHODS FOR RETRIEVING ELEVATION DATA
    private fun filterRoutePoints(
        coordinates: List<MapPoint>,
        modulo: Int
    ): MutableList<ExtendedMapPoint> {

        return coordinates.filterIndexed { index, _ -> index % modulo == 0 }
            .map { mapPoint ->
                ExtendedMapPoint(mapPoint.point, mapPoint.elevation, coordinates.indexOf(mapPoint))
            }
            .toMutableList()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setRouteElevationData(
        route: Route// TODO Remove it when you must. It is used in order to bypass execution during test., graph: com.jjoe64.graphview.GraphView){}, graph: com.jjoe64.graphview.GraphView){}, graph: com.jjoe64.graphview.GraphView){}
    ): MutableList<Long> {
        var elevationData = mutableListOf<Long>()

        if (getString(R.string.prodMode).toBooleanStrict()) { // TODO Remove. Only for test
            if (route.mapInfo!!.elevationDataLoaded) { // Means that these data may be stored in db and can be retrieved from there
                elevationData = (route.mapInfo!!.mapPoints?.stream()?.map { it.elevation }
                    ?.collect(Collectors.toList())?.toMutableList()
                    ?: emptyList<Long>()) as MutableList<Long>
                route.routeInfo?.elevationData = elevationData
            } else {
                // TODO Make a query to DB when implemented
                // Data have not been loaded so need Tilequery async API calls to populate data.
                GlobalScope.launch {

                    collectionElevData(route.mapInfo!!).collect { elevationDataList ->
                        elevationData =
                            elevationDataList.stream().map { it.elevation }
                                .collect(Collectors.toList())
                        route.routeInfo?.elevationData = elevationData
                    }
                    routeViewModel.elevationData.postValue(elevationData)
                }
            }
        }
        return elevationData
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun collectionElevData(
        mapInfo: MapInfo
    ): Flow<MutableList<ExtendedMapPoint>> = flow {

        val pointIndexMap = HashMap<String, Int>()
        var elevationData = mutableListOf<ExtendedMapPoint>()
        val extendedMapPoints = filterRoutePoints(mapInfo.mapPoints!!, 3)


        extendedMapPoints.stream().forEach {

            GlobalScope.launch {
                pointIndexMap[it.point.longitude().toString() + "," + it.point.latitude()
                    .toString()] =
                    it.index
                callElevationDataAPI(it, mapInfo, pointIndexMap, elevationData)
            }

        }
        while (elevationData.size != extendedMapPoints.size) {
            // wait
        }
        elevationData = elevationData
            .stream()
            .filter { it.elevation != -10000L }
            .sorted(Comparator.comparing(ExtendedMapPoint::index))
            .collect(Collectors.toList()).toMutableList()

        emit(elevationData)
    }


    private suspend fun callElevationDataAPI(
        extendedPoint: ExtendedMapPoint,
        mapInfo: MapInfo,
        pointIndexMap: HashMap<String, Int>,
        elevationData: MutableList<ExtendedMapPoint>
    ) {

        if (extendedPoint.index % 50 == 0) {
            delay(3000)
            println("WAITING")
        }

        val elevationQuery = formElevationRequestQuery(extendedPoint)

        elevationQuery.enqueueCall(object : Callback<FeatureCollection> {

            override fun onResponse(
                call: Call<FeatureCollection>,
                response: Response<FeatureCollection>
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    if (response.isSuccessful) {
                        val point =
                            (response.body()?.features()?.get(0)?.geometry() as Point)
                        val pointsMapKey =
                            point.longitude().toString() + "," + point.latitude().toString()


                        response.body()?.features()
                            ?.stream()
                            ?.mapToLong { feature ->
                                feature.properties()?.get("ele")?.asLong!!
                            }
                            ?.max()
                            ?.ifPresent { max ->
                                val index = pointIndexMap[pointsMapKey]
                                mapInfo.mapPoints?.get(index!!)?.elevation = max
                                extendedPoint.elevation = max
                                elevationData.add(extendedPoint)
                            }
                        Log.d(
                            "R",
                            "" + elevationData.indexOf(extendedPoint) + ", " + extendedPoint.elevation
                        )
                        call.cancel()
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

    private fun formElevationRequestQuery(extendedPoint: ExtendedMapPoint): MapboxTilequery {
        return MapboxTilequery.builder()
            .accessToken(getString(R.string.mapbox_access_token))
            .tilesetIds(GlobalUtils.TERRAIN_ID)
            .limit(50)
            .layers(GlobalUtils.TILEQUERY_ATTRIBUTE_REQUESTED_ID)
            .query(extendedPoint.point)
            .build()
    }
}