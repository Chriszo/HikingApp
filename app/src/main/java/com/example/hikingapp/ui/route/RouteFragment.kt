package com.example.hikingapp.ui.route

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hikingapp.GlobalUtils
import com.example.hikingapp.R
import com.example.hikingapp.SampleMapActivity
import com.example.hikingapp.SampleNavigationActivity
import com.example.hikingapp.domain.Route
import com.example.hikingapp.domain.map.ExtendedMapPoint
import com.example.hikingapp.domain.map.MapPoint
import com.example.hikingapp.persistence.MapInfo
import com.example.hikingapp.persistence.mock.db.MockDatabase
import com.example.hikingapp.services.culture.CultureUtils
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.mapbox.api.tilequery.MapboxTilequery
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.MultiLineString
import com.mapbox.geojson.Point
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.route_fragment.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.Collectors

class RouteFragment : Fragment() {

    companion object {
        fun newInstance() = RouteFragment()
    }

    private lateinit var viewModel: RouteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.route_fragment, container, false)

        val navHostFragment =  childFragmentManager.findFragmentById(R.id.route_nav_fragment) as NavHostFragment
        val navController = navHostFragment.navController



        val navView = view.info_nav_view
        navView.menu.clear()
        navView.inflateMenu(R.menu.route_nav_menu)

//        navView.setupWithNavController(navController)


        /*val backButton = view.to_home.setOnClickListener {
            val homeFragment = DiscoverFragment()
            val transaction = fragmentManager?.beginTransaction()
            transaction?.replace(nav_host_fragment_activity_main.id, homeFragment)?.commit()
        }*/

        val showMapButton = view.show_map
        showMapButton.setOnClickListener {
            activity?.let {
                val intent = Intent(it, SampleMapActivity::class.java)
                it.startActivity(intent)
            }
        }

        val showNavButton = view.navigate
        showNavButton.setOnClickListener {
            activity?.let {
                val intent = Intent(it, SampleNavigationActivity::class.java)
                it.startActivity(intent)
            }
        }

        //TODO Retrieve current Route Map information
        var routeName = savedInstanceState?.get("RouteName")

        val route = Route()
        routeName = routeName?.let { it as String }

        val mapInfo = retrieveMapInformation(routeName)
        route.mapInfo = mapInfo

        GlobalScope.launch {
            route.cultureInfo = CultureUtils.retrieveSightInformation(route.mapInfo!!.origin)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setRouteElevationData(route.mapInfo!!, getString(R.string.prodMode).toBooleanStrict(), view.graph )
        }

        return view
    }

    private fun retrieveMapInformation(routeName: String?): MapInfo {

        val jsonSource =
            context?.assets?.open(MockDatabase.routesMap["Philopapou"]?.second!!)?.readBytes()?.toString(Charsets.UTF_8)
        val routeJson: MultiLineString =
            FeatureCollection.fromJson(jsonSource!!).features()?.get(0)?.geometry() as MultiLineString

        val origin: Point = routeJson.coordinates()[0][0]
        val destination: Point = routeJson.coordinates()[0][routeJson.coordinates()[0].size - 1]

        val mapPoints = getMapPoints(routeJson)

        return MapInfo(
            origin,
            destination,
            routeJson.bbox()!!,
            routeJson,
            mapPoints,
            MockDatabase.routesMap["Philopapou"]?.second!!,
            false
        )
    }

    private fun getMapPoints(json: MultiLineString): List<MapPoint> {
        return json.coordinates()[0].map {
            MapPoint(it)
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(RouteViewModel::class.java)
        // TODO: Use the ViewModel
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
        mapInfo: MapInfo,
        isExecutable: Boolean,// TODO Remove it when you must. It is used in order to bypass execution during test., graph: com.jjoe64.graphview.GraphView){}, graph: com.jjoe64.graphview.GraphView){}, graph: com.jjoe64.graphview.GraphView){}
        graph: GraphView
    ) {
        if (isExecutable) {

            val series = LineGraphSeries<DataPoint>()

            if (mapInfo.elevationDataLoaded) { // Means that these data may be stored in db and can be retrieved from there

                mapInfo.mapPoints
                    ?.stream()
                    ?.filter { point -> point.elevation != null }
                    ?.forEach {
                        series.appendData(
                            DataPoint(
                                mapInfo.mapPoints.indexOf(it).toDouble(),
                                it.elevation!!.toDouble()
                            ),
                            false,
                            mapInfo.mapPoints.size
                        )
                    }
                graph.addSeries(series)

            } else { // Data have not been loaded so need Tilequery async API calls to populate data.
                GlobalScope.launch {

                    collectionElevData(mapInfo).collect {

                        println("Drawing graph")
                        it.stream().forEach { ep ->
                            series.appendData(
                                DataPoint(ep.index.toDouble(), ep.elevation!!.toDouble()),
                                false,
                                it.size
                            )
                        }
                        graph.addSeries(series)
                    }

                }
            }
        }
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
                pointIndexMap[it.point.longitude().toString()+","+it.point.latitude().toString()] =
                    it.index
                callElevationDataAPI(it, mapInfo, pointIndexMap, elevationData)
            }

        }
        while (elevationData.size != extendedMapPoints.size) {
            // wait
        }
        elevationData = elevationData
            .stream()
            .filter { it.elevation != -10000 }
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
                            ?.mapToInt { feature ->
                                feature.properties()?.get("ele")?.asInt!!
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